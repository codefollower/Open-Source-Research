代码1:
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(true);
		ssc.socket().setSoTimeout(2000);
		ssc.socket().bind(new InetSocketAddress(8080));
		ssc.socket().accept();

代码2:
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(true);
		ssc.socket().setSoTimeout(2000);
		ssc.socket().bind(new InetSocketAddress(8080));
		ssc.accept();

代码1和代码2只有最后一行有区别，
代码1是用 ServerSocket.accept();
代码2是用 ServerSocketChannel.accept();
只有前者才会出现java.net.SocketTimeoutException
后者是不会出限的，会一直阻塞在accept()里头