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

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * Sends a sequence of integers to a {@link FactorialServer} to calculate
 * the factorial of the specified integer.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class FactorialClient {

	static void nextInt(java.util.concurrent.atomic.AtomicInteger a) {
		int next = a.getAndIncrement();
		System.err.println(next);
        System.err.println(Math.abs(next % 100));
    }

    public static void main(String[] args) throws Exception {
		/*
		java.util.concurrent.atomic.AtomicInteger a = new java.util.concurrent.atomic.AtomicInteger(Integer.MAX_VALUE-1);
		for(int i=0; i<5;i++)
			System.err.println(a.incrementAndGet());//nextInt(a);//System.err.println(a.getAndIncrement());

		System.exit(-1);
		*/

        // Print usage if no argument is specified.
        if (args.length != 3) {
            System.err.println(
                    "Usage: " + FactorialClient.class.getSimpleName() +
                    " <host> <port> <count>");
            return;
        }

        // Parse options.
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int count = Integer.parseInt(args[2]);
        if (count <= 0) {
            throw new IllegalArgumentException("count must be a positive integer.");
        }

		ClientBootstrap bootstrap = null;

		boolean nio = true;
		//boolean nio = false;

		if(nio) {

        // Configure the client.
			bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
		} else {

			bootstrap = new ClientBootstrap(
                new org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory(
                        Executors.newCachedThreadPool()));
		}

		//bind定到一个固定的9999端口
		bootstrap.setOption("localAddress", new InetSocketAddress(9999));


        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new FactorialClientPipelineFactory(count));

		bootstrap.setOption("remoteAddress", new InetSocketAddress(host, port));
        ChannelFuture connectFuture = bootstrap.connect();

        // Make a new connection.
		//ChannelFuture connectFuture =
        //    bootstrap.connect(new InetSocketAddress(host, port));

		System.out.println("FactorialClient: connectFuture ="+connectFuture);
		System.out.println("FactorialClient: awaitUninterruptibly 111");

        // Wait until the connection is made successfully.
        Channel channel = connectFuture.awaitUninterruptibly().getChannel();

		System.out.println("FactorialClient: awaitUninterruptibly 222");

        // Get the handler instance to retrieve the answer.
        FactorialClientHandler handler =
            (FactorialClientHandler) channel.getPipeline().getLast();

		System.out.println("FactorialClient: awaitUninterruptibly 333");

        // Print out the answer.
       // System.err.format(
       //         "Factorial of %,d is: %,d", count, handler.getFactorial());

		System.out.println("FactorialClient: awaitUninterruptibly 444");

        // Shut down all thread pools to exit.
        //bootstrap.releaseExternalResources();
    }
}
