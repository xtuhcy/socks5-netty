# netty 学习

## 主要构件

### channel

channel表示一个到实体的开放连接。实体是指硬件设备、文件、网络套接字等。channel是传入和传出数据的载体。（连接——实体——运输数据）

### 回调

我的理解就是

1. A持有B的引用b。
2. 当A发生了某事件时，调用引用b的callback()方法。

### future

future是另一种操作完成通知应用程序的方式。

jdk中的实现：

```
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executors=Executors.newFixedThreadPool(20);
        TaskCallable task=new TaskCallable();
        Future<String> future=executors.submit(task);
        System.out.println(future.isDone());
        String result=future.get();
        System.out.println(future.isDone());
        System.out.println(result);
    }
```

样例输出

```
false
true
这是callable的运行结果
```

上面代码的get将会等待线程运行结束（阻塞）。

netty实现了自己ChannelFuture。用于异步操作（大概意思就是非阻塞的使用）。

ChannelFuture可以注册监听器（Listener），当ChannelFuture发生事件时，会调用监听器Listtener对应的方法。ChannelFuture和ChannelFutureListener提供的通知机制不需要程序员手动检查是否操作情况（例如上述的get、isdone等）。

```
    Channel channel=...;
    ChannelFuture future=channel.connect("127.0.0.1",25);
    future.addListener(new ChannelFutureListener(){
        @Override
        public void operationComplete(ChannelFuture future){//这里有Channelfuture的引用哦
            //todo: 操作完成时的操作
        }
    });
```

### 事件和ChannelHandler（通过回调！）

事件：通知状态的更改或者操作的状态

事件分类——入站相关：

- 连接被激活或者连接失活（从socket的角度看都是从远端接受了数据）
- 数据读取
- 用户事件
- 错误事件

出站事件是某个动作的操作结果

- 打开或者关闭到远程节点的连接（从socket角度看都是向远端发送数据）
- 将数据写到或者冲刷到套接字

每个事件都可以被分发给ChannelHandle中某个用户实现的方法（回调）。

Netty提供了很多开箱即用的ChannelHandler，比如用于Http和Tls的，好像还有用于socks5的。

### channel和EventLoop

在java的NIO中有一个Selector在处理套接字的可读可写事件，并且做相关的处理（派发）。

不同的是，Netty对每一个Channel分配一个EventLoop，用以处理所有事件！包括：

- 注册感兴趣的事件
- 将事件派发给ChannelHandle
- 安排进一步的事件

EventLoop本身由一个线程驱动，处理一个Channel的所有IO事件，并且在这个EventLoop生命周期都不会变化。

## 致谢

感谢作者提供了一个很好的学习 netty socks5 的项目。

之前一直想写一个代理，以了解http原理，最终目的是达到大陆到国外的不可描述的目的。作者的项目是很好的sslocal-加密 的项目。

## 问题描述

拿到这个项目，看了很久之后感觉作者写的很完善，没什么好自己动手修改的地方。

然后就看作者的具体实现。用jdk9带的 java misson control 工具查看了一下项目的内存情况。发现当长时间运行，产生多个与远端的连接时，程序占用的内存非常大，而且使用jmc的full gc工具也无法进行垃圾回收。具体情况可以见附图。

看jmc中的线程信息时，发现：出现了350多个类nioEventLoopGroup-350-1的runnable的线程。

结合学习的netty知识判断和对您源码的阅读，判断是在`Socks5CommandRequestHandler.channelRead0()`方法中的`EventLoopGroup bossGroup=new NioEventLoopGroup();`语句导致的。这句话导致——每次需要代理新建到远程服务器的连接时，都创建了一个NioEventLoopGroup。而且经过上面fullGC的测试，这些NioEventLoopGroup还不能被回收。

## 问题解决

netty实战告诉我，服务器端有两个NioEventLoopGroup，客户端有一个NioEventLoopGroup。

所以我设想，只使用一个NioEventLoopGroup固定的处理所有对远程的channel。

我做出了如下修改
在`Socks5CommandRequestHandler`中增加：

```
    private EventLoopGroup bossGroup;
    public Socks5CommandRequestHandler(ProxyServer proxyServer) {
        bossGroup=proxyServer.getBossGroup();
    }
```

即在ProxyServer中创建一个单独的NioEventLoopGroup，在Socks5CommandRequestHandler，导入这个NioEventLoopGroup,用以管理与远程的连接。

## 效果

在JMC的线程页面中显示只有3个NioEventLoopGroup（编号分别位2、3、4）。当执行full gc时，内存占用降低到13.8M。这个数据非常稳定，13.8M。

## 谢谢

这是第一次在github上提交issue。也是第一次利用自己的知识解决java内存相关的问题。感觉很宝贵。