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
package org.jboss.netty.channel.socket.nio;

import static org.jboss.netty.channel.Channels.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.AbstractServerChannel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.socket.DefaultServerSocketChannelConfig;
import org.jboss.netty.channel.socket.ServerSocketChannelConfig;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 *
 */
class NioServerSocketChannel extends AbstractServerChannel
                             implements org.jboss.netty.channel.socket.ServerSocketChannel {
	private static my.Debug DEBUG=new my.Debug(my.Debug.NioServerSocketChannel);//我加上的

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioServerSocketChannel.class);

    final ServerSocketChannel socket;
    final Lock shutdownLock = new ReentrantLock();
    volatile Selector selector;
    private final ServerSocketChannelConfig config;

    NioServerSocketChannel(
            ChannelFactory factory,
            ChannelPipeline pipeline,
            ChannelSink sink) {

        super(factory, pipeline, sink);

		try {//我加上的
		DEBUG.P(this,"NioServerSocketChannel(3)");

		//如果没有调用过ServerBootstrap的setParentHandler
		//则这个pipeline是StaticChannelPipeline，
		//且pipeline中只有一个Upstream类型的ServerBootstrap.Binder
		//否则是有两个Handler的DefaultChannelPipeline，
		//第一个是Upstream类型的ServerBootstrap.Binder
		//第二个是用户指定的ChannelHandler(不知道是Upstream类型的还是Downstream类型的)
		DEBUG.P("pipeline="+pipeline);

        try {
            socket = ServerSocketChannel.open();
        } catch (IOException e) {
            throw new ChannelException(
                    "Failed to open a server socket.", e);
        }

        try {
            socket.configureBlocking(false);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e2) {
                logger.warn(
                        "Failed to close a partially initialized socket.", e2);
            }

            throw new ChannelException("Failed to enter non-blocking mode.", e);
        }

		//这里并没有把ServerBootstrap.setOption进来的socket参数赋给socket.socket()
		//而是在fireChannelOpen(this)时在ServerBootstrap$Binder===>channelOpen(2)中setOptions时
		//才调用DefaultServerChannelConfig.setOptions把socket参数赋给socket.socket()
        config = new DefaultServerSocketChannelConfig(socket.socket());

		//Channels类
        fireChannelOpen(this);

		}finally{//我加上的
		DEBUG.P(0,this,"NioServerSocketChannel(3)");
		}
    }

    public ServerSocketChannelConfig getConfig() {
        return config;
    }

    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) socket.socket().getLocalSocketAddress();
    }

    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    public boolean isBound() {
        return isOpen() && socket.socket().isBound();
    }

    @Override
    protected boolean setClosed() {
        return super.setClosed();
    }
}
