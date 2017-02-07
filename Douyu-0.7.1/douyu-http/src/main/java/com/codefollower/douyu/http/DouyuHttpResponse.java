package com.codefollower.douyu.http;

import static com.codefollower.douyu.http.HttpResponseStatus.OK;
import static com.codefollower.douyu.http.HttpVersion.HTTP_1_1;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import com.codefollower.douyu.netty.buffer.ChannelBuffer;
import com.codefollower.douyu.netty.buffer.ChannelBufferOutputStream;
import com.codefollower.douyu.netty.buffer.ChannelBuffers;

public class DouyuHttpResponse extends DefaultHttpResponse implements douyu.http.HttpResponse {
	private String contentType;
	private String charset;
	private PrintWriter writer;
	private ChannelBuffer content = ChannelBuffers.dynamicBuffer();

	private StringWriter sw = new StringWriter();

	public DouyuHttpResponse() {
		this(HTTP_1_1, OK);
	}

	public DouyuHttpResponse(HttpVersion version, HttpResponseStatus status) {
		super(version, status);
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public PrintWriter getWriter() throws Exception {
		if (writer == null) {
			if (charset != null) {
				writer = new PrintWriter(new OutputStreamWriter(new ChannelBufferOutputStream(content), charset));
			} else {
				// writer = new PrintWriter(new OutputStreamWriter(new
				// ChannelBufferOutputStream(content), "utf-8"));
				writer = new PrintWriter(sw);
			}
		}
		return writer;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		this.charset = charset;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public void sendError(int status, String message) {
	}
	
	@Override
	public ChannelBuffer getContent() {
		try {
			content.clear();
			content.writeBytes(sw.toString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return content;
	}

}
