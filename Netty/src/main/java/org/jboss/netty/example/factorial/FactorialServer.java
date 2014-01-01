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
package org.jboss.netty.example.factorial;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.*;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
/**
 * Receives a sequence of integers from a {@link FactorialClient} to calculate
 * the factorial of the specified integer.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class FactorialServer {

    public static void main(String[] args) throws Exception {
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new OioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new FactorialServerPipelineFactory());

		bootstrap.setParentHandler(new MyParentChannelHandler());

		//ServerSocket的参数
		bootstrap.setOption("receiveBufferSize", 4096);
		bootstrap.setOption("reuseAddress", 4096); //数字如果大于0也当成是true
		bootstrap.setOption("backlog", 1000);

		//ServerSocket.accept后得到的Socket的参数
		bootstrap.setOption("child.receiveBufferSize", 4096);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(8080));


		//Thread.sleep(1500);

		//bootstrap.releaseExternalResources();
    }

	public static class MyParentChannelHandler extends SimpleChannelUpstreamHandler {
		/**
		 * Invoked when a message object (e.g: {@link ChannelBuffer}) was received
		 * from a remote peer.
		 */
		public void messageReceived(
				ChannelHandlerContext ctx, MessageEvent e) throws Exception {

			System.out.println("MyParentChannelHandler: messageReceived: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when an exception was raised by an I/O thread or a
		 * {@link ChannelHandler}.
		 */
		public void exceptionCaught(
				ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: exceptionCaught: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a {@link Channel} is open, but not bound nor connected.
		 */
		public void channelOpen(
				ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: channelOpen: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a {@link Channel} is open and bound to a local address,
		 * but not connected.
		 */
		public void channelBound(
				ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: channelBound: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a {@link Channel} is open, bound to a local address, and
		 * connected to a remote address.
		 */
		public void channelConnected(
				ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: channelConnected: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a {@link Channel}'s {@link Channel#getInterestOps() interestOps}
		 * was changed.
		 */
		public void channelInterestChanged(
				ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: channelInterestChanged: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a {@link Channel} was disconnected from its remote peer.
		 */
		public void channelDisconnected(
				ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: channelDisconnected: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a {@link Channel} was unbound from the current local address.
		 */
		public void channelUnbound(
				ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: channelUnbound: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a {@link Channel} was closed and all its related resources
		 * were released.
		 */
		public void channelClosed(
				ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: channelClosed: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when something was written into a {@link Channel}.
		 */
		public void writeComplete(
				ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: writeComplete: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a child {@link Channel} was open.
		 * (e.g. a server channel accepted a connection)
		 */
		public void childChannelOpen(
				ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: childChannelOpen: "+ e);
			ctx.sendUpstream(e);
		}

		/**
		 * Invoked when a child {@link Channel} was closed.
		 * (e.g. the accepted connection was closed)
		 */
		public void childChannelClosed(
				ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
			System.out.println("MyParentChannelHandler: childChannelClosed: "+ e);
			ctx.sendUpstream(e);
		}
	}
}
