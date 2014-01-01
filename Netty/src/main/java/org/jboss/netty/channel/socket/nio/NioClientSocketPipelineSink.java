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
import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.AbstractChannelSink;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.jboss.netty.util.internal.IoWorkerRunnable;
import org.jboss.netty.util.internal.LinkedTransferQueue;

/**
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2144 $, $Date: 2010-02-09 12:41:12 +0900 (Tue, 09 Feb 2010) $
 *
 */
//所有的Sink都只会生成一个，一个Sink对应一个ChannelFactory
class NioClientSocketPipelineSink extends AbstractChannelSink {
	private static my.Debug DEBUG=new my.Debug(my.Debug.NioServerSocketPipelineSink);//我加上的

    static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioClientSocketPipelineSink.class);
    private static final AtomicInteger nextId = new AtomicInteger();

	//有可能会变成负值
    final int id = nextId.incrementAndGet();
    final Executor bossExecutor;
    private final Boss boss = new Boss();
    private final NioWorker[] workers;
    private final AtomicInteger workerIndex = new AtomicInteger();

	//bossExecutor中的线程用来处理Socket的connect操作，
	//workerExecutor中的线程用来处理Socket的读写操作
    NioClientSocketPipelineSink(
            Executor bossExecutor, Executor workerExecutor, int workerCount) {
		try {//我加上的
		DEBUG.P(this,"NioClientSocketPipelineSink(3)");
		DEBUG.P("workerCount="+workerCount);

        this.bossExecutor = bossExecutor;
        workers = new NioWorker[workerCount]; //workerCount默认是cpu个数的两倍
        for (int i = 0; i < workers.length; i ++) {
            workers[i] = new NioWorker(id, i + 1, workerExecutor);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"NioClientSocketPipelineSink(3)");
		}
    }

	//可以多次调用同一个ClientBootstrap实例的connect方法建立多个Client通道，
	//这样多个Client通道都是使用同一个NioClientSocketPipelineSink实例，
	//但是每个Client通道都会得到一个新的ChannelPipeline实例
    public void eventSunk(
            ChannelPipeline pipeline, ChannelEvent e) throws Exception {

		try {//我加上的
		DEBUG.P(this,"eventSunk(2)");
		DEBUG.P("pipeline="+pipeline);
		DEBUG.P("e="+e);
		DEBUG.P("e.getClass()="+e.getClass());

        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent event = (ChannelStateEvent) e;
            NioClientSocketChannel channel =
                (NioClientSocketChannel) event.getChannel();
            ChannelFuture future = event.getFuture();
            ChannelState state = event.getState();
            Object value = event.getValue();

			DEBUG.P("state="+state);
			DEBUG.P("value="+value);

            switch (state) {
            case OPEN:
				//不用处理TRUE的情况了，
				//因为在生成NioClientSocketChannel的构造函数中调用newSocket()就算是打开了
                if (Boolean.FALSE.equals(value)) {
                    channel.worker.close(channel, future);
                }
                break;
            case BOUND:
                if (value != null) {
                    bind(channel, future, (SocketAddress) value);
                } else {
                    channel.worker.close(channel, future);
                }
                break;
            case CONNECTED:
                if (value != null) {
                    connect(channel, future, (SocketAddress) value);
                } else {
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
		DEBUG.P(0,this,"eventSunk(2)");
		}
    }

	//比如在ClientBootstrap中调用了connect(2)，指定了localAddress
    private void bind(
            NioClientSocketChannel channel, ChannelFuture future,
            SocketAddress localAddress) {
        try {
            channel.socket.socket().bind(localAddress);
            channel.boundManually = true;
            channel.setBound();
            future.setSuccess();
            fireChannelBound(channel, channel.getLocalAddress());
        } catch (Throwable t) {
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }
    }

    private void connect(
            final NioClientSocketChannel channel, final ChannelFuture cf,
            SocketAddress remoteAddress) {
		try {//我加上的
		DEBUG.P(this,"connect(3)");
		DEBUG.P("remoteAddress="+remoteAddress);

        try {
			//这里的socket.connect是非阻塞的，
			//在NioClientSocketChannel的构造函数中调用newSocket()的socket.configureBlocking(false)
			//如果不能马上连到remoteAddress，则返回false
            if (channel.socket.connect(remoteAddress)) {
				DEBUG.P("remoteAddress connect");

                channel.worker.register(channel, cf);
            } else {
				//默认是走else分支
                channel.getCloseFuture().addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture f)
                            throws Exception {

						DEBUG.P("remoteAddress connect cf.isDone()="+cf.isDone());

                        if (!cf.isDone()) {
                            cf.setFailure(new ClosedChannelException());
                        }
                    }
                });
                cf.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                channel.connectFuture = cf;
                boss.register(channel);
            }

        } catch (Throwable t) {
            cf.setFailure(t);
            fireExceptionCaught(channel, t);
            channel.worker.close(channel, succeededFuture(channel));
        }

		}finally{//我加上的
		DEBUG.P(0,this,"connect(3)");
		}
    }

	//这里不用担心当不断workerIndex.getAndIncrement()超过Integer.MAX_VALUE变负数
	//因为这里使用了Math.abs来求绝对值，所以当超过Integer.MAX_VALUE时，
	//又会从大到小取值(Integer.MAX_VALUE是2147483647，加1后变成最小值MIN_VALUE(-2147483648))
	//再加1又变成-2147483647
    NioWorker nextWorker() {
        return workers[Math.abs(
                workerIndex.getAndIncrement() % workers.length)];
    }

	//Boss主要处理SocketChannel的connect，因为connect在非阻塞的情况下可能马上返回false，
	//必须在一个for循环中检测connect的状态
    private final class Boss implements Runnable {

        volatile Selector selector;
        private boolean started;
        private final AtomicBoolean wakenUp = new AtomicBoolean();
        private final Object startStopLock = new Object();
        private final Queue<Runnable> registerTaskQueue = new LinkedTransferQueue<Runnable>();

        Boss() {
            super();
        }

        void register(NioClientSocketChannel channel) {
            Runnable registerTask = new RegisterTask(this, channel);
            Selector selector;

            synchronized (startStopLock) {
                if (!started) {
                    // Open a selector if this worker didn't start yet.
                    try {
                        this.selector = selector =  Selector.open();
                    } catch (Throwable t) {
                        throw new ChannelException(
                                "Failed to create a selector.", t);
                    }

                    // Start the worker thread with the new Selector.
                    boolean success = false;
                    try {
                        bossExecutor.execute(
                                new IoWorkerRunnable(
                                        new ThreadRenamingRunnable(
                                                this, "New I/O client boss #" + id)));
                        success = true;
                    } finally {
                        if (!success) {
                            // Release the Selector if the execution fails.
                            try {
                                selector.close();
                            } catch (Throwable t) {
                                logger.warn("Failed to close a selector.", t);
                            }
                            this.selector = selector = null;
                            // The method will return to the caller at this point.
                        }
                    }
                } else {
                    // Use the existing selector if this worker has been started.
                    selector = this.selector;
                }

                assert selector != null && selector.isOpen();

                started = true;
                boolean offered = registerTaskQueue.offer(registerTask);
                assert offered;
            }

            if (wakenUp.compareAndSet(false, true)) {
                selector.wakeup();
            }
        }

        public void run() {
            boolean shutdown = false;
            Selector selector = this.selector;
            long lastConnectTimeoutCheckTimeNanos = System.nanoTime();
            for (;;) {
                wakenUp.set(false);

                try {
                    int selectedKeyCount = selector.select(500);

                    // 'wakenUp.compareAndSet(false, true)' is always evaluated
                    // before calling 'selector.wakeup()' to reduce the wake-up
                    // overhead. (Selector.wakeup() is an expensive operation.)
                    //
                    // However, there is a race condition in this approach.
                    // The race condition is triggered when 'wakenUp' is set to
                    // true too early.
                    //
                    // 'wakenUp' is set to true too early if:
                    // 1) Selector is waken up between 'wakenUp.set(false)' and
                    //    'selector.select(...)'. (BAD)
                    // 2) Selector is waken up between 'selector.select(...)' and
                    //    'if (wakenUp.get()) { ... }'. (OK)
                    //
                    // In the first case, 'wakenUp' is set to true and the
                    // following 'selector.select(...)' will wake up immediately.
                    // Until 'wakenUp' is set to false again in the next round,
                    // 'wakenUp.compareAndSet(false, true)' will fail, and therefore
                    // any attempt to wake up the Selector will fail, too, causing
                    // the following 'selector.select(...)' call to block
                    // unnecessarily.
                    //
                    // To fix this problem, we wake up the selector again if wakenUp
                    // is true immediately after selector.select(...).
                    // It is inefficient in that it wakes up the selector for both
                    // the first case (BAD - wake-up required) and the second case
                    // (OK - no wake-up required).

                    if (wakenUp.get()) {
                        selector.wakeup();
                    }

                    processRegisterTaskQueue();

                    if (selectedKeyCount > 0) {
                        processSelectedKeys(selector.selectedKeys());
                    }

                    // Handle connection timeout every 0.5 seconds approximately.
                    long currentTimeNanos = System.nanoTime();
                    if (currentTimeNanos - lastConnectTimeoutCheckTimeNanos >= 500 * 1000000L) {
                        lastConnectTimeoutCheckTimeNanos = currentTimeNanos;
                        processConnectTimeout(selector.keys(), currentTimeNanos);
                    }

                    // Exit the loop when there's nothing to handle.
                    // The shutdown flag is used to delay the shutdown of this
                    // loop to avoid excessive Selector creation when
                    // connection attempts are made in a one-by-one manner
                    // instead of concurrent manner.
                    if (selector.keys().isEmpty()) {
                        if (shutdown ||
                            bossExecutor instanceof ExecutorService && ((ExecutorService) bossExecutor).isShutdown()) {

                            synchronized (startStopLock) {
                                if (registerTaskQueue.isEmpty() && selector.keys().isEmpty()) {
                                    started = false;
                                    try {
                                        selector.close();
                                    } catch (IOException e) {
                                        logger.warn(
                                                "Failed to close a selector.", e);
                                    } finally {
                                        this.selector = null;
                                    }
                                    break;
                                } else {
                                    shutdown = false;
                                }
                            }
                        } else {
                            // Give one more second.
                            shutdown = true;
                        }
                    } else {
                        shutdown = false;
                    }
                } catch (Throwable t) {
                    logger.warn(
                            "Unexpected exception in the selector loop.", t);

                    // Prevent possible consecutive immediate failures.
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignore.
                    }
                }
            }
        }

        private void processRegisterTaskQueue() {
            for (;;) {
                final Runnable task = registerTaskQueue.poll();
                if (task == null) {
                    break;
                }

                task.run();
            }
        }

        private void processSelectedKeys(Set<SelectionKey> selectedKeys) {
            for (Iterator<SelectionKey> i = selectedKeys.iterator(); i.hasNext();) {
                SelectionKey k = i.next();
                i.remove();

                if (!k.isValid()) {
                    close(k);
                    continue;
                }

                if (k.isConnectable()) {
                    connect(k);
                }
            }
        }

        private void processConnectTimeout(Set<SelectionKey> keys, long currentTimeNanos) {
            ConnectException cause = null;
            for (SelectionKey k: keys) {
                if (!k.isValid()) {
                    continue;
                }

                NioClientSocketChannel ch = (NioClientSocketChannel) k.attachment();
                if (ch.connectDeadlineNanos > 0 &&
                        currentTimeNanos >= ch.connectDeadlineNanos) {

                    if (cause == null) {
                        cause = new ConnectException("connection timed out");
                    }

                    ch.connectFuture.setFailure(cause);
                    fireExceptionCaught(ch, cause);
                    ch.worker.close(ch, succeededFuture(ch));
                }
            }
        }

        private void connect(SelectionKey k) {
            NioClientSocketChannel ch = (NioClientSocketChannel) k.attachment();
            try {
                if (ch.socket.finishConnect()) {
                    k.cancel();
                    ch.worker.register(ch, ch.connectFuture);
                }
            } catch (Throwable t) {
                ch.connectFuture.setFailure(t);
                fireExceptionCaught(ch, t);
                ch.worker.close(ch, succeededFuture(ch));
            }
        }

        private void close(SelectionKey k) {
            NioClientSocketChannel ch = (NioClientSocketChannel) k.attachment();
            ch.worker.close(ch, succeededFuture(ch));
        }
    }

    private static final class RegisterTask implements Runnable {
        private final Boss boss;
        private final NioClientSocketChannel channel;

        RegisterTask(Boss boss, NioClientSocketChannel channel) {
            this.boss = boss;
            this.channel = channel;
        }

        public void run() {
            try {
                channel.socket.register(
                        boss.selector, SelectionKey.OP_CONNECT, channel);
            } catch (ClosedChannelException e) {
                channel.worker.close(channel, succeededFuture(channel));
            }

            int connectTimeout = channel.getConfig().getConnectTimeoutMillis();
            if (connectTimeout > 0) {
                channel.connectDeadlineNanos = System.nanoTime() + connectTimeout * 1000000L;
            }
        }
    }
}
