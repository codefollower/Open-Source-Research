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
package com.codefollower.douyu.http;

import static com.codefollower.douyu.http.HttpCodecUtil.*;
import static com.codefollower.douyu.netty.buffer.ChannelBuffers.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.codefollower.douyu.netty.buffer.ChannelBuffer;
import com.codefollower.douyu.netty.buffer.ChannelBuffers;
import com.codefollower.douyu.netty.channel.Channel;
import com.codefollower.douyu.netty.channel.ChannelHandlerContext;
import com.codefollower.douyu.netty.handler.codec.oneone.OneToOneEncoder;
import com.codefollower.douyu.netty.util.CharsetUtil;

/**
 * Encodes an {@link HttpMessage} or an {@link HttpChunk} into
 * a {@link ChannelBuffer}.
 *
 * <h3>Extensibility</h3>
 *
 * Please note that this encoder is designed to be extended to implement
 * a protocol derived from HTTP, such as
 * <a href="http://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol">RTSP</a> and
 * <a href="http://en.wikipedia.org/wiki/Internet_Content_Adaptation_Protocol">ICAP</a>.
 * To implement the encoder of such a derived protocol, extend this class and
 * implement all abstract methods properly.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev$, $Date$
 *
 * @apiviz.landmark
 */
public abstract class HttpMessageEncoder extends OneToOneEncoder {

    private static final ChannelBuffer LAST_CHUNK =
        copiedBuffer("0\r\n\r\n", CharsetUtil.US_ASCII);

    private volatile boolean chunked;

    /**
     * Creates a new instance.
     */
    protected HttpMessageEncoder() {
        super();
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof HttpMessage) {
            HttpMessage m = (HttpMessage) msg;
            boolean chunked = this.chunked = HttpCodecUtil.isTransferEncodingChunked(m);
            ChannelBuffer header = ChannelBuffers.dynamicBuffer(
                    channel.getConfig().getBufferFactory());
            encodeInitialLine(header, m);
            encodeHeaders(header, m);
            header.writeByte(CR);
            header.writeByte(LF);

            ChannelBuffer content = m.getContent();
            //System.out.println(new String(header.array()));
            if (!content.readable()) {
                return header; // no content
            } else if (chunked) {
                throw new IllegalArgumentException(
                        "HttpMessage.content must be empty " +
                        "if Transfer-Encoding is chunked.");
            } else {
            	//System.out.println(new String(content.array()));
                return wrappedBuffer(header, content);
            }
        }

        if (msg instanceof HttpChunk) {
            HttpChunk chunk = (HttpChunk) msg;
            if (chunked) {
                if (chunk.isLast()) {
                    chunked = false;
                    if (chunk instanceof HttpChunkTrailer) {
                        ChannelBuffer trailer = ChannelBuffers.dynamicBuffer(
                                channel.getConfig().getBufferFactory());
                        trailer.writeByte((byte) '0');
                        trailer.writeByte(CR);
                        trailer.writeByte(LF);
                        encodeTrailingHeaders(trailer, (HttpChunkTrailer) chunk);
                        trailer.writeByte(CR);
                        trailer.writeByte(LF);
                        return trailer;
                    } else {
                        return LAST_CHUNK.duplicate();
                    }
                } else {
                    ChannelBuffer content = chunk.getContent();
                    int contentLength = content.readableBytes();

                    return wrappedBuffer(
                            copiedBuffer(
                                    Integer.toHexString(contentLength),
                                    CharsetUtil.US_ASCII),
                            wrappedBuffer(CRLF),
                            content.slice(content.readerIndex(), contentLength),
                            wrappedBuffer(CRLF));
                }
            } else {
                if (chunk.isLast()) {
                    return null;
                } else {
                    return chunk.getContent();
                }
            }

        }

        // Unknown message type.
        return msg;
    }

    private void encodeHeaders(ChannelBuffer buf, HttpMessage message) {
        try {
            for (Map.Entry<String, String> h: message.getHeaders()) {
                encodeHeader(buf, h.getKey(), h.getValue());
            }
        } catch (UnsupportedEncodingException e) {
            throw (Error) new Error().initCause(e);
        }
    }

    private void encodeTrailingHeaders(ChannelBuffer buf, HttpChunkTrailer trailer) {
        try {
            for (Map.Entry<String, String> h: trailer.getHeaders()) {
                encodeHeader(buf, h.getKey(), h.getValue());
            }
        } catch (UnsupportedEncodingException e) {
            throw (Error) new Error().initCause(e);
        }
    }

    private void encodeHeader(ChannelBuffer buf, String header, String value)
            throws UnsupportedEncodingException {
        buf.writeBytes(header.getBytes("ASCII"));
        buf.writeByte(COLON);
        buf.writeByte(SP);
        buf.writeBytes(value.getBytes("ASCII"));
        buf.writeByte(CR);
        buf.writeByte(LF);
    }

    protected abstract void encodeInitialLine(ChannelBuffer buf, HttpMessage message) throws Exception;
}
