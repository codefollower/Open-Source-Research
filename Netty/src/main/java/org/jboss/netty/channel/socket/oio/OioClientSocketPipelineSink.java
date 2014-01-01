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
package org.jboss.netty.channel.socket.oio;

import static org.jboss.netty.channel.Channels.*;

import java.io.PushbackInputStream;
import java.net.SocketAddress;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.AbstractChannelSink;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.jboss.netty.util.internal.IoWorkerRunnable;

/**
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 *
 */
class OioClientSocketPipelineSink extends AbstractChannelSink {
	private static my.Debug DEBUG=new my.Debug(my.Debug.OioClientSocketPipelineSink);//我加上的

    private final Executor workerExecutor;

    OioClientSocketPipelineSink(Executor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    public void eventSunk(
            ChannelPipeline pipeline, ChannelEvent e) throws Exception {
		try {//我加上的
		DEBUG.P(this,"eventSunk(2)");
		DEBUG.P("pipeline="+pipeline);
		DEBUG.P("e="+e);

        OioClientSocketChannel channel = (OioClientSocketChannel) e.getChannel();
        ChannelFuture future = e.getFuture();

		DEBUG.P("future="+future);

        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent stateEvent = (ChannelStateEvent) e;
            ChannelState state = stateEvent.getState();
            Object value = stateEvent.getValue();

			DEBUG.P("state="+state);
			DEBUG.P("value="+value);

            switch (state) {
            case OPEN:
                if (Boolean.FALSE.equals(value)) {
                    OioWorker.close(channel, future);
                }
                break;
            case BOUND:
                if (value != null) {
                    bind(channel, future, (SocketAddress) value);
                } else {
                    OioWorker.close(channel, future);
                }
                break;
            case CONNECTED:
                if (value != null) {
                    connect(channel, future, (SocketAddress) value);
                } else {
                    OioWorker.close(channel, future);
                }
                break;
            case INTEREST_OPS:
                OioWorker.setInterestOps(channel, future, ((Integer) value).intValue());
                break;
            }
        } else if (e instanceof MessageEvent) {
            OioWorker.write(
                    channel, future,
                    ((MessageEvent) e).getMessage());
        }

		}finally{//我加上的
		DEBUG.P(0,this,"eventSunk(2)");
		}
    }

	//ChannelFuture就相当于一个方法的调用结果，这个结果不可能在将来才知道
    private void bind(
            OioClientSocketChannel channel, ChannelFuture future,
            SocketAddress localAddress) {
        try {
            channel.socket.bind(localAddress);
            future.setSuccess();
            fireChannelBound(channel, channel.getLocalAddress());
        } catch (Throwable t) {
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }
    }

	//这个方法启动IO读写线程
    private void connect(
            OioClientSocketChannel channel, ChannelFuture future,
            SocketAddress remoteAddress) {
		try {//我加上的
		DEBUG.P(this,"connect(3)");
		DEBUG.P("future="+future);
		DEBUG.P("remoteAddress="+remoteAddress);

        boolean bound = channel.isBound();
        boolean connected = false;
        boolean workerStarted = false;

        future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

        try {
            channel.socket.connect(
                    remoteAddress, channel.getConfig().getConnectTimeoutMillis());
            connected = true;

            // Obtain I/O stream.
            channel.in = new PushbackInputStream(channel.socket.getInputStream(), 1);
            channel.out = channel.socket.getOutputStream();

            // Fire events.
            future.setSuccess();
            if (!bound) {
                fireChannelBound(channel, channel.getLocalAddress());
            }
            fireChannelConnected(channel, channel.getRemoteAddress());

            // Start the business.
            workerExecutor.execute(
                    new IoWorkerRunnable(
                            new ThreadRenamingRunnable(
                                    new OioWorker(channel),
                                    "Old I/O client worker (channelId: " +
                                    channel.getId() + ", " +
                                    channel.getLocalAddress() + " => " +
                                    channel.getRemoteAddress() + ')')));

			Thread.sleep(2000);
			Thread[] threads = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);

			DEBUG.P("Thread.currentThread()="+Thread.currentThread());
			DEBUG.P("Thread.activeCount()="+Thread.activeCount());
			for(Thread t: threads) {
				DEBUG.P("t="+t);
				DEBUG.P("t.isDaemon()="+t.isDaemon());
			}
				

            workerStarted = true;
        } catch (Throwable t) {
			DEBUG.P("Throwable t="+t);
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        } finally {
            if (connected && !workerStarted) {
                OioWorker.close(channel, future);
            }
        }

		}finally{//我加上的
		DEBUG.P(0,this,"connect(3)");
		}
    }
}
