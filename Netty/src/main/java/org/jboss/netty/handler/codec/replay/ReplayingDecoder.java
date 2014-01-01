/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.codec.replay;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * A specialized variation of {@link FrameDecoder} which enables implementation
 * of a non-blocking decoder in the blocking I/O paradigm.
 * <p>
 * The biggest difference between {@link ReplayingDecoder} and
 * {@link FrameDecoder} is that {@link ReplayingDecoder} allows you to
 * implement the {@code decode()} and {@code decodeLast()} methods just like
 * all required bytes were received already, rather than checking the
 * availability of the required bytes.  For example, the following
 * {@link FrameDecoder} implementation:
 * <pre>
 * public class IntegerHeaderFrameDecoder extends {@link FrameDecoder} {
 *
 *   {@code @Override}
 *   protected Object decode({@link ChannelHandlerContext} ctx,
 *                           {@link Channel} channel,
 *                           {@link ChannelBuffer} buf) throws Exception {
 *
 *     if (buf.readableBytes() &lt; 4) {
 *        return <strong>null</strong>;
 *     }
 *
 *     buf.markReaderIndex();
 *     int length = buf.readInt();
 *
 *     if (buf.readableBytes() &lt; length) {
 *        buf.resetReaderIndex();
 *        return <strong>null</strong>;
 *     }
 *
 *     return buf.readBytes(length);
 *   }
 * }
 * </pre>
 * is simplified like the following with {@link ReplayingDecoder}:
 * <pre>
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;{@link VoidEnum}&gt; {
 *
 *   protected Object decode({@link ChannelHandlerContext} ctx,
 *                           {@link Channel} channel,
 *                           {@link ChannelBuffer} buf,
 *                           {@link VoidEnum} state) throws Exception {
 *
 *     return buf.readBytes(buf.readInt());
 *   }
 * }
 * </pre>
 *
 * <h3>How does this work?</h3>
 * <p>
 * {@link ReplayingDecoder} passes a specialized {@link ChannelBuffer}
 * implementation which throws an {@link Error} of certain type when there's not
 * enough data in the buffer.  In the {@code IntegerHeaderFrameDecoder} above,
 * you just assumed that there will be 4 or more bytes in the buffer when
 * you call {@code buf.readInt()}.  If there's really 4 bytes in the buffer,
 * it will return the integer header as you expected.  Otherwise, the
 * {@link Error} will be raised and the control will be returned to
 * {@link ReplayingDecoder}.  If {@link ReplayingDecoder} catches the
 * {@link Error}, then it will rewind the {@code readerIndex} of the buffer
 * back to the 'initial' position (i.e. the beginning of the buffer) and call
 * the {@code decode(..)} method again when more data is received into the
 * buffer.
 * <p>
 * Please note that the overhead of throwing an {@link Error} is minimal unlike
 * throwing a new {@link Exception} in an ordinary way. {@link ReplayingDecoder}
 * reuses the same {@link Error} instance so that it does not need to fill its
 * stack trace, which takes most of {@link Exception} initialization time.
 *
 * <h3>Limitations</h3>
 * <p>
 * At the cost of the simplicity, {@link ReplayingDecoder} enforces you a few
 * limitations:
 * <ul>
 * <li>Some buffer operations are prohibited.</li>
 * <li>Performance can be worse if the network is slow and the message
 *     format is complicated unlike the example above.  In this case, your
 *     decoder might have to decode the same part of the message over and over
 *     again.</li>
 * <li>You must keep in mind that {@code decode(..)} method can be called many
 *     times to decode a single message.  For example, the following code will
 *     not work:
 * <pre> public class MyDecoder extends {@link ReplayingDecoder}&lt;{@link VoidEnum}&gt; {
 *
 *   private final Queue&lt;Integer&gt; values = new LinkedList&lt;Integer&gt;();
 *
 *   {@code @Override}
 *   public Object decode(.., {@link ChannelBuffer} buffer, ..) throws Exception {
 *
 *     // A message contains 2 integers.
 *     values.offer(buffer.readInt());
 *     values.offer(buffer.readInt());
 *
 *     // This assertion will fail intermittently since values.offer()
 *     // can be called more than two times!
 *     assert values.size() == 2;
 *     return values.poll() + values.poll();
 *   }
 * }</pre>
 *      The correct implementation looks like the following, and you can also
 *      utilize the 'checkpoint' feature which is explained in detail in the
 *      next section.
 * <pre> public class MyDecoder extends {@link ReplayingDecoder}&lt;{@link VoidEnum}&gt; {
 *
 *   private final Queue&lt;Integer&gt; values = new LinkedList&lt;Integer&gt;();
 *
 *   {@code @Override}
 *   public Object decode(.., {@link ChannelBuffer} buffer, ..) throws Exception {
 *
 *     // Revert the state of the variable that might have been changed
 *     // since the last partial decode.
 *     values.clear();
 *
 *     // A message contains 2 integers.
 *     values.offer(buffer.readInt());
 *     values.offer(buffer.readInt());
 *
 *     // Now we know this assertion will never fail.
 *     assert values.size() == 2;
 *     return values.poll() + values.poll();
 *   }
 * }</pre>
 *     </li>
 * </ul>
 *
 * <h3>Improving the performance</h3>
 * <p>
 * Fortunately, the performance of a complex decoder implementation can be
 * improved significantly with the {@code checkpoint()} method.  The
 * {@code checkpoint()} method updates the 'initial' position of the buffer so
 * that {@link ReplayingDecoder} rewinds the {@code readerIndex} of the buffer
 * to the last position where you called the {@code checkpoint()} method.
 *
 * <h4>Calling {@code checkpoint(T)} with an {@link Enum}</h4>
 * <p>
 * Although you can just use {@code checkpoint()} method and manage the state
 * of the decoder by yourself, the easiest way to manage the state of the
 * decoder is to create an {@link Enum} type which represents the current state
 * of the decoder and to call {@code checkpoint(T)} method whenever the state
 * changes.  You can have as many states as you want depending on the
 * complexity of the message you want to decode:
 *
 * <pre>
 * public enum MyDecoderState {
 *   READ_LENGTH,
 *   READ_CONTENT;
 * }
 *
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;<strong>MyDecoderState</strong>&gt; {
 *
 *   private int length;
 *
 *   public IntegerHeaderFrameDecoder() {
 *     // Set the initial state.
 *     <strong>super(MyDecoderState.READ_LENGTH);</strong>
 *   }
 *
 *   {@code @Override}
 *   protected Object decode({@link ChannelHandlerContext} ctx,
 *                           {@link Channel} channel,
 *                           {@link ChannelBuffer} buf,
 *                           <b>MyDecoderState</b> state) throws Exception {
 *     switch (state) {
 *     case READ_LENGTH:
 *       length = buf.readInt();
 *       <strong>checkpoint(MyDecoderState.READ_CONTENT);</strong>
 *     case READ_CONTENT:
 *       ChannelBuffer frame = buf.readBytes(length);
 *       <strong>checkpoint(MyDecoderState.READ_LENGTH);</strong>
 *       return frame;
 *     default:
 *       throw new Error("Shouldn't reach here.");
 *     }
 *   }
 * }
 * </pre>
 *
 * <h4>Calling {@code checkpoint()} with no parameter</h4>
 * <p>
 * An alternative way to manage the decoder state is to manage it by yourself.
 * <pre>
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;<strong>{@link VoidEnum}</strong>&gt; {
 *
 *   <strong>private boolean readLength;</strong>
 *   private int length;
 *
 *   {@code @Override}
 *   protected Object decode({@link ChannelHandlerContext} ctx,
 *                           {@link Channel} channel,
 *                           {@link ChannelBuffer} buf,
 *                           {@link VoidEnum} state) throws Exception {
 *     if (!readLength) {
 *       length = buf.readInt();
 *       <strong>readLength = true;</strong>
 *       <strong>checkpoint();</strong>
 *     }
 *
 *     if (readLength) {
 *       ChannelBuffer frame = buf.readBytes(length);
 *       <strong>readLength = false;</strong>
 *       <strong>checkpoint();</strong>
 *       return frame;
 *     }
 *   }
 * }
 * </pre>
 *
 * <h3>Replacing a decoder with another decoder in a pipeline</h3>
 * <p>
 * If you are going to write a protocol multiplexer, you will probably want to
 * replace a {@link ReplayingDecoder} (protocol detector) with another
 * {@link ReplayingDecoder} or {@link FrameDecoder} (actual protocol decoder).
 * It is not possible to achieve this simply by calling
 * {@link ChannelPipeline#replace(ChannelHandler, String, ChannelHandler)}, but
 * some additional steps are required:
 * <pre>
 * public class FirstDecoder extends {@link ReplayingDecoder}&lt;{@link VoidEnum}&gt; {
 *
 *     public FirstDecoder() {
 *         super(true); // Enable unfold
 *     }
 *
 *     {@code @Override}
 *     protected Object decode({@link ChannelHandlerContext} ctx,
 *                             {@link Channel} ch,
 *                             {@link ChannelBuffer} buf,
 *                             {@link VoidEnum} state) {
 *         ...
 *         // Decode the first message
 *         Object firstMessage = ...;
 *
 *         // Add the second decoder
 *         ctx.getPipeline().addLast("second", new SecondDecoder());
 *
 *         // Remove the first decoder (me)
 *         ctx.getPipeline().remove(this);
 *
 *         if (buf.readable()) {
 *             // Hand off the remaining data to the second decoder
 *             return new Object[] { firstMessage, buf.readBytes(<b>super.actualReadableBytes()</b>) };
 *         } else {
 *             // Nothing to hand off
 *             return firstMessage;
 *         }
 *     }
 * </pre>
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2188 $, $Date: 2010-02-19 18:00:00 +0900 (Fri, 19 Feb 2010) $
 *
 * @param <T>
 *        the state type; use {@link VoidEnum} if state management is unused
 *
 * @apiviz.landmark
 * @apiviz.has org.jboss.netty.handler.codec.replay.UnreplayableOperationException oneway - - throws
 */
public abstract class ReplayingDecoder<T extends Enum<T>>
        extends SimpleChannelUpstreamHandler {
	private static my.Debug DEBUG=new my.Debug(my.Debug.ReplayingDecoder);//我加上的


    private final AtomicReference<ChannelBuffer> cumulation =
        new AtomicReference<ChannelBuffer>();
    private final boolean unfold;
    private ReplayingDecoderBuffer replayable;
    private T state;
    private int checkpoint;

    /**
     * Creates a new instance with no initial state (i.e: {@code null}).
     */
    protected ReplayingDecoder() {
        this(null);
    }

    protected ReplayingDecoder(boolean unfold) {
        this(null, unfold);
    }

    /**
     * Creates a new instance with the specified initial state.
     */
    protected ReplayingDecoder(T initialState) {
        this(initialState, false);
    }

    protected ReplayingDecoder(T initialState, boolean unfold) {
        this.state = initialState;
        this.unfold = unfold;
    }

    /**
     * Stores the internal cumulative buffer's reader position.
     */
    protected void checkpoint() {
		try {//我加上的
		DEBUG.P(this,"checkpoint()");

        ChannelBuffer cumulation = this.cumulation.get();
        if (cumulation != null) {
            checkpoint = cumulation.readerIndex();
        } else {
            checkpoint = -1; // buffer not available (already cleaned up)
        }

		DEBUG.P("state="+state);
		DEBUG.P("checkpoint="+checkpoint);

		}finally{//我加上的
		DEBUG.P(0,this,"checkpoint()");
		}
    }

    /**
     * Stores the internal cumulative buffer's reader position and updates
     * the current decoder state.
     */
    protected void checkpoint(T state) {
		try {//我加上的
		DEBUG.P(this,"checkpoint(1)");
		DEBUG.P("state="+state);

        checkpoint();
        setState(state);

		}finally{//我加上的
		DEBUG.P(0,this,"checkpoint(1)");
		}
    }

    /**
     * Returns the current state of this decoder.
     * @return the current state of this decoder
     */
    protected T getState() {
        return state;
    }

    /**
     * Sets the current state of this decoder.
     * @return the old state of this decoder
     */
    protected T setState(T newState) {
        T oldState = state;
        state = newState;
        return oldState;
    }

    /**
     * Returns the actual number of readable bytes in the cumulative buffer
     * of this decoder.  You do not need to rely on this value to write a
     * decoder.  Use it only when necessary.
     */
    protected int actualReadableBytes() {
        ChannelBuffer buf = cumulation.get();
        if (buf == null) {
            return 0;
        }

        return buf.readableBytes();
    }

    /**
     * Decodes the received packets so far into a frame.
     *
     * @param ctx      the context of this handler
     * @param channel  the current channel
     * @param buffer   the cumulative buffer of received packets so far.
     *                 Note that the buffer might be empty, which means you
     *                 should not make an assumption that the buffer contains
     *                 at least one byte in your decoder implementation.
     * @param state    the current decoder state ({@code null} if unused)
     *
     * @return the decoded frame
     */
    protected abstract Object decode(ChannelHandlerContext ctx,
            Channel channel, ChannelBuffer buffer, T state) throws Exception;

    /**
     * Decodes the received data so far into a frame when the channel is
     * disconnected.
     *
     * @param ctx      the context of this handler
     * @param channel  the current channel
     * @param buffer   the cumulative buffer of received packets so far.
     *                 Note that the buffer might be empty, which means you
     *                 should not make an assumption that the buffer contains
     *                 at least one byte in your decoder implementation.
     * @param state    the current decoder state ({@code null} if unused)
     *
     * @return the decoded frame
     */
    protected Object decodeLast(
            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, T state) throws Exception {
        return decode(ctx, channel, buffer, state);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
		try {//我加上的
		DEBUG.P(this,"messageReceived(2)");
		DEBUG.P("e="+e);

        Object m = e.getMessage();
        if (!(m instanceof ChannelBuffer)) {
            ctx.sendUpstream(e);
            return;
        }

        ChannelBuffer input = (ChannelBuffer) m;

		DEBUG.P("input.readable()="+input.readable());
		//等于writerIndex - readerIndex
		DEBUG.P("input.readableBytes()="+input.readableBytes());

        if (!input.readable()) {
            return;
        }

        ChannelBuffer cumulation = cumulation(ctx);

		DEBUG.P("cumulation="+cumulation);

		//把0到readerIndex之间的字节废弃掉，
		//然后把writerIndex到readerIndex之间的的字节搬到最前面(从0索引开始存放)
        cumulation.discardReadBytes();

		DEBUG.P("cumulation="+cumulation);

        cumulation.writeBytes(input);

		DEBUG.P("cumulation="+cumulation);

        callDecode(ctx, e.getChannel(), cumulation, e.getRemoteAddress());

		}finally{//我加上的
		DEBUG.P(0,this,"messageReceived(2)");
		}
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
            ChannelStateEvent e) throws Exception {
        cleanup(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx,
            ChannelStateEvent e) throws Exception {
        cleanup(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        ctx.sendUpstream(e);
    }

    private void callDecode(ChannelHandlerContext context, Channel channel, ChannelBuffer cumulation, SocketAddress remoteAddress) throws Exception {
        try {//我加上的
		DEBUG.P(this,"callDecode(4)");
		DEBUG.P("remoteAddress="+remoteAddress);
		DEBUG.P("cumulation="+cumulation);
		DEBUG.P("cumulation.readable()="+cumulation.readable());
		DEBUG.P("state="+state); //HttpMessageDecoder初始化时默认是SKIP_CONTROL_CHARS

		while (cumulation.readable()) {
            int oldReaderIndex = checkpoint = cumulation.readerIndex();
            Object result = null;
            T oldState = state;
            try {
                result = decode(context, channel, replayable, state);
                
				DEBUG.P("result="+result);
				DEBUG.P(1);
				DEBUG.P("oldReaderIndex="+oldReaderIndex);
				DEBUG.P("cumulation.readerIndex()="+cumulation.readerIndex());
				DEBUG.P("oldState="+oldState);
				DEBUG.P("state="+state);

				DEBUG.P(1);

				if (result == null) {
                    if (oldReaderIndex == cumulation.readerIndex() && oldState == state) {
                        throw new IllegalStateException(
                                "null cannot be returned if no data is consumed and state didn't change.");
                    } else {
                        // Previous data has been discarded or caused state transition.
                        // Probably it is reading on.
                        continue;
                    }
                }
            } catch (ReplayError replay) {
				DEBUG.P("ReplayError replay="+replay);

                // Return to the checkpoint (or oldPosition) and retry.
                int checkpoint = this.checkpoint;
                if (checkpoint >= 0) {
                    cumulation.readerIndex(checkpoint);
                } else {
                    // Called by cleanup() - no need to maintain the readerIndex
                    // anymore because the buffer has been released already.
                }
            }

			DEBUG.P("result="+result);

            if (result == null) {
                // Seems like more data is required.
                // Let us wait for the next notification.
                break;
            }

            if (oldReaderIndex == cumulation.readerIndex() && oldState == state) {
                throw new IllegalStateException(
                        "decode() method must consume at least one byte " +
                        "if it returned a decoded message (caused by: " +
                        getClass() + ")");
            }

            // A successful decode
            unfoldAndfireMessageReceived(context, result, remoteAddress);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"callDecode(4)");
		}
    }

	//激活下一个ChannelHandler的messageReceived
    private void unfoldAndfireMessageReceived(
            ChannelHandlerContext context, Object result, SocketAddress remoteAddress) {
		try {//我加上的
		DEBUG.P(this,"unfoldAndfireMessageReceived(3)");
		DEBUG.P("unfold="+unfold);
		DEBUG.P("result="+result);
		if(result!=null) DEBUG.P("result.class="+result.getClass());

        if (unfold) {
            if (result instanceof Object[]) {
                for (Object r: (Object[]) result) {
                    Channels.fireMessageReceived(context, r, remoteAddress);
                }
            } else if (result instanceof Iterable<?>) {
                for (Object r: (Iterable<?>) result) {
                    Channels.fireMessageReceived(context, r, remoteAddress);
                }
            } else {
                Channels.fireMessageReceived(context, result, remoteAddress);
            }
        } else {
            Channels.fireMessageReceived(context, result, remoteAddress);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"unfoldAndfireMessageReceived(3)");
		}
    }

    private void cleanup(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
		try {//我加上的
		DEBUG.P(this,"cleanup(2)");

        try {
            ChannelBuffer cumulation = this.cumulation.getAndSet(null);
            if (cumulation == null) {
                return;
            }

            replayable.terminate();

			DEBUG.P("cumulation.readable()="+cumulation.readable());

            if (cumulation.readable()) {
                // Make sure all data was read before notifying a closed channel.
                callDecode(ctx, e.getChannel(), cumulation, null);
            }

            // Call decodeLast() finally.  Please note that decodeLast() is
            // called even if there's nothing more to read from the buffer to
            // notify a user that the connection was closed explicitly.
            Object partiallyDecoded = decodeLast(ctx, e.getChannel(), replayable, state);
            
			DEBUG.P("partiallyDecoded="+partiallyDecoded);
			if (partiallyDecoded != null) {
                unfoldAndfireMessageReceived(ctx, partiallyDecoded, null);
            }
        } catch (ReplayError replay) {
			DEBUG.P("replay="+replay);
            // Ignore
        } finally {
            ctx.sendUpstream(e);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"cleanup(2)");
		}
    }

    private ChannelBuffer cumulation(ChannelHandlerContext ctx) {
		try {//我加上的
		DEBUG.P(this,"cumulation(1)");
		
        ChannelBuffer buf = cumulation.get();

		DEBUG.P("buf="+buf);

        if (buf == null) {
            ChannelBufferFactory factory = ctx.getChannel().getConfig().getBufferFactory();

			DEBUG.P("factory="+factory);

            buf = new UnsafeDynamicChannelBuffer(factory);

			//buf即放在replayable中也放在cumulation中
            if (cumulation.compareAndSet(null, buf)) {
                replayable = new ReplayingDecoderBuffer(buf);
            } else {
                buf = cumulation.get();
            }
        }

		DEBUG.P("buf="+buf);
		DEBUG.P("replayable="+replayable);

        return buf;

		}finally{//我加上的
		DEBUG.P(0,this,"cumulation(1)");
		}
    }
}
