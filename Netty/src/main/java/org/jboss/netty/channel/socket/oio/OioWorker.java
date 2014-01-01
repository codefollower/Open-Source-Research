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

import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.util.regex.Pattern;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

/**
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2307 $, $Date: 2010-06-16 12:33:29 +0900 (Wed, 16 Jun 2010) $
 *
 */
class OioWorker implements Runnable {
	private static my.Debug DEBUG=new my.Debug(my.Debug.OioWorker);//我加上的

    private static final Pattern SOCKET_CLOSED_MESSAGE = Pattern.compile(
            "^.*(?:Socket.*closed).*$", Pattern.CASE_INSENSITIVE);

    private final OioSocketChannel channel;

    OioWorker(OioSocketChannel channel) {
        this.channel = channel;
    }

    public void run() {
		try {//我加上的
		DEBUG.P(this,"run()");

        channel.workerThread = Thread.currentThread();
        final PushbackInputStream in = channel.getInputStream();

		//是AbstractChannel的isOpen()
        while (channel.isOpen()) {
            synchronized (channel.interestOpsLock) {

				DEBUG.P("channel.isReadable()="+channel.isReadable());
                while (!channel.isReadable()) {
                    try {
                        // notify() is not called at all.
                        // close() and setInterestOps() calls Thread.interrupt()
                        channel.interestOpsLock.wait();
                    } catch (InterruptedException e) {
						e.printStackTrace();
                        if (!channel.isOpen()) {
                            break;
                        }
                    }
                }
            }

            byte[] buf;
            int readBytes;
            try {
                int bytesToRead = in.available();

				DEBUG.P("bytesToRead="+bytesToRead);
                if (bytesToRead > 0) {
                    buf = new byte[bytesToRead];
                    readBytes = in.read(buf);
                } else {
                    int b = in.read();
                    if (b < 0) {
                        break;
                    }
                    in.unread(b);
                    continue;
                }
            } catch (Throwable t) {
                if (!channel.socket.isClosed()) {
                    fireExceptionCaught(channel, t);
                }
                break;
            }

            fireMessageReceived(
                    channel,
                    channel.getConfig().getBufferFactory().getBuffer(buf, 0, readBytes));
        }

        // Setting the workerThread to null will prevent any channel
        // operations from interrupting this thread from now on.
        channel.workerThread = null;

        // Clean up.
        close(channel, succeededFuture(channel));

		}finally{//我加上的
		DEBUG.P(0,this,"run()");
		}
    }

    static void write(
            OioSocketChannel channel, ChannelFuture future,
            Object message) {
		try {//我加上的
		DEBUG.P(OioWorker.class,"write(3)");

        OutputStream out = channel.getOutputStream();
        if (out == null) {
            Exception e = new ClosedChannelException();
            future.setFailure(e);
            fireExceptionCaught(channel, e);
            return;
        }

        try {
            ChannelBuffer a = (ChannelBuffer) message;
            int length = a.readableBytes();
            synchronized (out) {
				//比如在HeapChannelBuffer中把ChannelBuffer中的字节写到OutputStream
				//使用的是OutputStream类的write(byte[] b, int off, int len)
				//这里是阻塞写入
                a.getBytes(a.readerIndex(), out, length);
            }
            fireWriteComplete(channel, length); //上面是阻塞写入，一定是完成了
            future.setSuccess();
        } catch (Throwable t) {
            // Convert 'SocketException: Socket closed' to
            // ClosedChannelException.
            if (t instanceof SocketException &&
                    SOCKET_CLOSED_MESSAGE.matcher(
                            String.valueOf(t.getMessage())).matches()) {
                t = new ClosedChannelException();
            }
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }

		}finally{//我加上的
		DEBUG.P(0,OioWorker.class,"write(3)");
		}
    }

    static void setInterestOps(
            OioSocketChannel channel, ChannelFuture future, int interestOps) {
		try {//我加上的
		DEBUG.P(OioWorker.class,"setInterestOps(3)");

        // Override OP_WRITE flag - a user cannot change this flag.
        interestOps &= ~Channel.OP_WRITE;
        interestOps |= channel.getInterestOps() & Channel.OP_WRITE;

        boolean changed = false;
        try {
            if (channel.getInterestOps() != interestOps) {
                if ((interestOps & Channel.OP_READ) != 0) {
                    channel.setInterestOpsNow(Channel.OP_READ);
                } else {
                    channel.setInterestOpsNow(Channel.OP_NONE);
                }
                changed = true;
            }

            future.setSuccess();
            if (changed) {
                synchronized (channel.interestOpsLock) {
                    channel.setInterestOpsNow(interestOps);

                    // Notify the worker so it stops or continues reading.
                    Thread currentThread = Thread.currentThread();
                    Thread workerThread = channel.workerThread;
                    if (workerThread != null && currentThread != workerThread) {
                        workerThread.interrupt();
                    }
                }

                fireChannelInterestChanged(channel);
            }
        } catch (Throwable t) {
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }

		}finally{//我加上的
		DEBUG.P(0,OioWorker.class,"setInterestOps(3)");
		}
    }

    static void close(OioSocketChannel channel, ChannelFuture future) {
		try {//我加上的
		DEBUG.P(OioWorker.class,"close(2)");

        boolean connected = channel.isConnected();
        boolean bound = channel.isBound();

		DEBUG.P("connected="+connected);
		DEBUG.P("bound="+bound);

        try {
            channel.socket.close();
            if (channel.setClosed()) {
                future.setSuccess();
                if (connected) {
                    // Notify the worker so it stops reading.
                    Thread currentThread = Thread.currentThread();
                    Thread workerThread = channel.workerThread;
                    if (workerThread != null && currentThread != workerThread) {
                        workerThread.interrupt();
                    }
                    fireChannelDisconnected(channel);
                }
                if (bound) {
                    fireChannelUnbound(channel);
                }
                fireChannelClosed(channel);
            } else {
                future.setSuccess();
            }
        } catch (Throwable t) {
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }

		}finally{//我加上的
		DEBUG.P(0,OioWorker.class,"close(2)");
		}
    }
}
