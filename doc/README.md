##  RPC styles
  * request-response
  * streaming
  * pipelining(HTTP pipelining, Redis pipelining)
  * authentication
  * transactions

## Client Features

  * Connection Pooling
  * Load Balancing
  * Failure Detection
  * Failover/Retry
  * Distributed Tracing (à la Dapper)
  * Service Discovery (e.g., via Zookeeper)
  * Rich Statistics
  * Native OpenSSL Bindings
  * Top


##　Server Features

  * Backpressure (to defend against abusive clients)
  * Service Registration (e.g., via Zookeeper)
  * Distributed Tracing
  * Native OpenSSL bindings

## Supported Protocols

  * HTTP
  * HTTP streaming (Comet)
  * Thrift
  * Memcached/Kestrel
  * More to come!

## Finagle objects
  * Future objects enable asynchronous operations required by a service
  * Service objects perform the work associated with a remote procedure call
  * Filter objects enable you to transform data or act on messages before or after the data or messages are processed by a service
  * Codec objects decode messages in a specific protocol before they are handled by a service and encode messages before they are transported to a client or server.

        CodecFactory
  * Servers

        https://github.com/twitter/finagle/raw/master/doc/Filters.png
    ServerBuilder
    general attributes:

    * codec:  Object to handle encoding and decoding of the service's request/response protocol
    * statsReceiver: Statistics receiver object, which enables logging of important events and statistics
    * name: Name of the service
    * bindTo: The IP host:port pairs on which to listen for requests; localhost is assumed if the host is not specified
    * logger: Logger object

    handle fault tolerance and manage clients:

    * maxConcurrentRequests: Maximum number of requests that can be handled concurrently by the server
    * hostConnectionMaxIdleTime: Maximum time that this server can be idle before the connection is closed
    * hostConnectionMaxLifeTime: Maximum time that this server can be connected before the connection is closed
    * requestTimeout: Maximum time to complete a request
    * readTimeout: Maximum time to wait for the first byte to be read
    * writeCompletionTimeout: Maximum time to wait for notification of write completion from a client

    manage TCP connections:
    * sendBufferSize: Requested TCP buffer size for responses
    * recvBufferSize: Actual TCP buffer size for requests

    * tls: The kind of transport layer security
    * channelFactory: Channel service factory object
    * traceReceiver: Trace receiver object
  * Clients
    * Your code should separate building the client from invocation of the client,
    * subsequent execution of the client does not require rebuilding
    * Finagle will retry the request in the event of an error, up to the number of times specified
    * Finagle does not assume your RPC service is Idempotent.Retries occur only when the request is known to be idempotent, such as in the event of TCP-related WriteException errors, for which the RPC has not been transmitted to the remote server.
    * A robust way to use RPC clients is to have an upper-bound on how long to wait for a response to arrive

    With Future objects, you can

       * block, waiting for a response to arrive and throw an exception if it does not arrive in time.
       * register a callback to handle the result if it arrives in time, and register another callback to invoke if the result does not arrive in time

     ClientBuilder general attributes:

       * name: Name of the service
       * codec: Object to handle encoding and decoding of the service's request/response protocol
       * statsReceiver: Statistics receiver object, which enables logging of important events and statistics
       * loadStatistics: How often to load statistics from the server
       * logger: A Logger object with which to log Finagle messages
       * retries: Number of tries (not retries) per request (only applies to recoverable errors)

     manage the host connection:

       * connectionTimeout: Time allowed to establish a connection
       * requestTimeout: Request timeout
       * hostConnectionLimit: Number of connections allowed from this client to the host
       * hostConnectionCoresize: Host connection's cache allocation
       * hostConnectionIdleTime:
       * hostConnectionMaxWaiters: The maximum number of queued requests awaiting a connection
       * hostConnectionMaxIdleTime: Maximum time that the client can be idle until the connection is closed
       * hostConnectionMaxLifeTime: Maximum time that client can be connected before the connection is closed

     manage TCP connections:

       * cluster: The cluster connections associated with the client
       * channelFactory: Channel factory associated with this client
       * tls: The kind of transport layer security

  If you are using stateful protocols, such as those used for transaction processing or authentication, you should call `buildFactory`


## Threading Model
The Finagle threading model requires that you avoid blocking operations in the Finagle event loop

Blocking events include but are not limited to

   * network calls
   * system calls
   * database calls

In complex RPC operations, it may be necessary to perform blocking operations. In these cases, you must set up your own thread pool and use Future or FuturePool objects to execute the blocking operation on your own thread.

## Starting and Stopping Servers
A server automatically starts when you call build on the server after assigning the IP address on which it runs
To stop a server, call its close method.The server will immediately stop accepting requests
the server will continue to process outstanding requests until all have been handled or until a specific duration has elapsed
 You specify the duration when you call close
 You are responsible for releasing all resources when the server is no longer needed.


##  Using Future Objects
In the simplest case, you can use `Future` to block for a request to complete

    // Issue a request, get a response:
    val request: HttpRequest = new DefaultHttpRequest(HTTP_1_1, GET, "/")
    val responseFuture: Future[HttpResponse] = client(request)

### Future Callbacks
In cases where you want to continue execution immediately, you can specify a callback. The callback is identified by the `onSuccess` keyword:

    val request: HttpRequest = new DefaultHttpRequest(HTTP_1_1, GET, "/")
    val responseFuture: Future[HttpResponse] = client(request)
    responseFuture onSuccess { responseFuture =>
      println(responseFuture)
    }

### Future Timeouts
In cases where you want to continue execution after some amount of elapsed time, you can specify the length of time to wait in the `Future` object.

    val request: HttpRequest = new DefaultHttpRequest(HTTP_1_1, GET, "/")
    val responseFuture: Future[HttpResponse] = client(request)
    println(responseFuture(1.second))

You use the `within` method of Future to specify how long to wait for the response. Finagle also creates a Timer thread on which to wait until one of the conditions are satisfied.

    import com.twitter.finagle.util.Timer._
    ...
    val request: HttpRequest = new DefaultHttpRequest(HTTP_1_1, GET, "/")
    val responseFuture: Future[HttpResponse] = client(request)
    responseFuture.within(1.second) onSuccess { response =>
      println("responseFuture)
    } onFailure {
      case e: TimeoutException => ...
    }

    // within需要一个隐式的Timer，怎么传入？


### Future Exceptions
创建一个异常，在try语句块中指定操作，在catch语句块中处理失败。这里是一个处理`Future`超时异常的一个例子：

    val request: HttpRequest = new DefaultHttpRequest(HTTP_1_1, GET, "/")
    val responseFuture: Future[HttpResponse] = client(request)
    try {
      println(responseFuture(1.second))
    } catch {
      case e: TimeoutException => ...
    }
在这个例子中，1秒过后，要么显示HTTP响应，要么抛出一个`TimeoutException`异常。

### Promises
Promise是Future的一个子类。尽管一个`Future`只能被读取，一个`Promise`既能被读取，也能写入。通常，生产者创建一个`Promise`，在传送给消费者之前将他转换为一个
`Future`。下面的例子显示了在你需要创建一个`Future`的服务，但是需要预测错误时，`Promise`是多么有用。

    def make() = {
    ...
    val promise = new Promise[Service[Req, Rep]]
    ... {
      case Ok(myObject) =>
        ...
        promise() = myConfiguredObject
      case Error(cause) =>
        promise() = Throw(new ...Exception(cause))
      case Cancelled =>
        promise() = Throw(new WriteException(new ...Exception))
      }
      promise
    }

并不鼓励自己创建自己的Promise。可代替的，如果需要，使用`Future`组合器来组合操作（下面会说到）

### 使用Future的map和flatMap操作
除了等待结果返回之外，`Future`可以有些有趣的转换。举个例子，他可以使用`map`将一个`Future[String]`转换为`Future[Int]`

    val stringFuture : Future[String] = Future("1")
    val intFuture : Future[Int] = stringFuture map (_.toInt)

与`map`类似的，你可以使用`flatMap`很简单地传递一串`Futures`

    val authenticateUser : Future[User] = User.authenticate(email, password)
    val lookupTweets : Future[Seq[Tweet]] = authenticateUser flatMap {user =>
        Tweet.findAllByUser(user)
    }
在这个例子中，`Tweet.findAllByUser(user)`是`User => Future[Seq[Tweet]]`类型的函数。

### 使用Future用Scatter/Gather模式
对于Scatter/Gather模式，挑战是如何解决一系列并行请求，等待他们所有都返回。为了等待一串`Future`对象返回，你可以定义一个队列来保存这个对象，并且使用`Future.collect`方法来等待他们。如下：

    val myFutures : Seq[Future[Int]] = ...
    val waitTillAllComplete : Future[Seq[Int]] = Future.collect(myFutures)

Scatter/Gather模式的一个复杂的变化是：执行一些异步的操作，只处理那些在指定时间返回的，忽略那些在指定时间没有返回的。举个例子，你可能想要将一些并行的请求转化为查询索引的N部分，那些没有及时返回的将设为空。
下面的例子显示1秒的查询时间：

    import com.twitter.finagle.util.Timer._

    val timedResults : Seq[Future[Result]] = partitions.map {partition =>
        partition.get(query).within(1.second) handle {
            case  _: TimeoutException => EmptyResult
        }
    }

    val allResults : Future[Seq[Result]] = Future.collect(timedResult

    allResults onSuccess { results =>
        println(results)
    }

### 使用Future池
`FuturePool`对象可以让你将阻塞放在你自己的线程中执行。下面的例子中，一个Service的`apply`方法在Finagle的eventloop中执行，创建`FuturePool`对象，将阻塞操作放在与`FuturePool`对象关联的一个线程中。`apply`方法立刻返回，不会阻塞。

    class ThriftFileReader extends Service[String, Array[Byte]] {
        val diskToFuturePool = FuturePool(Executors.newFixedThreadPool(4)

        def apply(path : String) = {
            def blockingOperation = {
                scala.Source.fromFile(path) // potential to block
            }

            // give this BlockingOperation to the future pool to execute
            diskToFuturePool(blockingOperation)
            // return immediately while the future pool execute the operation on a different thread
        }
    }

## 创建Service
下面的例子继承了`Service`类来处理一个HTTP请求

    class Respond extends Service[HttpRequest, HttpResponse] {
        def apply(request : HttpRequest) = {
            val response = new DefaultHttpResponse(HTTP_1_1, OK)
            response.setContent(copiedBuffer(myContent, UTF_8))
            Future.value(response)
        }
    }

## 创建简单的Filters
下面的例子继承`SimpleFilter`类，如果authorization头包含了与指定的字符串不同的值就抛出一个异常。

    class Authorize extends SimpleFilter[HttpRequest, HttpResponse] {
        def apply(request : HttpRequest, continue : Service[HttpRequest, HttpResponse]) = {
            if ("shared secret" == request.getHeader("Authorization")) {
                continue(request)
            } else {
                Future.exception(new IllegalArgumentException("You don't know the secret"))
            }
        }
    }
下面的例子继承了`SimpleFilter`类，如果发生了错误，则设置HTTP请求码并且将错误信息和堆栈跟踪信息返回给response

    class HandleExceptions extends SimpleFilter[HttpRequest, HttpResponse] {
        def apply(request : HttpRequest, service : Service[HttpRequest, HttpResponse]) = {
            service(request) handle { case error =>
                val statusCode = error match {
                    case _: IllegalArgumentException => FORBIDDEN
                    case _ => INTERNAL_SERVICE_ERROR
                }

                val errorResponse = new DefaultHttpResponse(HTTP_1_1, statusCode)
                errorResponse.setContent(copiedBuffer(error.getStackTraceString, UTF_8))

                errorResponse
            }
        }
    }

## 创建一个健壮的服务端
下面的例子使用了之前例子的filters和service，在service之后定义了filters的执行顺序。`ServerBuilder`对象指定了执行顺序，解码器和绑定的IP地址。

    object HttpServer {
        class HandleExceptions extends SimpleFilter[HttpRequest, HttpResponse] {...}
        class Authorize extends SimpleFilter[HttpRequest, HttpResponse] {...}
        class Respond extends Service[HttpRequest, HttpResponse] {...}

        def main(args : Array[String]) {
            val handleExceptions = new HandleExceptions
            val authorize = new Authorize
            val respond = new Respond

            val myService : Service[HttpRequest, HttpResponse] = handleExceptions andThen authorize andThen respond

            val server : Server = ServerBuilder()
                .name("myService")
                .codec(Http())
                .bindTo(new InetSocketAddress(8080))
                .build(myService)
        }
    }
在这个例子中，`handleException`filter是在`authorize` filter之前执行的。所有的filter在service之前执行。服务是健壮的不是因为它的复杂性。
而是因为它在执行service之前使用了filters来移除问题。

## 创建一个健壮的客户端
一个健壮的客户端不需要写什么代码（SLOC)，然而，健壮性取决于你是怎么配置客户端和对它做的测试。看看下面的HTTP客户端：

    val client = ClientBuilder()
        .codec(Http())
        .hosts("localhost:10000,localhost:10001,localhost:10003")
        .hostConnectionLimit(1)   // max number of connections at a time to a host
        .connectionTimeout(1.second) // max time to spend establishing a TCP connection
        .retries(2)  // (1) per-request retries
        .reportTo(new OstrichStatsReceiver) // export host-level load data to ostrich
        .logger(Logger.getLogger("http"))
        .build()
`ClientBuilder`对象创建并配置了一个负载均衡的HTTP客户端，它在本地3个终端之间均衡。Finagle的负载均衡策略是挑选最少未完成请求数目的终端，
它与其他负载均衡器的最少连接数策略类似。The Finagle load balancer deliberately introduces jitter to avoid synchronicity (and thundering herds) in a distributed system
它也支持故障转移。
下面的例子显示怎么分别用Scala和Java执行这个客户端：

    // Scala Client Invocation
    val request : HttpRequest = new DefaultHttpRequest(HTTP_1_1, GET, "/")
    val futureResponse : Future[HttpResponse] = client(request)

    // Java Client Invocation
    HttpRequest request = new DefaultHttpRequest(HTTP_1_1, GET, "/")
    Future<HttpResponse> futureResponse = client.apply(request)

## 创建Filters来改变请求和响应
下面的例子继承了`Filter`类来验证请求，请求被转变成HTTP响应在被`AuthResult`处理之前。在这种情况下，`RequireAuthentication`没有转换HTTP响应。

    class RequireAuthentication(val p : ...) extends Filter[Request, HttpResponse, AuthenticationRequest,
    HttpResponse] {
        def apply(request : Request, service : Service[AuthenticationRequest, HttpResponse]) = {
            p.authenticate(request) flatMap {
                case AuthResult(AuthResultCode.OK, Some(passport : OAuthPassport), _, _) =>
                    service(AuthenticatedRequest(request, passport)
                case AuthResult(AuthResultCode.OK, Some(passport : SessionPassport), _, _) =>
                    service(AuthenticatedRequest(request, passport))
                case ar: AuthResult =>
                    Trace.record("Authentication failed with " + ar)
                    Future.exception(new RequestUnauthenticated(ar.resultCode))
            }
        }
    }
在这个例子中，`flatMap`方法允许一个请求管道。

## 使用ServerSet对象
`finagle-serversets`是Finagle Cluster接口的一个实现，用`com.twitter.com.zookeeper`[ServerSets](http://twitter.github.io/commons/apidocs/#com.twitter.common.zookeeper.ServerSet)
可以用下面的方法实例化一个`ServerSet`对象：

    val serverset = new ServerSetImpl(zookeeperClient, "/twitter/services/...")
    val cluster = new ZookeeperServerSetCluster(serverset)

Servers被加入到Cluster，用下面的方法：

    val serviceAddress = new InetSocketAddress(...)
    val server = ServerBuilder()
        .bindTo(serviceAddress)
        .build()
客户端可以访问集群，如下：

    val client = ClientBuilder()
        .cluster(cluster)
        .hostConnectionLimit(1)
        .codec(new StringCodec)
        .build()

## 配置Finagle服务端和客户端
Finagle提供了大量的对服务端和客户读的配置选项。一些Finagle用户，默认的是合理的，足够的，这部分是不必要的。服务端和客户端需要很少配置参数。
### 使用ServerBuilder和ClientBuilder
客户端是如下的方式定义的：

    val client : Service[Req, Resp] = ClientBuilder()
        .configParam1(val1)
        .configParam2(val2)
        ...
        .build()
每个`configParam`被一个值（`val`)初始化；这些参数是按顺序初始化的，从上面到下面。从概念上说，builder就想是一个不可变的map。如果`configParam1`和
`configParam2`是互斥的，后面那个会覆盖前面那个。最后，`build`方法被调用，没有参数，实际上生成了客户端，这是唯一的没有副作用的操作。

服务端看起来像下面的：

    val server : Server = ServerBuilder()
        .configParam1(val1)
        .configParam2(val2)
        ...
        .build(myService)
不像ClientBuilder，ServerBuilder的`build`方法需要一个参数，一个用来连接客户端的service。
这些builders是不可变的，持久性的；这样是有好处的，允许构造模板builders包含了一些参数。这是有用的，并且是一个通用的设计模式。

### ClientBuilder需要的参数
ClientBuilder有2个主要的抽象，第一它是由client, hosts和connections的三人组。一个客户端可以连接一个或多个主机，指定一个将请求发送到那些主机的策略。
每个主机会允许一个或多个单独的连接，在请求中并发地暴露从它连接的客户端，允许并行的执行。
第二个抽象是codec,它是用来负责将分离的请求或响应转换成字节流，用来在网络中发送，反过来也一样。

这些概念很重要，因为当你指定任何客户端都需要它们。

ClientBuilder需要定义`cluster`或`hosts`, `codec`和'hostConnectionLimit'。在Scala中，他们是静态类型检查的，在Java中，缺少他们的任何一个都会报运行时异常。

可选的，在java中用ClientBuilder可以用`ClientBuilder.safeBuild()`来做静态类型检查。
   * `hosts`必须包含一个hosts列表或者用`cluster`来明确指出用集群。
   * `codec`实现了客户端使用的网络协议，因此决定了请求和响应的类型。
   * `hostConnectionLimit`指定了连接到每个host所创建的最大连接数量。

如果你没有指定这些，而且使用的是Scala，你会看到一个错误信息：`Builder is not fully configured`，加上不完整的请求配置的详细信息。

### ServerBuilder需要的参数
`ServerBuilder`有三个必须的参数：`codec`, 服务的名称（一个字符串）（被`name`调用）和一个地址（通常是`InetSocketAddress(serverPort)`（被`bindTo`调用）。
跟客户端一样，在Scala中，这些是静态类型检查的；java可以使用`ServerBuilder.safeBuild()`来静态类型检查。否则在Java中缺少它们的任何一个都会报运行时异常。

### Clusters
集群的目的是抽象一组相同的服务器，请求可以被路由到集群中的任意服务器。注意集群有动态的关系。回顾下前面提到的：
>The Finagle balancing strategy is to pick the endpoint with the least number of outstanding requests, which is similar to a least connections strategy in other load balancers. The Finagle load balancer deliberately introduces jitter to avoid synchronicity (and thundering herds) in a distributed system. It also supports failover.

### Idle Times
>`hostConnectionIdleTime`对比`hostConnectionMaxIdleTime`：它们有什么区别？

`hostConnectionIdleTime`提供给缓存池：“连接在被关闭之前保留的最大时间（不被连接池关闭）”。更准确地说，它是提供给在低和高水印之间的任意连接。
`hostConnectionMaxIdleTime`提供给真实的连接，“一个连接不被使用保留的最长时间”

### Timeouts
在连接运行的时候客户端有多种超时参数，包括如下步骤：

   1. 创建一个请求
   2. 请求负载均衡器得到要连接哪个（负载均衡器选择最少连接的终端）
   3. 请求终端连接池得到一个连接。大部分时间，在连接池中存在一个未使用的连接，并且返回这个连接。否则，申请一个连接，或者依赖连接池策略，请求可能会被排队。
一个排队的请求可能会得到一个返回连接池的连接，或者根据连接池策略可能会创建一个连接。
   4. 当一个连接建立了，建立了一个socket和connection
   5. 将请求分发给指定的连接
   6. 在得到一个回复之前一直等待，得到`Future`
   7. 连接返回到连接池中。

可配置的超时参数如下：

   * `connectionTimeout` -- 获得一个连接的总时间，无论是获得一个真实的连接，还是在队列中等待可用连接。（1-3）
   * `tcpConnectionTimeout` -- TCP层的连接超时，与Netty的`connectTimeoutMillis`和java的`java.net.Socket.connect(SocketAddress,
   int)`方法的第2个参数相同。它是指等待一个可用socket连接并且成功的总时间。默认情况下，这个值为10毫秒，这个对远处的服务器来说是不够的。（4）
   * `requestTimeout` -- 每个请求超时时间，意味着每个重试过程会消耗这么多时间。这个时间是从连接建立时就开始计时的。（5-6）
   * `timeout` -- 高层的超时时间，从请求的发起（通过`service(request)`)到得到返回结果。没有请求可以超过这个时间。（1-6）

![2](https://raw.github.com/twitter/finagle/master/doc/request-timeline.png)

### 配置连接
Finagle为客户端管理了一个连接池。到服务端的连接的创建是要很大的代价的，所以当Finagle为一个特定的请求创建了一个连接，即使这个连接完成了它仍然会管理这个连接。
然后其他的请求可以复用这个连接。对连接池的管理是Finagle处理的，不过可以配置一些连接池的参数。

在连接池中的连接是怎么工作的？下面说了几点可以设置的参数：

   * 当客户端建立时，没有连接被建立。
   * 当发送第一个请求的时候，它会建立一个连接，并且将它返回给请求。
   * 当这个请求结束时，请求会释放这个连接到连接池中。 `hostConnectionCoresize`设置连接池的大小，连接池管理着每个host的连接数。
   * 如果有大于`hostConnectionCoresize`个未完成的请求，根据`hostConnectionLimit`的值，新的连接会被建立。等那些请求完成后，他们会被释放到cachingPool,
   在`hostConnectionIdleTime`值的周围。在`hostConnectionIdleTime`的时间内，如果有新的请求连接，它会重用这个连接。
   * 任何连接层度的错误（写异常或者超时）会使得这个连接不可用，这个连接会立刻被丢弃。

设置`hostConnectionLimit`指定了每个host所允许的最大连接数;Finagle保证不会有比这个值更大的存活连接。`hostConnectionCoresize`设置最小的连接，
连接池不会有比这个值更小的连接数，除非它们在空时间时间内超时了。

如果设置那2个值相等，结果是Finagle对每个host的连接不会创建比这个值更多的值，它也不会放弃正常的连接。但是，这不意味这你总能看到相同数目的连接。要得到这些连接，
需要去请求。Finagle不会主动创建连接（当client建立时，没有连接会被创建）。当一个新的请求被派发，会发生下面的代码：

    if num(active connections) == max, enqueue it
    otherwise establish a new connection and dispatch it

当一个请求完成了，连接重新放入到连接池，如果这个连接是正常的（它没有被服务端关闭）。

有一些其他的参数可以设置，如果客户端设置了idle timeouts,请求会保留，如果在参数设置的时间内是空闲的。除非连接数达到了每个host的最大值，请求不会排队。

另外一个有用的参数是`hostConnectionMaxWaiters`,它可以限制等待的最大数，当等待的请求达到这个值时，`Future.exception(TooManyWaitersException)`会发生。

`expHostConnectionBufferSize(n)`的目标是通过一个快速，自由锁的buffer减少锁竞争，这里的`n`应该设置为期望同时的未完成请求的数量（把n设置为高点，比低点更好，最好是设置为2
的倍数。），这个参数只是实验性的，最后会被集成到主要的代码中。(作为`hostConnectionBufferSize`)

Finagle也提供了一些有用的统计信息用来监控连接池，负载均衡器以及队列的状态。这些可以用来确认如何确认N个连接数中，这个N的值。

### maxConcurrentRequests
`maxConcurrentRequests`是用来告诉Finagle，你的服务端实现同一时间可以并发处理的请求数。如果超过这个值，Finagle会将请求放到一个无界的队列中等待它们返回。
注意，`maxConcurrentRequests`不会拒绝请求，因为使用了无界的队列。但是也是有可能的，因为它上游的超时和取消。

### Retries
`ClientBuilder`允许指定一个`retryPolicy`，或者一个`retries`的值。他们是互斥的，能够各自相互覆盖，如果定义了两者，后面那个会覆盖前面那个。

"retries"意思是"tries"; `retry = 1`意味着只尝试一次，并没有重试，`retry = 2`意思是尝试一次，重试一次，等等。

### Debugging
调试的一个技巧是添加一个日志：

    ServerBuilder() // or ClientBuilder
    ...
    .logger(java.util.logging.Logger.getLogger("debug"))
    ...

当为服务端记录日志时，获取到对任何请求客户端的访问信息是有用的。ClientId组合对象提供了`.current`方法，返回client的id，表示是从哪个客户端请求发来的。
文档（`finagle/finagle-thrift/src/main/scala/com/twitter/finagle/thrift/authentication.scala`）提到：
>[ClientID] is set at the beginning of the request and is available throughout the life-cycle of the request. It is [available] iff the client has an upgraded finagle connection and has chosen to specify the client ID in its codec.

日志只是用来调试用的，日志有严重的性能影响。

## Finagle的Java设计模式
用Java来实现RPC服务端和客户端与Scala是类似的。可以写Java Service用命令行模式，它是Java语言的传统模式，也可以使用函数模式，Java和Scala的主要不同是异常处理。

### 用Java使用Future对象
在Java中定义`Future`对象使用`Future<Type>`

    Future<String> future = executor.schedule(job);
`Future`类是定义在`com.twitter.util.Future`中的，不是Java的类。

可以使用`Future`对象的`get`方法来得到`Future`对象的内容。

    // Wait indefinitely for result
    String result = future.get();

调用`get`方法是更通用的模式，因为这样可以简单的处理异常，查看[Handling Synchronous Responses With Exception Handling](https://github.com/twitter/finagle#Handling%20Synchronous%20Responses%20With%20Exception%20Handling)
获取更多信息。

也可以调用`Future`对象的`apply`方法，它的参数是函数：

    // Wait up to 1 second for result
    String result = future.apply(Duration.apply(1, SECOND));
这个技术适合用在异常捕获不是一个问题。

### java命令行模式
下面的例子显示了命令行模式，使用了一个event listener，用来响应`Future`对象的不同方法。

    Future<String> future = executor.schedule(job);
    future.addEventListener(
      new FutureEventListener<String>() {
        public void onSuccess(String value) {
          println(value);
        }
        public void onFailure(Throwable t) ...
      }
    )

### Java函数式
下面的例子显示了函数式写法，与Scala的写法类似：

    import scala.runtime.BoxedUnit;
    Future<String> future = executor.schedule(job);
      future.onSuccess( new Function<String, BoxedUnit>() {
        public BoxedUnit apply(String value) {
          System.out.println(value);
          return BoxedUnit.UNIT;
        }
      }).onFailure(...).ensure(...);

下面的例子显示了`map`方法的函数式写法：

    Future<String> future = executor.schedule(job);
    Future<Integer> result = future.map( new Function<String, Integer>() {
      public Integer apply(String value) { return Integer.valueOf(value); }
    });

## 用Java创建一个服务器
可以用Java来创建服务端，有不少的选项。可以创建一个服务端，同步或异步处理请求，也可以创建异常处理的不同等级。在所有情况下，一个`Future`或异常会返回。

### Server Imports

    import java.net.InetSocketAddress;

    import org.jboss.netty.buffer.ChannelBuffers;
    import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
    import org.jboss.netty.handler.codec.http.HttpRequest;
    import org.jboss.netty.handler.codec.http.HttpResponse;
    import org.jboss.netty.handler.codec.http.HttpResponseStatus;
    import org.jboss.netty.handler.codec.http.HttpVersion;

    import com.twitter.finagle.Service;
    import com.twitter.finagle.builder.ServerBuilder;
    import com.twitter.finagle.http.Http;
    import com.twitter.util.Future;
    import com.twitter.util.FutureEventListener;
    import com.twitter.util.FutureTransformer;



