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
package com.codefollower.douyu.netty.channel.nio;

import static com.codefollower.douyu.netty.channel.Channels.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import com.codefollower.douyu.logging.InternalLogger;
import com.codefollower.douyu.logging.InternalLoggerFactory;
import com.codefollower.douyu.netty.channel.AbstractChannel;
import com.codefollower.douyu.netty.channel.ChannelException;
import com.codefollower.douyu.netty.channel.ChannelFactory;
import com.codefollower.douyu.netty.channel.ChannelFuture;
import com.codefollower.douyu.netty.channel.ChannelPipeline;
import com.codefollower.douyu.netty.channel.ChannelSink;
import com.codefollower.douyu.netty.channel.ServerChannel;

/**
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev$, $Date$
 *
 */
class NioServerSocketChannel extends AbstractChannel implements ServerChannel {

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioServerSocketChannel.class);

    final ServerSocketChannel socket;
    final Lock shutdownLock = new ReentrantLock();
    volatile Selector selector;
    private final NioServerSocketChannelConfig config;

    NioServerSocketChannel(
            ChannelFactory factory,
            ChannelPipeline pipeline,
            ChannelSink sink) {

        super(null, factory, pipeline, sink);

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

        config = new NioServerSocketChannelConfig(socket.socket());

        fireChannelOpen(this);
    }

    @Override
    public NioServerSocketChannelConfig getConfig() {
        return config;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) socket.socket().getLocalSocketAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public boolean isBound() {
        return isOpen() && socket.socket().isBound();
    }

    @Override
    protected boolean setClosed() {
        return super.setClosed();
    }
    
    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return getUnsupportedOperationFuture();
    }

    @Override
    public ChannelFuture disconnect() {
        return getUnsupportedOperationFuture();
    }

    @Override
    public int getInterestOps() {
        return OP_NONE;
    }

    @Override
    public ChannelFuture setInterestOps(int interestOps) {
        return getUnsupportedOperationFuture();
    }

    @Override
    protected void setInterestOpsNow(int interestOps) {
        // Ignore.
    }

    @Override
    public ChannelFuture write(Object message) {
        return getUnsupportedOperationFuture();
    }

    @Override
    public ChannelFuture write(Object message, SocketAddress remoteAddress) {
        return getUnsupportedOperationFuture();
    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
