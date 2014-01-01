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
package org.jboss.netty.channel;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.util.internal.ConcurrentHashMap;

/**
 * A skeletal {@link Channel} implementation.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2280 $, $Date: 2010-05-19 15:29:43 +0900 (Wed, 19 May 2010) $
 *
 */
public abstract class AbstractChannel implements Channel {
	private static my.Debug DEBUG=new my.Debug(my.Debug.AbstractChannel);//我加上的

    static final ConcurrentMap<Integer, Channel> allChannels = new ConcurrentHashMap<Integer, Channel>();
    private static final IdDeallocator ID_DEALLOCATOR = new IdDeallocator();

    private static Integer allocateId(Channel channel) {
        Integer id = Integer.valueOf(System.identityHashCode(channel));
        for (;;) {
            // Loop until a unique ID is acquired.
            // It should be found in one loop practically.
            if (allChannels.putIfAbsent(id, channel) == null) {
                // Successfully acquired.
                return id;
            } else {
                // Taken by other channel at almost the same moment.
                id = Integer.valueOf(id.intValue() + 1);
            }
        }
    }

	//io操作完成后删除id对应的Channel
    private static final class IdDeallocator implements ChannelFutureListener {
        IdDeallocator() {
            super();
        }

        public void operationComplete(ChannelFuture future) throws Exception {
            allChannels.remove(future.getChannel().getId());
        }
    }

    private final Integer id;
    private final Channel parent;
    private final ChannelFactory factory;
    private final ChannelPipeline pipeline;
    private final ChannelFuture succeededFuture = new SucceededChannelFuture(this);
    private final ChannelCloseFuture closeFuture = new ChannelCloseFuture();
    private volatile int interestOps = OP_READ;

    /** Cache for the string representation of this channel */
    private String strVal;

    /**
     * Creates a new instance.
     *
     * @param parent
     *        the parent of this channel. {@code null} if there's no parent.
     * @param factory
     *        the factory which created this channel
     * @param pipeline
     *        the pipeline which is going to be attached to this channel
     * @param sink
     *        the sink which will receive downstream events from the pipeline
     *        and send upstream events to the pipeline
     */
    protected AbstractChannel(
            Channel parent, ChannelFactory factory,
            ChannelPipeline pipeline, ChannelSink sink) {
		try {//我加上的
		DEBUG.P(this,"AbstractChannel(4)");
		DEBUG.P("parent="+parent);
		DEBUG.P("factory="+factory);
		DEBUG.P("pipeline="+pipeline);
		DEBUG.P("sink="+sink);

        this.parent = parent;
        this.factory = factory;
        this.pipeline = pipeline;

        id = allocateId(this);

		DEBUG.P("id="+id);

        closeFuture.addListener(ID_DEALLOCATOR);

        pipeline.attach(this, sink);

		}finally{//我加上的
		DEBUG.P(0,this,"AbstractChannel(4)");
		}
    }

    /**
     * (Internal use only) Creates a new temporary instance with the specified
     * ID.
     *
     * @param parent
     *        the parent of this channel. {@code null} if there's no parent.
     * @param factory
     *        the factory which created this channel
     * @param pipeline
     *        the pipeline which is going to be attached to this channel
     * @param sink
     *        the sink which will receive downstream events from the pipeline
     *        and send upstream events to the pipeline
     */
    protected AbstractChannel(
            Integer id,
            Channel parent, ChannelFactory factory,
            ChannelPipeline pipeline, ChannelSink sink) {

        this.id = id;
        this.parent = parent;
        this.factory = factory;
        this.pipeline = pipeline;
        pipeline.attach(this, sink);
    }

    public final Integer getId() {
        return id;
    }

    public Channel getParent() {
        return parent;
    }

    public ChannelFactory getFactory() {
        return factory;
    }

    public ChannelPipeline getPipeline() {
        return pipeline;
    }

    /**
     * Returns the cached {@link SucceededChannelFuture} instance.
     */
    protected ChannelFuture getSucceededFuture() {
        return succeededFuture;
    }

    /**
     * Returns the {@link FailedChannelFuture} whose cause is an
     * {@link UnsupportedOperationException}.
     */
    protected ChannelFuture getUnsupportedOperationFuture() {
        return new FailedChannelFuture(this, new UnsupportedOperationException());
    }

    /**
     * Returns the {@linkplain System#identityHashCode(Object) identity hash code}
     * of this channel.
     */
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Returns {@code true} if and only if the specified object is identical
     * with this channel (i.e: {@code this == o}).
     */
    @Override
    public final boolean equals(Object o) {
        return this == o;
    }

    /**
     * Compares the {@linkplain #getId() ID} of the two channels.
     */
    public final int compareTo(Channel o) {
        return getId().compareTo(o.getId());
    }

    public boolean isOpen() {
        return !closeFuture.isDone();
    }

    /**
     * Marks this channel as closed.  This method is intended to be called by
     * an internal component - please do not call it unless you know what you
     * are doing.
     *
     * @return {@code true} if and only if this channel was not marked as
     *                      closed yet
     */
    protected boolean setClosed() {
        return closeFuture.setClosed();
    }

    public ChannelFuture bind(SocketAddress localAddress) {
		try {//我加上的
		DEBUG.P(this,"bind(1)");
		DEBUG.P("localAddress="+localAddress);

        return Channels.bind(this, localAddress);

		}finally{//我加上的
		DEBUG.P(0,this,"bind(1)");
		}
    }

    public ChannelFuture unbind() {
        return Channels.unbind(this);
    }

    public ChannelFuture close() {
		try {//我加上的
		DEBUG.P(this,"close()");

        ChannelFuture returnedCloseFuture = Channels.close(this);

		//加java -enableassertions运行时才启用断言
        assert closeFuture == returnedCloseFuture;
        return closeFuture;

		}finally{//我加上的
		DEBUG.P(0,this,"close()");
		}
    }

    public ChannelFuture getCloseFuture() {
        return closeFuture;
    }

    public ChannelFuture connect(SocketAddress remoteAddress) {
		try {//我加上的
		DEBUG.P(this,"connect(1)");
		DEBUG.P("remoteAddress="+remoteAddress);

        return Channels.connect(this, remoteAddress);

		}finally{//我加上的
		DEBUG.P(0,this,"connect(1)");
		}
    }

    public ChannelFuture disconnect() {
        return Channels.disconnect(this);
    }

    public int getInterestOps() {
        return interestOps;
    }

    public ChannelFuture setInterestOps(int interestOps) {
        return Channels.setInterestOps(this, interestOps);
    }

    /**
     * Sets the {@link #getInterestOps() interestOps} property of this channel
     * immediately.  This method is intended to be called by an internal
     * component - please do not call it unless you know what you are doing.
     */
    protected void setInterestOpsNow(int interestOps) {
        this.interestOps = interestOps;
    }

    public boolean isReadable() {
        return (getInterestOps() & OP_READ) != 0;
    }

    public boolean isWritable() {
        return (getInterestOps() & OP_WRITE) == 0;
    }

    public ChannelFuture setReadable(boolean readable) {
        if (readable) {
            return setInterestOps(getInterestOps() | OP_READ);
        } else {
            return setInterestOps(getInterestOps() & ~OP_READ);
        }
    }

    public ChannelFuture write(Object message) {
		try {//我加上的
		DEBUG.P(this,"write(1)");
		DEBUG.P("message="+message);

        return Channels.write(this, message);

		}finally{//我加上的
		DEBUG.P(0,this,"write(1)");
		}
    }

    public ChannelFuture write(Object message, SocketAddress remoteAddress) {
        return Channels.write(this, message, remoteAddress);
    }

    /**
     * Returns the {@link String} representation of this channel.  The returned
     * string contains the {@linkplain #getId() ID}, {@linkplain #getLocalAddress() local address},
     * and {@linkplain #getRemoteAddress() remote address} of this channel for
     * easier identification.
     */
    @Override
    public String toString() {
        boolean connected = isConnected();
        if (connected && strVal != null) {
            return strVal;
        }

        StringBuilder buf = new StringBuilder(128);
		buf.append(this.getClass().getSimpleName()); //我加上的
        buf.append("[id: 0x");
        buf.append(getIdString());

        SocketAddress localAddress = getLocalAddress();
        SocketAddress remoteAddress = getRemoteAddress();

		//ServerSocket的remoteAddress是null

		//ServerSocket.accept得到的Socket的localAddress是ServerSocket的localAddress
		//ServerSocket.accept得到的Socket的remoteAddress是普通Socket的localAddress


		//DEBUG.P("localAddress="+localAddress);
		//DEBUG.P("remoteAddress="+remoteAddress);

		//普通Socket或ServerSocket.accept得到的Socket
        if (remoteAddress != null) {
            buf.append(", ");
            if (getParent() == null) { //普通Socket
				//如: [id: 0x00dc840f, /127.0.0.1:1052 => localhost/127.0.0.1:8080]
                buf.append(localAddress);
                buf.append(" => ");
                buf.append(remoteAddress);
            } else { //ServerSocket.accept得到的Socket
				//如: [id: 0x01a5ab41, /127.0.0.1:1052 => /127.0.0.1:8080]
                buf.append(remoteAddress);
                buf.append(" => ");
                buf.append(localAddress);
            }
        } else if (localAddress != null) { //ServerSocket
		    //如: [id: 0x01100d7a, /0.0.0.0:8080]
            buf.append(", ");
            buf.append(localAddress);
        }

        buf.append(']');

        String strVal = buf.toString();
        if (connected) {
            this.strVal = strVal;
        } else {
            this.strVal = null;
        }
        return strVal;
    }

    private String getIdString() {
        String answer = Integer.toHexString(id.intValue());
        switch (answer.length()) {
        case 0:
            answer = "00000000";
            break;
        case 1:
            answer = "0000000" + answer;
            break;
        case 2:
            answer = "000000" + answer;
            break;
        case 3:
            answer = "00000" + answer;
            break;
        case 4:
            answer = "0000" + answer;
            break;
        case 5:
            answer = "000" + answer;
            break;
        case 6:
            answer = "00" + answer;
            break;
        case 7:
            answer = "0" + answer;
            break;
        }
        return answer;
    }

    private final class ChannelCloseFuture extends DefaultChannelFuture {

        public ChannelCloseFuture() {
            super(AbstractChannel.this, false);
        }

        @Override
        public boolean setSuccess() {
            // User is not supposed to call this method - ignore silently.
            return false;
        }

        @Override
        public boolean setFailure(Throwable cause) {
            // User is not supposed to call this method - ignore silently.
            return false;
        }

        boolean setClosed() {
            return super.setSuccess();
        }
    }
}
