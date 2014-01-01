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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.ReceiveBufferSizePredictor;
import org.jboss.netty.channel.socket.nio.SocketSendBufferPool.SendBuffer;
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
 * @version $Rev: 2307 $, $Date: 2010-06-16 12:33:29 +0900 (Wed, 16 Jun 2010) $
 *
 */
class NioWorker implements Runnable {
	private static my.Debug DEBUG=new my.Debug(my.Debug.NioWorker);//我加上的

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioWorker.class);

    private static final int CONSTRAINT_LEVEL = NioProviderMetadata.CONSTRAINT_LEVEL;

    static final int CLEANUP_INTERVAL = 256; // XXX Hard-coded value, but won't need customization.

    private final int bossId;
    private final int id;
    private final Executor executor;
    private boolean started;
    private volatile Thread thread;
    volatile Selector selector;
    private final AtomicBoolean wakenUp = new AtomicBoolean();
    private final ReadWriteLock selectorGuard = new ReentrantReadWriteLock();
    private final Object startStopLock = new Object();
    private final Queue<Runnable> registerTaskQueue = new LinkedTransferQueue<Runnable>();
    private final Queue<Runnable> writeTaskQueue = new LinkedTransferQueue<Runnable>();
    private volatile int cancelledKeys; // should use AtomicInteger but we just need approximation

    private final SocketReceiveBufferPool recvBufferPool = new SocketReceiveBufferPool();
    private final SocketSendBufferPool sendBufferPool = new SocketSendBufferPool();

    NioWorker(int bossId, int id, Executor executor) {
		try {//我加上的
		DEBUG.P(this,"NioWorker(3)");
		DEBUG.P("bossId="+bossId);
		DEBUG.P("id="+id);

        this.bossId = bossId;
        this.id = id;
        this.executor = executor;

		}finally{//我加上的
		DEBUG.P(0,this,"NioWorker(3)");
		}
    }

	//注册新的NioSocketChannel(两种Socket(accept和普通的类型)都使用
    void register(NioSocketChannel channel, ChannelFuture future) {
		try {//我加上的
		DEBUG.P(this,"register(2)");

        boolean server = !(channel instanceof NioClientSocketChannel);
        Runnable registerTask = new RegisterTask(channel, future, server);
        Selector selector;

		DEBUG.P("server="+server);
		DEBUG.P("started="+started);

        synchronized (startStopLock) {
            if (!started) {
                // Open a selector if this worker didn't start yet.
                try {
                    this.selector = selector = Selector.open();
                } catch (Throwable t) {
                    throw new ChannelException(
                            "Failed to create a selector.", t);
                }

                // Start the worker thread with the new Selector.
                String threadName =
                    (server ? "New I/O server worker #"
                            : "New I/O client worker #") + bossId + '-' + id;


				DEBUG.P("threadName="+threadName);

                boolean success = false;
                try {
                    executor.execute(
                            new IoWorkerRunnable(
                                    new ThreadRenamingRunnable(this, threadName)));
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

		}finally{//我加上的
		DEBUG.P(0,this,"register(2)");
		}
    }

    public void run() {
		try {//我加上的
		DEBUG.P(this,"run()");

        thread = Thread.currentThread();
		DEBUG.P("thread="+thread);
		DEBUG.P("CONSTRAINT_LEVEL="+CONSTRAINT_LEVEL);

        boolean shutdown = false;
        Selector selector = this.selector;
        for (;;) {
            wakenUp.set(false);

            if (CONSTRAINT_LEVEL != 0) {
                selectorGuard.writeLock().lock();
                    // This empty synchronization block prevents the selector
                    // from acquiring its lock.
                selectorGuard.writeLock().unlock();
            }

            try {
                SelectorUtil.select(selector);

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

                cancelledKeys = 0;
                processRegisterTaskQueue();
                processWriteTaskQueue();
                processSelectedKeys(selector.selectedKeys());

                // Exit the loop when there's nothing to handle.
                // The shutdown flag is used to delay the shutdown of this
                // loop to avoid excessive Selector creation when
                // connections are registered in a one-by-one manner instead of
                // concurrent manner.
                if (selector.keys().isEmpty()) {
                    if (shutdown ||
                        executor instanceof ExecutorService && ((ExecutorService) executor).isShutdown()) {

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
                                break; //退出false循环
                            } else {
                                shutdown = false; //只要有一个任务还没处理就不能关闭
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

                // Prevent possible consecutive immediate failures that lead to
                // excessive CPU consumption.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }

		}finally{//我加上的
		DEBUG.P(0,this,"run()");
		}
    }

    private void processRegisterTaskQueue() throws IOException {
        for (;;) {
            final Runnable task = registerTaskQueue.poll();
            if (task == null) {
                break;
            }

            task.run();
            cleanUpCancelledKeys();
        }
    }

    private void processWriteTaskQueue() throws IOException {
        for (;;) {
            final Runnable task = writeTaskQueue.poll();
            if (task == null) {
                break;
            }

            task.run();
            cleanUpCancelledKeys();
        }
    }

    private void processSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException {
        try {//我加上的
		DEBUG.P(this,"processSelectedKeys(1)");

		for (Iterator<SelectionKey> i = selectedKeys.iterator(); i.hasNext();) {
            SelectionKey k = i.next();
            i.remove();
            try {
                int readyOps = k.readyOps();

				DEBUG.P("readyOps="+readyOps);

                if ((readyOps & SelectionKey.OP_READ) != 0 || readyOps == 0) {
                    if (!read(k)) {
                        // Connection already closed - no need to handle write.
                        continue;
                    }
                }
                if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                    writeFromSelectorLoop(k);
                }
            } catch (CancelledKeyException e) {
                close(k);
            }

            if (cleanUpCancelledKeys()) {
                break; // break the loop to avoid ConcurrentModificationException
            }
        }

		}finally{//我加上的
		DEBUG.P(0,this,"processSelectedKeys(1)");
		}
    }

    private boolean cleanUpCancelledKeys() throws IOException {
        if (cancelledKeys >= CLEANUP_INTERVAL) {
            cancelledKeys = 0;
            selector.selectNow();
            return true;
        }
        return false;
    }

    private boolean read(SelectionKey k) {
		try {//我加上的
		DEBUG.P(this,"read(1)");
		

        final SocketChannel ch = (SocketChannel) k.channel();
        final NioSocketChannel channel = (NioSocketChannel) k.attachment();

		DEBUG.P("channel="+channel);

        final ReceiveBufferSizePredictor predictor =
            channel.getConfig().getReceiveBufferSizePredictor();
        final int predictedRecvBufSize = predictor.nextReceiveBufferSize();

        int ret = 0;
        int readBytes = 0;
        boolean failure = true;

        ByteBuffer bb = recvBufferPool.acquire(predictedRecvBufSize);
        try {
            while ((ret = ch.read(bb)) > 0) {
                readBytes += ret;
                if (!bb.hasRemaining()) {
                    break;
                }
            }
            failure = false;
        } catch (ClosedChannelException e) {
            // Can happen, and does not need a user attention.
        } catch (Throwable t) {
            fireExceptionCaught(channel, t);
        }

		DEBUG.P("readBytes="+readBytes);

        if (readBytes > 0) {
            bb.flip();

            final ChannelBufferFactory bufferFactory =
                channel.getConfig().getBufferFactory();
            final ChannelBuffer buffer = bufferFactory.getBuffer(
                    bb.order(bufferFactory.getDefaultOrder()));

            recvBufferPool.release(bb);

            // Update the predictor.
            predictor.previousReceiveBufferSize(readBytes);

            // Fire the event.
            fireMessageReceived(channel, buffer);
        } else {
            recvBufferPool.release(bb);
        }

        if (ret < 0 || failure) {
            close(channel, succeededFuture(channel));
            return false;
        }

        return true;

		}finally{//我加上的
		DEBUG.P(0,this,"read(1)");
		}
    }

    private void close(SelectionKey k) {
        NioSocketChannel ch = (NioSocketChannel) k.attachment();
        close(ch, succeededFuture(ch));
    }

    void writeFromUserCode(final NioSocketChannel channel) {
		try {//我加上的
		DEBUG.P(this,"writeFromUserCode(1)");
		DEBUG.P("(!channel.isConnected())="+(!channel.isConnected()));

        if (!channel.isConnected()) {
            cleanUpWriteBuffer(channel);
            return;
        }

        if (scheduleWriteIfNecessary(channel)) {
            return;
        }

        // From here, we are sure Thread.currentThread() == workerThread.

        if (channel.writeSuspended) {
            return;
        }

        if (channel.inWriteNowLoop) {
            return;
        }

        write0(channel);

		}finally{//我加上的
		DEBUG.P(0,this,"writeFromUserCode(1)");
		}
    }

    void writeFromTaskLoop(final NioSocketChannel ch) {
        if (!ch.writeSuspended) {
            write0(ch);
        }
    }

    void writeFromSelectorLoop(final SelectionKey k) {
        NioSocketChannel ch = (NioSocketChannel) k.attachment();
        ch.writeSuspended = false;
        write0(ch);
    }

    private boolean scheduleWriteIfNecessary(final NioSocketChannel channel) {
        final Thread currentThread = Thread.currentThread();
        final Thread workerThread = thread;
        if (currentThread != workerThread) {
            if (channel.writeTaskInTaskQueue.compareAndSet(false, true)) {
                boolean offered = writeTaskQueue.offer(channel.writeTask);
                assert offered;
            }

            if (!(channel instanceof NioAcceptedSocketChannel) ||
                ((NioAcceptedSocketChannel) channel).bossThread != currentThread) {
                final Selector workerSelector = selector;
                if (workerSelector != null) {
                    if (wakenUp.compareAndSet(false, true)) {
                        workerSelector.wakeup();
                    }
                }
            } else {
                // A write request can be made from an acceptor thread (boss)
                // when a user attempted to write something in:
                //
                //   * channelOpen()
                //   * channelBound()
                //   * channelConnected().
                //
                // In this case, there's no need to wake up the selector because
                // the channel is not even registered yet at this moment.
            }

            return true;
        }

        return false;
    }

    private void write0(NioSocketChannel channel) {
		try {//我加上的
		DEBUG.P(this,"write0(1)");

        boolean open = true;
        boolean addOpWrite = false;
        boolean removeOpWrite = false;

        long writtenBytes = 0;

        final SocketSendBufferPool sendBufferPool = this.sendBufferPool;
        final SocketChannel ch = channel.socket;
        final Queue<MessageEvent> writeBuffer = channel.writeBuffer;
        final int writeSpinCount = channel.getConfig().getWriteSpinCount();
        synchronized (channel.writeLock) {
            channel.inWriteNowLoop = true;
            for (;;) {
                MessageEvent evt = channel.currentWriteEvent;
                SendBuffer buf;
                if (evt == null) {
                    if ((channel.currentWriteEvent = evt = writeBuffer.poll()) == null) {
                        removeOpWrite = true;
                        channel.writeSuspended = false;
                        break;
                    }

                    channel.currentWriteBuffer = buf = sendBufferPool.acquire(evt.getMessage());
                } else {
                    buf = channel.currentWriteBuffer;
                }

                ChannelFuture future = evt.getFuture();
                try {
                    long localWrittenBytes = 0;
                    for (int i = writeSpinCount; i > 0; i --) {
                        localWrittenBytes = buf.transferTo(ch);
                        if (localWrittenBytes != 0) {
                            writtenBytes += localWrittenBytes;
                            break;
                        }
                        if (buf.finished()) {
                            break;
                        }
                    }

                    if (buf.finished()) {
                        // Successful write - proceed to the next message.
                        buf.release();
                        channel.currentWriteEvent = null;
                        channel.currentWriteBuffer = null;
                        evt = null;
                        buf = null;
                        future.setSuccess();
                    } else {
                        // Not written fully - perhaps the kernel buffer is full.
                        addOpWrite = true;
                        channel.writeSuspended = true;

                        if (localWrittenBytes > 0) {
                            // Notify progress listeners if necessary.
                            future.setProgress(
                                    localWrittenBytes,
                                    buf.writtenBytes(), buf.totalBytes());
                        }
                        break;
                    }
                } catch (AsynchronousCloseException e) {
                    // Doesn't need a user attention - ignore.
                } catch (Throwable t) {
                    buf.release();
                    channel.currentWriteEvent = null;
                    channel.currentWriteBuffer = null;
                    buf = null;
                    evt = null;
                    future.setFailure(t);
                    fireExceptionCaught(channel, t);
                    if (t instanceof IOException) {
                        open = false;
                        close(channel, succeededFuture(channel));
                    }
                }
            }
            channel.inWriteNowLoop = false;
        }

        fireWriteComplete(channel, writtenBytes);

        if (open) {
            if (addOpWrite) {
                setOpWrite(channel);
            } else if (removeOpWrite) {
                clearOpWrite(channel);
            }
        }

		}finally{//我加上的
		DEBUG.P(0,this,"write0(1)");
		}
    }

    private void setOpWrite(NioSocketChannel channel) {
        Selector selector = this.selector;
        SelectionKey key = channel.socket.keyFor(selector);
        if (key == null) {
            return;
        }
        if (!key.isValid()) {
            close(key);
            return;
        }
        int interestOps;
        boolean changed = false;

        // interestOps can change at any time and at any thread.
        // Acquire a lock to avoid possible race condition.
        synchronized (channel.interestOpsLock) {
            interestOps = channel.getRawInterestOps();
            if ((interestOps & SelectionKey.OP_WRITE) == 0) {
                interestOps |= SelectionKey.OP_WRITE;
                key.interestOps(interestOps);
                changed = true;
            }
        }

        if (changed) {
            channel.setRawInterestOpsNow(interestOps);
        }
    }

    private void clearOpWrite(NioSocketChannel channel) {
        Selector selector = this.selector;
        SelectionKey key = channel.socket.keyFor(selector);
        if (key == null) {
            return;
        }
        if (!key.isValid()) {
            close(key);
            return;
        }
        int interestOps;
        boolean changed = false;

        // interestOps can change at any time and at any thread.
        // Acquire a lock to avoid possible race condition.
        synchronized (channel.interestOpsLock) {
            interestOps = channel.getRawInterestOps();
            if ((interestOps & SelectionKey.OP_WRITE) != 0) {
                interestOps &= ~SelectionKey.OP_WRITE;
                key.interestOps(interestOps);
                changed = true;
            }
        }

        if (changed) {
            channel.setRawInterestOpsNow(interestOps);
        }
    }

    void close(NioSocketChannel channel, ChannelFuture future) {
		try {//我加上的
		DEBUG.P(this,"close(2)");
		DEBUG.P("channel="+channel);
		DEBUG.P("future="+future);

        boolean connected = channel.isConnected();
        boolean bound = channel.isBound();

		DEBUG.P("connected="+connected);
		DEBUG.P("bound="+bound);

        try {
            channel.socket.close();
            cancelledKeys ++;

            if (channel.setClosed()) {
                future.setSuccess();
                if (connected) {
                    fireChannelDisconnected(channel);
                }
                if (bound) {
                    fireChannelUnbound(channel);
                }

                cleanUpWriteBuffer(channel);
                fireChannelClosed(channel);
            } else {
                future.setSuccess();
            }
        } catch (Throwable t) {
			DEBUG.P("t="+t);

            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"close(2)");
		}
    }

    private void cleanUpWriteBuffer(NioSocketChannel channel) {
        Exception cause = null;
        boolean fireExceptionCaught = false;

        // Clean up the stale messages in the write buffer.
        synchronized (channel.writeLock) {
            MessageEvent evt = channel.currentWriteEvent;
            if (evt != null) {
                // Create the exception only once to avoid the excessive overhead
                // caused by fillStackTrace.
                if (channel.isOpen()) {
                    cause = new NotYetConnectedException();
                } else {
                    cause = new ClosedChannelException();
                }

                ChannelFuture future = evt.getFuture();
                channel.currentWriteBuffer.release();
                channel.currentWriteBuffer = null;
                channel.currentWriteEvent = null;
                evt = null;
                future.setFailure(cause);
                fireExceptionCaught = true;
            }

            Queue<MessageEvent> writeBuffer = channel.writeBuffer;
            if (!writeBuffer.isEmpty()) {
                // Create the exception only once to avoid the excessive overhead
                // caused by fillStackTrace.
                if (cause == null) {
                    if (channel.isOpen()) {
                        cause = new NotYetConnectedException();
                    } else {
                        cause = new ClosedChannelException();
                    }
                }

                for (;;) {
                    evt = writeBuffer.poll();
                    if (evt == null) {
                        break;
                    }
                    evt.getFuture().setFailure(cause);
                    fireExceptionCaught = true;
                }
            }
        }

        if (fireExceptionCaught) {
            fireExceptionCaught(channel, cause);
        }
    }

    void setInterestOps(
            NioSocketChannel channel, ChannelFuture future, int interestOps) {
        boolean changed = false;
        try {
            // interestOps can change at any time and at any thread.
            // Acquire a lock to avoid possible race condition.
            synchronized (channel.interestOpsLock) {
                Selector selector = this.selector;
                SelectionKey key = channel.socket.keyFor(selector);

                if (key == null || selector == null) {
                    // Not registered to the worker yet.
                    // Set the rawInterestOps immediately; RegisterTask will pick it up.
                    channel.setRawInterestOpsNow(interestOps);
                    return;
                }

                // Override OP_WRITE flag - a user cannot change this flag.
                interestOps &= ~Channel.OP_WRITE;
                interestOps |= channel.getRawInterestOps() & Channel.OP_WRITE;

                switch (CONSTRAINT_LEVEL) {
                case 0:
                    if (channel.getRawInterestOps() != interestOps) {
                        key.interestOps(interestOps);
                        if (Thread.currentThread() != thread &&
                            wakenUp.compareAndSet(false, true)) {
                            selector.wakeup();
                        }
                        changed = true;
                    }
                    break;
                case 1:
                case 2:
                    if (channel.getRawInterestOps() != interestOps) {
                        if (Thread.currentThread() == thread) {
                            key.interestOps(interestOps);
                            changed = true;
                        } else {
                            selectorGuard.readLock().lock();
                            try {
                                if (wakenUp.compareAndSet(false, true)) {
                                    selector.wakeup();
                                }
                                key.interestOps(interestOps);
                                changed = true;
                            } finally {
                                selectorGuard.readLock().unlock();
                            }
                        }
                    }
                    break;
                default:
                    throw new Error();
                }
            }

            future.setSuccess();
            if (changed) {
                channel.setRawInterestOpsNow(interestOps);
                fireChannelInterestChanged(channel);
            }
        } catch (CancelledKeyException e) {
            // setInterestOps() was called on a closed channel.
            ClosedChannelException cce = new ClosedChannelException();
            future.setFailure(cce);
            fireExceptionCaught(channel, cce);
        } catch (Throwable t) {
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }
    }

	//当新建立通道时，会生成一个注册任务，
	//通道分两种：NioAcceptedSocketChannel和NioClientSocketChannel
    private final class RegisterTask implements Runnable {
        private final NioSocketChannel channel;

		//对于NioClientSocketChannel，future!=null且server=false
		//对于NioAcceptedSocketChannel，future==null且server=true
        private final ChannelFuture future;
        private final boolean server;

        RegisterTask(
                NioSocketChannel channel, ChannelFuture future, boolean server) {

            this.channel = channel;
            this.future = future;
            this.server = server;
        }

        public void run() {
			try {//我加上的
			DEBUG.P(this,"run()");

            SocketAddress localAddress = channel.getLocalAddress();
            SocketAddress remoteAddress = channel.getRemoteAddress();

			DEBUG.P("localAddress="+localAddress);
			DEBUG.P("remoteAddress="+remoteAddress);

            if (localAddress == null || remoteAddress == null) {

				//当是NioClientSocketChannel时(即server=false)，future不为null
                if (future != null) {
                    future.setFailure(new ClosedChannelException());
                }
                close(channel, succeededFuture(channel));
                return;
            }

            try {
				DEBUG.P("server="+server);
				DEBUG.P("channel.getRawInterestOps()="+channel.getRawInterestOps());

				//NioClientSocketChannel的socket在调用NioClientSocketChannel.newSocket()时
				//已设过configureBlocking(false)了，
				//而NioAcceptedSocketChannel没有设，所以在这里设
                if (server) {
                    channel.socket.configureBlocking(false);
                }

                synchronized (channel.interestOpsLock) {
					//selector是NioWorker的,
					//channel当成附件
                    channel.socket.register(
                            selector, channel.getRawInterestOps(), channel);
                }

				DEBUG.P("future="+future);
                if (future != null) {
                    channel.setConnected();
                    future.setSuccess();
                }
            } catch (IOException e) {
                if (future != null) {
                    future.setFailure(e);
                }
                close(channel, succeededFuture(channel));
                if (!(e instanceof ClosedChannelException)) {
                    throw new ChannelException(
                            "Failed to register a socket to the selector.", e);
                }
            }

			DEBUG.P("server="+server);
            if (!server) {

				DEBUG.P("((NioClientSocketChannel) channel).boundManually="+((NioClientSocketChannel) channel).boundManually);

                if (!((NioClientSocketChannel) channel).boundManually) {
                    fireChannelBound(channel, localAddress);
                }
                fireChannelConnected(channel, remoteAddress);
            }

			}finally{//我加上的
			DEBUG.P(0,this,"run()");
			}
        }
    }
}
