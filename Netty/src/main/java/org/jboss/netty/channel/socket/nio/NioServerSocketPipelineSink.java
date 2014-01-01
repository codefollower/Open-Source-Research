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
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

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
 * @version $Rev: 2144 $, $Date: 2010-02-09 12:41:12 +0900 (Tue, 09 Feb 2010) $
 *
 */
class NioServerSocketPipelineSink extends AbstractChannelSink {
	private static my.Debug DEBUG=new my.Debug(my.Debug.NioServerSocketPipelineSink);//我加上的

    static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioServerSocketPipelineSink.class);
    private static final AtomicInteger nextId = new AtomicInteger();

    private final int id = nextId.incrementAndGet();
    private final NioWorker[] workers;
    private final AtomicInteger workerIndex = new AtomicInteger();

    NioServerSocketPipelineSink(Executor workerExecutor, int workerCount) {
		try {//我加上的
		DEBUG.P(this,"NioServerSocketPipelineSink(2)");
		
        workers = new NioWorker[workerCount];
        for (int i = 0; i < workers.length; i ++) {
            workers[i] = new NioWorker(id, i + 1, workerExecutor);
        }

		//DEBUG.PA("workers",workers);

		}finally{//我加上的
		DEBUG.P(0,this,"NioServerSocketPipelineSink(2)");
		}
    }

    public void eventSunk(
            ChannelPipeline pipeline, ChannelEvent e) throws Exception {
		try {//我加上的
		DEBUG.P(this,"eventSunk(2)");
		DEBUG.P("pipeline="+pipeline);
		DEBUG.P("e="+e);
		//DEBUG.e();

        Channel channel = e.getChannel();

		DEBUG.P("channel.getClass()="+channel.getClass());
        if (channel instanceof NioServerSocketChannel) {
            handleServerSocket(e);
        } else if (channel instanceof NioSocketChannel) {
            handleAcceptedSocket(e);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"eventSunk(2)");
		}
    }

    private void handleServerSocket(ChannelEvent e) {
		try {//我加上的
		DEBUG.P(this,"handleServerSocket(1)");

        if (!(e instanceof ChannelStateEvent)) {
            return;
        }

        ChannelStateEvent event = (ChannelStateEvent) e;
        NioServerSocketChannel channel =
            (NioServerSocketChannel) event.getChannel();
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

        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent event = (ChannelStateEvent) e;
            NioSocketChannel channel = (NioSocketChannel) event.getChannel();
            ChannelFuture future = event.getFuture();
            ChannelState state = event.getState();
            Object value = event.getValue();

            switch (state) {
            case OPEN:
                if (Boolean.FALSE.equals(value)) {
                    channel.worker.close(channel, future);
                }
                break;
            case BOUND:
            case CONNECTED:
                if (value == null) {
                    channel.worker.close(channel, future);
                }
                break;
            case INTEREST_OPS:
                channel.worker.setInterestOps(channel, future, ((Integer) value).intValue());
                break;
            }
        } else if (e instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) e;
            NioSocketChannel channel = (NioSocketChannel) event.getChannel();
            boolean offered = channel.writeBuffer.offer(event);
            assert offered;
            channel.worker.writeFromUserCode(channel);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"handleAcceptedSocket(2)");
		}
    }

    private void bind(
            NioServerSocketChannel channel, ChannelFuture future,
            SocketAddress localAddress) {
		try {//我加上的
		DEBUG.P(this,"bind(3)");

        boolean bound = false;
        boolean bossStarted = false;
        try {
            channel.socket.socket().bind(localAddress, channel.getConfig().getBacklog());
            bound = true;

            future.setSuccess();
            fireChannelBound(channel, channel.getLocalAddress());

            Executor bossExecutor =
                ((NioServerSocketChannelFactory) channel.getFactory()).bossExecutor;
            bossExecutor.execute(
                    new IoWorkerRunnable(
                            new ThreadRenamingRunnable(
                                    new Boss(channel),
                                    "New I/O server boss #" + id +
                                    " (channelId: " + channel.getId() +
                                    ", " + channel.getLocalAddress() + ')')));
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

    private void close(NioServerSocketChannel channel, ChannelFuture future) {
        boolean bound = channel.isBound();
        try {
            if (channel.socket.isOpen()) {
                channel.socket.close();
                Selector selector = channel.selector;
                if (selector != null) {
                    selector.wakeup();
                }
            }

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
    }

    NioWorker nextWorker() {
        return workers[Math.abs(
                workerIndex.getAndIncrement() % workers.length)];
    }

    private final class Boss implements Runnable {
        private final Selector selector;
        private final NioServerSocketChannel channel;

        Boss(NioServerSocketChannel channel) throws IOException {
            this.channel = channel;

            selector = Selector.open();

            boolean registered = false;
            try {
                channel.socket.register(selector, SelectionKey.OP_ACCEPT);
                registered = true;
            } finally {
                if (!registered) {
                    closeSelector();
                }
            }

            channel.selector = selector;
        }

        public void run() {
			try {//我加上的
			DEBUG.P(this,"run()");

            final Thread currentThread = Thread.currentThread();

            channel.shutdownLock.lock();
            for (;;) {
                try {
                    if (selector.select(1000) > 0) {
                        selector.selectedKeys().clear();
                    }

					//在NioServerSocketChannel的构造函数中已将channel.socket设成非阻塞模式
                    SocketChannel acceptedSocket = channel.socket.accept();

					//select后，如果没有通道套接字的连接，accept()返回null
					DEBUG.P("acceptedSocket="+acceptedSocket);
                    if (acceptedSocket != null) {
                        registerAcceptedChannel(acceptedSocket, currentThread);
                    }
                } catch (SocketTimeoutException e) {
                	//只有ServerSocket.accept()才会出现java.net.SocketTimeoutException
                	//ServerSocketChannel.accept()是不会出限的，会一直阻塞在accept()里头
                	
                    // Thrown every second to get ClosedChannelException
                    // raised.
                } catch (CancelledKeyException e) {
                    // Raised by accept() when the server socket was closed.
                } catch (ClosedSelectorException e) {
                    // Raised by accept() when the server socket was closed.
                } catch (ClosedChannelException e) {
                    // Closed as requested.
                    break;
                } catch (IOException e) {
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
            closeSelector();

			}finally{//我加上的
			DEBUG.P(0,this,"run()");
			}
        }

        private void registerAcceptedChannel(SocketChannel acceptedSocket, Thread currentThread) {
			try {//我加上的
			DEBUG.P(this,"registerAcceptedChannel(2)");

            try {
				//channel.socket.accept()后得到的每个SocketChannel都会对应一个新的pipeline
                ChannelPipeline pipeline =
                    channel.getConfig().getPipelineFactory().getPipeline();
                NioWorker worker = nextWorker();
                worker.register(new NioAcceptedSocketChannel(
                        channel.getFactory(), pipeline, channel,
                        NioServerSocketPipelineSink.this, acceptedSocket,
                        worker, currentThread), null);
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

			}finally{//我加上的
			DEBUG.P(0,this,"registerAcceptedChannel(2)");
			}
        }

        private void closeSelector() {
            channel.selector = null;
            try {
                selector.close();
            } catch (Exception e) {
                logger.warn("Failed to close a selector.", e);
            }
        }
    }
}
