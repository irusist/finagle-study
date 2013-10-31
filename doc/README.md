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

In cases where you want to continue execution immediately, you can specify a callback. The callback is identified by the `onSuccess` keyword:

    val request: HttpRequest = new DefaultHttpRequest(HTTP_1_1, GET, "/")
    val responseFuture: Future[HttpResponse] = client(request)
    responseFuture onSuccess { responseFuture =>
      println(responseFuture)
    }

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


Future Exceptions
