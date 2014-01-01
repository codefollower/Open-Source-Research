/*
 * Copyright 2010 Red Hat, Inc.
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
package org.jboss.netty.handler.codec.http;

import java.util.Queue;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.util.internal.LinkedTransferQueue;

/**
 * A combination of {@link HttpRequestEncoder} and {@link HttpResponseDecoder}
 * which enables easier client side HTTP implementation. {@link HttpClientCodec}
 * provides additional state management for <tt>HEAD</tt> and <tt>CONNECT</tt>
 * requests, which {@link HttpResponseDecoder} lacks.  Please refer to
 * {@link HttpResponseDecoder} to learn what additional state management needs
 * to be done for <tt>HEAD</tt> and <tt>CONNECT</tt> and why
 * {@link HttpResponseDecoder} can not handle it by itself.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2249 $, $Date: 2010-04-16 22:32:55 +0900 (Fri, 16 Apr 2010) $
 *
 * @see HttpServerCodec
 *
 * @apiviz.has org.jboss.netty.handler.codec.http.HttpResponseDecoder
 * @apiviz.has org.jboss.netty.handler.codec.http.HttpRequestEncoder
 */
public class HttpClientCodec implements ChannelUpstreamHandler,
        ChannelDownstreamHandler {
	private static my.Debug DEBUG=new my.Debug(my.Debug.http);//我加上的

    /** A queue that is used for correlating a request and a response. */
    final Queue<HttpMethod> queue = new LinkedTransferQueue<HttpMethod>();

    /** If true, decoding stops (i.e. pass-through) */
    volatile boolean done;

    private final HttpRequestEncoder encoder = new Encoder();
    private final HttpResponseDecoder decoder;

    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}, and
     * {@code maxChunkSize (8192)}).
     */
    public HttpClientCodec() {
        this(4096, 8192, 8192);
    }

    /**
     * Creates a new instance with the specified decoder options.
     */
    public HttpClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        decoder = new Decoder(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        decoder.handleUpstream(ctx, e);
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        encoder.handleDownstream(ctx, e);
    }

    private final class Encoder extends HttpRequestEncoder {

        Encoder() {
            super();
        }

        @Override
        protected Object encode(ChannelHandlerContext ctx, Channel channel,
                Object msg) throws Exception {
			try {//我加上的
			DEBUG.P(this,"encode(3)");
			DEBUG.P("msg="+msg);
			DEBUG.P("done="+done);
			DEBUG.P("queue="+queue);

            if (msg instanceof HttpRequest && !done) {
                queue.offer(((HttpRequest) msg).getMethod());
            }

			DEBUG.P("queue="+queue);

            return super.encode(ctx, channel, msg);

			}finally{//我加上的
			DEBUG.P(0,this,"encode(3)");
			}
        }
    }

    private final class Decoder extends HttpResponseDecoder {

        Decoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
            super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, Channel channel,
                ChannelBuffer buffer, State state) throws Exception {
			try {//我加上的
			DEBUG.P(this,"decode(4)");
			DEBUG.P("state="+state);
			DEBUG.P("done="+done);

            if (done) {
                return buffer.readBytes(actualReadableBytes());
            } else {
                return super.decode(ctx, channel, buffer, state);
            }

			}finally{//我加上的
			DEBUG.P(0,this,"decode(4)");
			}
        }

        @Override
        protected boolean isContentAlwaysEmpty(HttpMessage msg) {
			try {//我加上的
			DEBUG.P(this,"isContentAlwaysEmpty(1)");
			DEBUG.P("msg="+msg);

            // Get the method of the HTTP request that corresponds to the
            // current response.
            HttpMethod method = queue.poll();

            char firstChar = method.getName().charAt(0);

			DEBUG.P("firstChar="+firstChar);

            switch (firstChar) {
            case 'H':
                // According to 4.3, RFC2616:
                // All responses to the HEAD request method MUST NOT include a
                // message-body, even though the presence of entity-header fields
                // might lead one to believe they do.
                if (HttpMethod.HEAD.equals(method)) {
                    // Interesting edge case:
                    // Zero-byte chunk will appear if Transfer-Encoding of the
                    // response is 'chunked'.  This is probably because of the
                    // trailing headers.
                    return !msg.isChunked();
                }
                break;
            case 'C':
                // Successful CONNECT request results in a response with empty body.
                if (((HttpResponse) msg).getStatus().getCode() == 200) {
                    if (HttpMethod.CONNECT.equals(method)) {
                        // Proxy connection established - Not HTTP anymore.
                        done = true;
                        queue.clear();
                        return true;
                    }
                }
                break;
            }

            return super.isContentAlwaysEmpty(msg);

			}finally{//我加上的
			DEBUG.P(0,this,"isContentAlwaysEmpty(1)");
			}
        }
    }
}
