package com.irusist.finagle.study.http

import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http._
import com.twitter.util.Future
import java.net.{InetSocketAddress, SocketAddress}
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.Http

/**
 * Scala HTTP Server Implementation
 *
 * @author zhulx
 */
object HttpServerScala {

  def main(args: Array[String]) {
    val service: Service[HttpRequest, HttpResponse] = new Service[HttpRequest, HttpResponse] {
      def apply(request: HttpRequest) = Future(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
    }

    val address: SocketAddress = new InetSocketAddress(10000)

    val server: Server = ServerBuilder()
      .codec(Http())
      .bindTo(address)
      .name("HttpServer")
      .build(service)

  }

}
