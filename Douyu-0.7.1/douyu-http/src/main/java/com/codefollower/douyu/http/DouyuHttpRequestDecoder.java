package com.codefollower.douyu.http;


public class DouyuHttpRequestDecoder extends HttpMessageDecoder {

	/**
	 * Creates a new instance with the default
	 * {@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}, and
	 * {@code maxChunkSize (8192)}.
	 */
	public DouyuHttpRequestDecoder() {
		super();
	}

	/**
	 * Creates a new instance with the specified parameters.
	 */
	public DouyuHttpRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
		super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
	}

	@Override
	protected HttpMessage createMessage(String[] initialLine) throws Exception {
		return new DouyuHttpRequest(HttpVersion.valueOf(initialLine[2]), HttpMethod.valueOf(initialLine[0]), initialLine[1]);
	}
}
