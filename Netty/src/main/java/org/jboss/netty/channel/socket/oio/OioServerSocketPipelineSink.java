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

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.AbstractChannelSink;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
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
class OioServerSocketPipelineSink extends AbstractChannelSink {
	private static my.Debug DEBUG=new my.Debug(my.Debug.OioWorker);//我加上的

    static final InternalLogger logger =
        InternalLoggerFactory.getInstance(OioServerSocketPipelineSink.class);

    final Executor workerExecutor;

    OioServerSocketPipelineSink(Executor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    public void eventSunk(
            ChannelPipeline pipeline, ChannelEvent e) throws Exception {
		try {//我加上的
		DEBUG.P(this,"eventSunk(2)");
		DEBUG.P("e="+e);

        Channel channel = e.getChannel();
        if (channel instanceof OioServerSocketChannel) {
            handleServerSocket(e);
        } else if (channel instanceof OioAcceptedSocketChannel) {
            handleAcceptedSocket(e);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"eventSunk(2)");
		}
    }

    private void handleServerSocket(ChannelEvent e) {
		try {//我加上的
		DEBUG.P(this,"handleServerSocket(1)");
		DEBUG.P("e="+e);

        if (!(e instanceof ChannelStateEvent)) {
            return;
        }

        ChannelStateEvent event = (ChannelStateEvent) e;
        OioServerSocketChannel channel =
            (OioServerSocketChannel) event.getChannel();
        ChannelFuture future = event.getFuture();
        ChannelState state = event.getState();
        Object value = event.getValue();

		DEBUG.P("state="+state);
		DEBUG.P("value="+value);

        switch (state) {
        case OPEN:
            if (Boolean.FALSE.equals(value)) {
                close(channel, future);
            }
            break;
        case BOUND:
            if (value != null) {
                bind(channel, future, (SocketAddress) value);
            } else {
                close(channel, future);
            }
            break;
        }

		}finally{//我加上的
		DEBUG.P(0,this,"handleServerSocket(1)");
		}
    }

    private void handleAcceptedSocket(ChannelEvent e) {
		try {//我加上的
		DEBUG.P(this,"handleAcceptedSocket(1)");
		DEBUG.P("e="+e);

        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent event = (ChannelStateEvent) e;
            OioAcceptedSocketChannel channel =
                (OioAcceptedSocketChannel) event.getChannel();
            ChannelFuture future = event.getFuture();
            ChannelState state = event.getState();
            Object value = event.getValue();

			DEBUG.P("state="+state);
			DEBUG.P("value="+value);

            switch (state) {
            case OPEN:
                if (Boolean.FALSE.equals(value)) {
                    OioWorker.close(channel, future);
                }
                break;
            case BOUND:
            case CONNECTED:
                if (value == null) {
                    OioWorker.close(channel, future);
                }
                break;
            case INTEREST_OPS:
                OioWorker.setInterestOps(channel, future, ((Integer) value).intValue());
                break;
            }
        } else if (e instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) e;
            OioSocketChannel channel = (OioSocketChannel) event.getChannel();
            ChannelFuture future = event.getFuture();
            Object message = event.getMessage();
            OioWorker.write(channel, future, message);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"handleAcceptedSocket(1)");
		}
    }

    private void bind(
            OioServerSocketChannel channel, ChannelFuture future,
            SocketAddress localAddress) {
		try {//我加上的
		DEBUG.P(this,"bind(3)");
		DEBUG.P("future="+future);
		DEBUG.P("localAddress="+localAddress);

        boolean bound = false;
        boolean bossStarted = false;
        try {
            channel.socket.bind(localAddress, channel.getConfig().getBacklog());
            bound = true;

            future.setSuccess();
            localAddress = channel.getLocalAddress();
            fireChannelBound(channel, localAddress);

            Executor bossExecutor =
                ((OioServerSocketChannelFactory) channel.getFactory()).bossExecutor;
            bossExecutor.execute(
                    new IoWorkerRunnable(
                            new ThreadRenamingRunnable(
                                    new Boss(channel),
                                    "Old I/O server boss (channelId: " +
                                    channel.getId() + ", " + localAddress + ')')));
            bossStarted = true;
        } catch (Throwable t) {
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        } finally {
            if (!bossStarted && bound) {
                close(channel, future);
            }
        }

		}finally{//我加上的
		DEBUG.P(0,this,"bind(3)");
		}
    }

    private void close(OioServerSocketChannel channel, ChannelFuture future) {
		try {//我加上的
		DEBUG.P(this,"close(2)");
		DEBUG.P("future="+future);

        boolean bound = channel.isBound();

		DEBUG.P("bound="+bound);

        try {
            channel.socket.close();

            // Make sure the boss thread is not running so that that the future
            // is notified after a new connection cannot be accepted anymore.
            // See NETTY-256 for more information.
            channel.shutdownLock.lock();
            try {
                if (channel.setClosed()) {
                    future.setSuccess();
                    if (bound) {
                        fireChannelUnbound(channel);
                    }
                    fireChannelClosed(channel);
                } else {
                    future.setSuccess();
                }
            } finally {
                channel.shutdownLock.unlock();
            }
        } catch (Throwable t) {
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"close(2)");
		}
    }

    private final class Boss implements Runnable {
        private final OioServerSocketChannel channel;

        Boss(OioServerSocketChannel channel) {
            this.channel = channel;
        }

        public void run() {
			try {//我加上的
			DEBUG.P(this,"run()");
			try {
				//默认一秒种accept超时
				DEBUG.P("channel.socket.getSoTimeout()="+channel.socket.getSoTimeout());
			} catch (Exception e) {}

            channel.shutdownLock.lock();
            while (channel.isBound()) {
                try {
					///*
					Thread[] threads = new Thread[Thread.activeCount()];
					Thread.enumerate(threads);

					DEBUG.P("Thread.currentThread()="+Thread.currentThread());
					DEBUG.P("Thread.activeCount()="+Thread.activeCount());
					for(Thread t: threads) {
						DEBUG.P("t="+t);
						DEBUG.P("t.isDaemon()="+t.isDaemon());
					}
					//*/


                    Socket acceptedSocket = channel.socket.accept();

					DEBUG.P("acceptedSocket="+acceptedSocket);
                    try {
                        ChannelPipeline pipeline =
                            channel.getConfig().getPipelineFactory().getPipeline();
                        final OioAcceptedSocketChannel acceptedChannel =
                            new OioAcceptedSocketChannel(
                                    channel,
                                    channel.getFactory(),
                                    pipeline,
                                    OioServerSocketPipelineSink.this,
                                    acceptedSocket);
                        workerExecutor.execute(
                                new IoWorkerRunnable(
                                        new ThreadRenamingRunnable(
                                                new OioWorker(acceptedChannel),
                                                "Old I/O server worker (parentId: " +
                                                channel.getId() + ", channelId: " +
                                                acceptedChannel.getId() + ", " +
                                                channel.getRemoteAddress() + " => " +
                                                channel.getLocalAddress() + ')')));
                    } catch (Exception e) {
                        logger.warn(
                                "Failed to initialize an accepted socket.", e);
                        try {
                            acceptedSocket.close();
                        } catch (IOException e2) {
                            logger.warn(
                                    "Failed to close a partially accepted socket.",
                                    e2);
                        }
                    }
                } catch (SocketTimeoutException e) {
					DEBUG.P(new java.util.Date()+" e="+e);
                    // Thrown every second to stop when requested.
                } catch (IOException e) {
                    // Do not log the exception if the server socket was closed
                    // by a user.
                    if (!channel.socket.isBound() || channel.socket.isClosed()) {
                        break;
                    }

                    logger.warn(
                            "Failed to accept a connection.", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        // Ignore
                    }
                }
            }
            channel.shutdownLock.unlock();

			}finally{//我加上的
			DEBUG.P(0,this,"run()");
			}
        }
    }
}
