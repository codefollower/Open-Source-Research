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
package com.codefollower.douyu.netty.handler.codec.embedder;

import static com.codefollower.douyu.netty.channel.Channels.*;

import com.codefollower.douyu.netty.buffer.ChannelBuffer;
import com.codefollower.douyu.netty.buffer.ChannelBufferFactory;
import com.codefollower.douyu.netty.channel.ChannelDownstreamHandler;
import com.codefollower.douyu.netty.channel.ChannelPipeline;
import com.codefollower.douyu.netty.handler.codec.base64.Base64Encoder;
import com.codefollower.douyu.netty.handler.codec.string.StringEncoder;
import com.codefollower.douyu.netty.util.CharsetUtil;

/**
 * A helper that wraps an encoder so that it can be used without doing actual
 * I/O in unit tests or higher level codecs.  For example, you can encode a
 * {@link String} into a Base64-encoded {@link ChannelBuffer} with
 * {@link Base64Encoder} and {@link StringEncoder} without setting up the
 * {@link ChannelPipeline} and other mock objects by yourself:
 * <pre>
 * String data = "foobar";
 *
 * {@link EncoderEmbedder}&lt;{@link ChannelBuffer}&gt; embedder = new {@link EncoderEmbedder}&lt;{@link ChannelBuffer}&gt;(
 *         new {@link Base64Encoder}(), new {@link StringEncoder}());
 *
 * embedder.offer(data);
 *
 * {@link ChannelBuffer} encoded = embedder.poll();
 * assert encoded.toString({@link CharsetUtil}.US_ASCII).equals("Zm9vYmFy");
 * </pre>
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev$, $Date$
 *
 * @apiviz.landmark
 * @see DecoderEmbedder
 */
public class EncoderEmbedder<E> extends AbstractCodecEmbedder<E> {

    /**
     * Creates a new embedder whose pipeline is composed of the specified
     * handlers.
     */
    public EncoderEmbedder(ChannelDownstreamHandler... handlers) {
        super(handlers);
    }

    /**
     * Creates a new embedder whose pipeline is composed of the specified
     * handlers.
     *
     * @param bufferFactory the {@link ChannelBufferFactory} to be used when
     *                      creating a new buffer.
     */
    public EncoderEmbedder(ChannelBufferFactory bufferFactory, ChannelDownstreamHandler... handlers) {
        super(bufferFactory, handlers);
    }

    @Override
    public boolean offer(Object input) {
        write(getChannel(), input).setSuccess();
        return !isEmpty();
    }
}
