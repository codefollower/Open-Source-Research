package com.codefollower.douyu.http;

import java.io.IOException;


import com.codefollower.douyu.http.websocket.DefaultWebSocketFrame;
import com.codefollower.douyu.netty.buffer.ChannelBuffers;
import com.codefollower.douyu.netty.channel.Channel;

public class DouyuOutbound implements douyu.http.WebSocket.Outbound {
	private Channel channel;

	public DouyuOutbound(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void close() {
		channel.close();
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void send(String data) throws IOException {
		checkClosed();
		channel.write(new DefaultWebSocketFrame(data));
	}

	@Override
	public void send(int type, String data) throws IOException {
		checkClosed();
		channel.write(new DefaultWebSocketFrame(type, ChannelBuffers
				.copiedBuffer(data, com.codefollower.douyu.netty.util.CharsetUtil.UTF_8)));
	}

	@Override
	public void send(int type, byte[] data) throws IOException {
		send(type, data, 0, data.length);
	}

	@Override
	public void send(int type, byte[] data, int offset, int length) throws IOException {
		checkClosed();
		channel.write(new DefaultWebSocketFrame(type, ChannelBuffers.wrappedBuffer(data, offset, length)));
	}

	private void checkClosed() throws IOException {
		if (!isOpen())
			throw new IOException("WebSocket is closed");
	}

}
