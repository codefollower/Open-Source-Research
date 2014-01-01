Netty即可以做为Http客户端，也可以做为Http服务器端

做为Http客户端时，只要关心一种Channel:
org.jboss.netty.channel.socket.nio.NioClientSocketChannel

做为Http服务器端时，要关心两种Channel:
org.jboss.netty.channel.socket.nio.NioServerSocketChannel
org.jboss.netty.channel.socket.nio.NioAcceptedSocketChannel

1. 做为Http客户端时


Netty有两种线程: Boss线程 和 NioWorker线程
Boss线程是非阻塞的，只有一个Boss线程，可以处理多个NioClientSocketChannel，用来对NioClientSocketChannel进行connect操作，
Boss线程会被激活多次，如果不用建立新的Channel时，此时Boss线程空闲了，就会退出，
当有新的Channel需要连接到服务器端时，重新运行Boss线程。

Channel连接成功后，Boss线程会把NioClientSocketChannel注册给NioWorker线程，放到NioWorker线程的registerTaskQueue。

NioWorker线程也是非阻塞的，也可以处理多个NioClientSocketChannel，
另外，当Netty做为Http服务器时，NioWorker线程还可以处理多个NioAcceptedSocketChannel。

也就是说org.jboss.netty.channel.socket.nio.NioWorker这个类是其用的，
做为Http客户端时，NioWorker线程管理NioClientSocketChannel,
做为Http服务器时，NioWorker线程管理NioAcceptedSocketChannel。

这会造成NioWorker这个类的代码有点难理解。



2. 做为Http服务器时

Netty也有两种线程: Boss线程 和 NioWorker线程

此时的Boss线程跟前面的是不一样的，这个Boss线程是非阻塞的，不会激活多次，只有一个Boss线程,
Boss线程一直在运行，用来处理NioServerSocketChannel，当接收到新来的Socket时，构造成NioAcceptedSocketChannel，
然后转交给NioWorker线程处理。

