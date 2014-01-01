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
package org.jboss.netty.handler.codec.http;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

/**
 * Decodes {@link ChannelBuffer}s into {@link HttpMessage}s and
 * {@link HttpChunk}s.
 *
 * <h3>Parameters that prevents excessive memory consumption</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>The maximum length of the initial line
 *     (e.g. {@code "GET / HTTP/1.0"} or {@code "HTTP/1.0 200 OK"})
 *     If the length of the initial line exceeds this value, a
 *     {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>The maximum length of all headers.  If the sum of the length of each
 *     header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxChunkSize}</td>
 * <td>The maximum length of the content or each chunk.  If the content length
 *     (or the length of each chunk) exceeds this value, the content or chunk
 *     will be split into multiple {@link HttpChunk}s whose length is
 *     {@code maxChunkSize} at maximum.</td>
 * </tr>
 * </table>
 *
 * <h3>Chunked Content</h3>
 *
 * If the content of an HTTP message is greater than {@code maxChunkSize} or
 * the transfer encoding of the HTTP message is 'chunked', this decoder
 * generates one {@link HttpMessage} instance and its following
 * {@link HttpChunk}s per single HTTP message to avoid excessive memory
 * consumption. For example, the following HTTP message:
 * <pre>
 * GET / HTTP/1.1
 * Transfer-Encoding: chunked
 *
 * 1a
 * abcdefghijklmnopqrstuvwxyz
 * 10
 * 1234567890abcdef
 * 0
 * Content-MD5: ...
 * <i>[blank line]</i>
 * </pre>
 * triggers {@link HttpRequestDecoder} to generate 4 objects:
 * <ol>
 * <li>An {@link HttpRequest} whose {@link HttpMessage#isChunked() chunked}
 *     property is {@code true},</li>
 * <li>The first {@link HttpChunk} whose content is {@code 'abcdefghijklmnopqrstuvwxyz'},</li>
 * <li>The second {@link HttpChunk} whose content is {@code '1234567890abcdef'}, and</li>
 * <li>An {@link HttpChunkTrailer} which marks the end of the content.</li>
 * </ol>
 *
 * If you prefer not to handle {@link HttpChunk}s by yourself for your
 * convenience, insert {@link HttpChunkAggregator} after this decoder in the
 * {@link ChannelPipeline}.  However, please note that your server might not
 * be as memory efficient as without the aggregator.
 *
 * <h3>Extensibility</h3>
 *
 * Please note that this decoder is designed to be extended to implement
 * a protocol derived from HTTP, such as
 * <a href="http://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol">RTSP</a> and
 * <a href="http://en.wikipedia.org/wiki/Internet_Content_Adaptation_Protocol">ICAP</a>.
 * To implement the decoder of such a derived protocol, extend this class and
 * implement all abstract methods properly.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2287 $, $Date: 2010-05-27 21:38:54 +0900 (Thu, 27 May 2010) $
 *
 * @apiviz.landmark
 */
public abstract class HttpMessageDecoder extends ReplayingDecoder<HttpMessageDecoder.State> {
	private static my.Debug DEBUG=new my.Debug(my.Debug.http);//我加上的

    private final int maxInitialLineLength;
    private final int maxHeaderSize;
    private final int maxChunkSize;
    private HttpMessage message;
    private ChannelBuffer content;
    private long chunkSize;
    private int headerSize;

    /**
     * The internal state of {@link HttpMessageDecoder}.
     * <em>Internal use only</em>.
     *
     * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
     * @author <a href="http://gleamynode.net/">Trustin Lee</a>
     * @version $Rev: 2287 $, $Date: 2010-05-27 21:38:54 +0900 (Thu, 27 May 2010) $
     *
     * @apiviz.exclude
     */
    protected static enum State {
        SKIP_CONTROL_CHARS,
        READ_INITIAL,
        READ_HEADER,
        READ_VARIABLE_LENGTH_CONTENT,
        READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS,
        READ_FIXED_LENGTH_CONTENT,
        READ_FIXED_LENGTH_CONTENT_AS_CHUNKS,
        READ_CHUNK_SIZE,
        READ_CHUNKED_CONTENT,
        READ_CHUNKED_CONTENT_AS_CHUNKS,
        READ_CHUNK_DELIMITER,
        READ_CHUNK_FOOTER;
    }

    /**
     * Creates a new instance with the default
     * {@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}, and
     * {@code maxChunkSize (8192)}.
     */
    protected HttpMessageDecoder() {
        this(4096, 8192, 8192);
    }

    /**
     * Creates a new instance with the specified parameters.
     */
    protected HttpMessageDecoder(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {

        super(State.SKIP_CONTROL_CHARS, true);

        if (maxInitialLineLength <= 0) {
            throw new IllegalArgumentException(
                    "maxInitialLineLength must be a positive integer: " +
                    maxInitialLineLength);
        }
        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException(
                    "maxHeaderSize must be a positive integer: " +
                    maxChunkSize);
        }
        if (maxChunkSize < 0) {
            throw new IllegalArgumentException(
                    "maxChunkSize must be a positive integer: " +
                    maxChunkSize);
        }
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxHeaderSize = maxHeaderSize;
        this.maxChunkSize = maxChunkSize;
    }

	//这里的ChannelBuffer buffer是ReplayingDecoder.replayable, 
	//而不是ReplayingDecoder.cumulation，
	//ReplayingDecoder.cumulation包含在ReplayingDecoder.replayable中
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, State state) throws Exception {
        try {//我加上的
		DEBUG.P(this,"decode(4)");
		DEBUG.P("buffer="+buffer);
		new Exception().printStackTrace();
		DEBUG.P("state="+state);

		switch (state) {
		//如果state是SKIP_CONTROL_CHARS,那么处理完这个case后会直接转到case READ_INITIAL的代码
        case SKIP_CONTROL_CHARS: {
            try {
                skipControlCharacters(buffer);

				//这一句不能省，因为在skipControlCharacters时可以抛出ReplayError，
				//此时会转到finally，前面读过的字节不用再重复读了，
				//当不抛出ReplayError时，再把state转变一下，不过会调用两次checkpoint
                checkpoint(State.READ_INITIAL);
            } finally {
                checkpoint();
            }
        }
        case READ_INITIAL: {
            String[] initialLine = splitInitialLine(readLine(buffer, maxInitialLineLength));
            
			DEBUG.PA("initialLine",initialLine);

			if (initialLine.length < 3) {
                // Invalid initial line - ignore.
                checkpoint(State.SKIP_CONTROL_CHARS);
                return null; //返回null时会重新执行decode方法
            }

			//如果是客户端则建立DefaultHttpResponse
			//如果是服务器端则建立DefaultHttpRequest
			//因为HttpMessageDecoder的子类总是在排在ChannelHandler链之前，
			//当前程序实例如果是服务器端，那么它必需解码进来的请求信息，
			//然后构造一个DefaultHttpRequest消息，最后再转给后续的ChannelHandler处理。
			//如果当前程序实例是客户端，那么它必需解码从服务器端发回的响应信息，
			//然后构造一个DefaultHttpResponse消息，最后再转给后续的ChannelHandler处理。
            message = createMessage(initialLine);
            checkpoint(State.READ_HEADER);
        }
        case READ_HEADER: {
            State nextState = readHeaders(buffer);
            checkpoint(nextState);
            if (nextState == State.READ_CHUNK_SIZE) {
                // Chunked encoding
                message.setChunked(true);
                // Generate HttpMessage first.  HttpChunks will follow.
                return message;
            } else if (nextState == State.SKIP_CONTROL_CHARS) {
                // No content is expected.
                // Remove the headers which are not supposed to be present not
                // to confuse subsequent handlers.
                message.removeHeader(HttpHeaders.Names.TRANSFER_ENCODING);
                return message;
            } else {
                long contentLength = HttpHeaders.getContentLength(message, -1);

				DEBUG.P("contentLength="+contentLength);
				DEBUG.P("isDecodingRequest()="+isDecodingRequest());

                if (contentLength == 0 || contentLength == -1 && isDecodingRequest()) {
                    content = ChannelBuffers.EMPTY_BUFFER;
                    return reset();
                }

                switch (nextState) {
                case READ_FIXED_LENGTH_CONTENT:
                    if (contentLength > maxChunkSize) {
                        // Generate HttpMessage first.  HttpChunks will follow.
                        checkpoint(State.READ_FIXED_LENGTH_CONTENT_AS_CHUNKS);
                        message.setChunked(true);
                        // chunkSize will be decreased as the READ_FIXED_LENGTH_CONTENT_AS_CHUNKS
                        // state reads data chunk by chunk.
                        chunkSize = HttpHeaders.getContentLength(message, -1);
                        return message;
                    }
                    break;
                case READ_VARIABLE_LENGTH_CONTENT:
                    if (buffer.readableBytes() > maxChunkSize) {
                        // Generate HttpMessage first.  HttpChunks will follow.
                        checkpoint(State.READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS);
                        message.setChunked(true);
                        return message;
                    }
                    break;
                }
            }
            // We return null here, this forces decode to be called again where we will decode the content
            return null;
        }
        case READ_VARIABLE_LENGTH_CONTENT: {
            if (content == null) {
                content = ChannelBuffers.dynamicBuffer(channel.getConfig().getBufferFactory());
            }
            //this will cause a replay error until the channel is closed where this will read what's left in the buffer
            content.writeBytes(buffer.readBytes(buffer.readableBytes()));
            return reset();
        }
        case READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS: {
            // Keep reading data as a chunk until the end of connection is reached.
            int chunkSize = Math.min(maxChunkSize, buffer.readableBytes());
            HttpChunk chunk = new DefaultHttpChunk(buffer.readBytes(chunkSize));

            if (!buffer.readable()) {
                // Reached to the end of the connection.
                reset();
                if (!chunk.isLast()) {
                    // Append the last chunk.
                    return new Object[] { chunk, HttpChunk.LAST_CHUNK };
                }
            }
            return chunk;
        }
        case READ_FIXED_LENGTH_CONTENT: {
            //we have a content-length so we just read the correct number of bytes
            readFixedLengthContent(buffer);
            return reset();
        }
        case READ_FIXED_LENGTH_CONTENT_AS_CHUNKS: {
            long chunkSize = this.chunkSize;
            HttpChunk chunk;
            if (chunkSize > maxChunkSize) {
                chunk = new DefaultHttpChunk(buffer.readBytes(maxChunkSize));
                chunkSize -= maxChunkSize;
            } else {
                assert chunkSize <= Integer.MAX_VALUE;
                chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
                chunkSize = 0;
            }
            this.chunkSize = chunkSize;

            if (chunkSize == 0) {
                // Read all content.
                reset();
                if (!chunk.isLast()) {
                    // Append the last chunk.
                    return new Object[] { chunk, HttpChunk.LAST_CHUNK };
                }
            }
            return chunk;
        }
        /**
         * everything else after this point takes care of reading chunked content. basically, read chunk size,
         * read chunk, read and ignore the CRLF and repeat until 0
         */
        case READ_CHUNK_SIZE: {
            String line = readLine(buffer, maxInitialLineLength);
            int chunkSize = getChunkSize(line);
            this.chunkSize = chunkSize;
            if (chunkSize == 0) {
                checkpoint(State.READ_CHUNK_FOOTER);
                return null;
            } else if (chunkSize > maxChunkSize) {
                // A chunk is too large. Split them into multiple chunks again.
                checkpoint(State.READ_CHUNKED_CONTENT_AS_CHUNKS);
            } else {
                checkpoint(State.READ_CHUNKED_CONTENT);
            }
        }
        case READ_CHUNKED_CONTENT: {
            assert chunkSize <= Integer.MAX_VALUE;
            HttpChunk chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
            checkpoint(State.READ_CHUNK_DELIMITER);
            return chunk;
        }
        case READ_CHUNKED_CONTENT_AS_CHUNKS: {
            long chunkSize = this.chunkSize;
            HttpChunk chunk;
            if (chunkSize > maxChunkSize) {
                chunk = new DefaultHttpChunk(buffer.readBytes(maxChunkSize));
                chunkSize -= maxChunkSize;
            } else {
                assert chunkSize <= Integer.MAX_VALUE;
                chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
                chunkSize = 0;
            }
            this.chunkSize = chunkSize;

            if (chunkSize == 0) {
                // Read all content.
                checkpoint(State.READ_CHUNK_DELIMITER);
            }

            if (!chunk.isLast()) {
                return chunk;
            }
        }
        case READ_CHUNK_DELIMITER: {
            for (;;) {
                byte next = buffer.readByte();
                if (next == HttpCodecUtil.CR) {
                    if (buffer.readByte() == HttpCodecUtil.LF) {
                        checkpoint(State.READ_CHUNK_SIZE);
                        return null;
                    }
                } else if (next == HttpCodecUtil.LF) {
                    checkpoint(State.READ_CHUNK_SIZE);
                    return null;
                }
            }
        }
        case READ_CHUNK_FOOTER: {
            HttpChunkTrailer trailer = readTrailingHeaders(buffer);
            if (maxChunkSize == 0) {
                // Chunked encoding disabled.
                return reset();
            } else {
                reset();
                // The last chunk, which is empty
                return trailer;
            }
        }
        default: {
            throw new Error("Shouldn't reach here.");
        }

        }

		}finally{//我加上的
		DEBUG.P(0,this,"decode(4)");
		}
    }

    protected boolean isContentAlwaysEmpty(HttpMessage msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse res = (HttpResponse) msg;
            int code = res.getStatus().getCode();
            if (code < 200) {
                return true;
            }
            switch (code) {
            case 204: case 205: case 304:
                return true;
            }
        }
        return false;
    }

    private Object reset() {
		try {//我加上的
		DEBUG.P(this,"reset()");
		DEBUG.P("message="+message);
		DEBUG.P("content="+content);

        HttpMessage message = this.message;
        ChannelBuffer content = this.content;

        if (content != null) {
            message.setContent(content);
            this.content = null;
        }
        this.message = null;

        checkpoint(State.SKIP_CONTROL_CHARS);

		DEBUG.P("message="+message);
        return message;

		}finally{//我加上的
		DEBUG.P(0,this,"reset()");
		}
    }

	//执行完这个方法之后与buffer.readerIndex()这个位置对应的字符是一个非控制字符、非空白字符
    private void skipControlCharacters(ChannelBuffer buffer) {
		try {//我加上的
		DEBUG.P(this,"skipControlCharacters(1)");
		DEBUG.P("buffer="+buffer);

        for (;;) {
            char c = (char) buffer.readUnsignedByte();
            if (!Character.isISOControl(c) &&
                !Character.isWhitespace(c)) {
                buffer.readerIndex(buffer.readerIndex() - 1);
                break;
            }
        }

		}finally{//我加上的
		DEBUG.P(0,this,"skipControlCharacters(1)");
		}
    }

    private void readFixedLengthContent(ChannelBuffer buffer) {
        long length = HttpHeaders.getContentLength(message, -1);
        assert length <= Integer.MAX_VALUE;

        if (content == null) {
            content = buffer.readBytes((int) length);
        } else {
            content.writeBytes(buffer.readBytes((int) length));
        }
    }

    private State readHeaders(ChannelBuffer buffer) throws TooLongFrameException {
		try {//我加上的
		DEBUG.P(this,"readHeaders(1)");

        headerSize = 0; //每次开始解析头部消息时headerSize都置0，重新开始验证是否超出预定值
        final HttpMessage message = this.message;
        String line = readHeader(buffer);

        String lastHeader = null;
        if (line.length() != 0) {
            message.clearHeaders();
            do {
                char firstChar = line.charAt(0);

				DEBUG.P("firstChar="+firstChar);
				DEBUG.P("lastHeader="+lastHeader);

				//头值跨多行时，第二行开始要么以' '开始要么以'\t'开始
				//如:request.setHeader("testHeader", "testHeaderVa\r\n \tlue begin another line");
				//变成: 
				//lastHeader=testHeader
				//current=[testHeaderVa]
				//current=[testHeaderValue begin another line]

                if (lastHeader != null && (firstChar == ' ' || firstChar == '\t')) {
                    List<String> current = message.getHeaders(lastHeader);
                    int lastPos = current.size() - 1;

					DEBUG.P("current="+current);

                    String newString = current.get(lastPos) + line.trim();
                    current.set(lastPos, newString);

					DEBUG.P("current="+current);
                } else {
                    String[] header = splitHeader(line);
                    message.addHeader(header[0], header[1]);
                    lastHeader = header[0];
                }

                line = readHeader(buffer);
            } while (line.length() != 0);
        }

        State nextState;

		//如果是请求消息总是返回false，因为消息内容是否为空只有知道响应状态码才能判断
        if (isContentAlwaysEmpty(message)) {
            nextState = State.SKIP_CONTROL_CHARS;
        } else if (message.isChunked()) {
            // HttpMessage.isChunked() returns true when either:
            // 1) HttpMessage.setChunked(true) was called or
            // 2) 'Transfer-Encoding' is 'chunked'.
            // Because this decoder did not call HttpMessage.setChunked(true)
            // yet, HttpMessage.isChunked() should return true only when
            // 'Transfer-Encoding' is 'chunked'.
            nextState = State.READ_CHUNK_SIZE;
        } else if (HttpHeaders.getContentLength(message, -1) >= 0) {
			//当指了长度Content-Length时(>=0)
			//如：request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, 0);
            nextState = State.READ_FIXED_LENGTH_CONTENT;
        } else {
            nextState = State.READ_VARIABLE_LENGTH_CONTENT;
        }

		DEBUG.P("nextState="+nextState);

        return nextState;

		}finally{//我加上的
		DEBUG.P(0,this,"readHeaders(1)");
		}
    }

    private HttpChunkTrailer readTrailingHeaders(ChannelBuffer buffer) throws TooLongFrameException {
        headerSize = 0;
        String line = readHeader(buffer);
        String lastHeader = null;
        if (line.length() != 0) {
            HttpChunkTrailer trailer = new DefaultHttpChunkTrailer();
            do {
                char firstChar = line.charAt(0);
                if (lastHeader != null && (firstChar == ' ' || firstChar == '\t')) {
                    List<String> current = trailer.getHeaders(lastHeader);
                    if (current.size() != 0) {
                        int lastPos = current.size() - 1;
                        String newString = current.get(lastPos) + line.trim();
                        current.set(lastPos, newString);
                    } else {
                        // Content-Length, Transfer-Encoding, or Trailer
                    }
                } else {
                    String[] header = splitHeader(line);
                    String name = header[0];
                    if (!name.equalsIgnoreCase(HttpHeaders.Names.CONTENT_LENGTH) &&
                        !name.equalsIgnoreCase(HttpHeaders.Names.TRANSFER_ENCODING) &&
                        !name.equalsIgnoreCase(HttpHeaders.Names.TRAILER)) {
                        trailer.addHeader(name, header[1]);
                    }
                    lastHeader = name;
                }

                line = readHeader(buffer);
            } while (line.length() != 0);

            return trailer;
        }

        return HttpChunk.LAST_CHUNK;
    }

    private String readHeader(ChannelBuffer buffer) throws TooLongFrameException {
		try {//我加上的
		DEBUG.P(this,"readHeader(1)");
		DEBUG.P("headerSize="+headerSize);

        StringBuilder sb = new StringBuilder(64);
        int headerSize = this.headerSize;

        loop:
        for (;;) {
            char nextByte = (char) buffer.readByte();
            headerSize ++;

            switch (nextByte) {
            case HttpCodecUtil.CR:
                nextByte = (char) buffer.readByte();
                headerSize ++;
                if (nextByte == HttpCodecUtil.LF) {
                    break loop;
                }
                break;
            case HttpCodecUtil.LF:
                break loop;
            }

            // Abort decoding if the header part is too large.
            if (headerSize >= maxHeaderSize) {
                throw new TooLongFrameException(
                        "HTTP header is larger than " +
                        maxHeaderSize + " bytes.");

            }

            sb.append(nextByte);
        }

        this.headerSize = headerSize;

		DEBUG.P("header="+sb.toString());
		DEBUG.P("header.length="+sb.toString().length());
		DEBUG.P("headerSize="+headerSize);
        return sb.toString();

		}finally{//我加上的
		DEBUG.P(0,this,"readHeader(1)");
		}
    }

    protected abstract boolean isDecodingRequest();
    protected abstract HttpMessage createMessage(String[] initialLine) throws Exception;

    private int getChunkSize(String hex) {
        hex = hex.trim();
        for (int i = 0; i < hex.length(); i ++) {
            char c = hex.charAt(i);
            if (c == ';' || Character.isWhitespace(c) || Character.isISOControl(c)) {
                hex = hex.substring(0, i);
                break;
            }
        }

        return Integer.parseInt(hex, 16);
    }

    private String readLine(ChannelBuffer buffer, int maxLineLength) throws TooLongFrameException {
		StringBuilder sb = new StringBuilder(64);
        int lineLength = 0;

		try {//我加上的
		DEBUG.P(this,"readLine(2)");
		DEBUG.P("buffer="+buffer);
		DEBUG.P("maxLineLength="+maxLineLength);

        while (true) {
			//只有两种结束条件:
			//CRLF或单独的LF，没有单独的CR
            byte nextByte = buffer.readByte();
            if (nextByte == HttpCodecUtil.CR) {
                nextByte = buffer.readByte();
                if (nextByte == HttpCodecUtil.LF) {
                    return sb.toString();
                }
            }
            else if (nextByte == HttpCodecUtil.LF) {
                return sb.toString();
            }
            else {
                if (lineLength >= maxLineLength) {
                    throw new TooLongFrameException(
                            "An HTTP line is larger than " + maxLineLength +
                            " bytes.");
                }
                lineLength ++;
                sb.append((char) nextByte);
            }
        }

		}finally{//我加上的
		DEBUG.P("lineLength="+lineLength);
		DEBUG.P("line="+sb.toString());
		DEBUG.P(0,this,"readLine(2)");
		}
    }
	//RFC2616中请求行和响应状态行都有三个部份
	//Request-Line   = Method SP Request-URI SP HTTP-Version CRLF
	//Status-Line    = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
    private String[] splitInitialLine(String sb) {
		try {//我加上的
		DEBUG.P(this,"splitInitialLine(1)");
		DEBUG.P("sb="+sb);

        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonWhitespace(sb, 0);
        aEnd = findWhitespace(sb, aStart);

        bStart = findNonWhitespace(sb, aEnd);
        bEnd = findWhitespace(sb, bStart);

        cStart = findNonWhitespace(sb, bEnd);
        cEnd = findEndOfString(sb);

        return new String[] {
                sb.substring(aStart, aEnd),
                sb.substring(bStart, bEnd),
                cStart < cEnd? sb.substring(cStart, cEnd) : "" };

		}finally{//我加上的
		DEBUG.P(0,this,"splitInitialLine(1)");
		}
    }

    private String[] splitHeader(String sb) {
        final int length = sb.length();
        int nameStart;
        int nameEnd;
        int colonEnd;
        int valueStart;
        int valueEnd;

        nameStart = findNonWhitespace(sb, 0);
        for (nameEnd = nameStart; nameEnd < length; nameEnd ++) {
            char ch = sb.charAt(nameEnd);
            if (ch == ':' || Character.isWhitespace(ch)) {
                break;
            }
        }

        for (colonEnd = nameEnd; colonEnd < length; colonEnd ++) {
            if (sb.charAt(colonEnd) == ':') {
                colonEnd ++;
                break;
            }
        }

        valueStart = findNonWhitespace(sb, colonEnd);
        if (valueStart == length) {
            return new String[] {
                    sb.substring(nameStart, nameEnd),
                    ""
            };
        }

        valueEnd = findEndOfString(sb);
        return new String[] {
                sb.substring(nameStart, nameEnd),
                sb.substring(valueStart, valueEnd)
        };
    }

    private int findNonWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private int findWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private int findEndOfString(String sb) {
        int result;
        for (result = sb.length(); result > 0; result --) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }
}
