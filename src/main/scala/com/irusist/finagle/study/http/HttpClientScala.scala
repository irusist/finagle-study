package com.irusist.finagle.study.http

import org.jboss.netty.handler.codec.http._
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{Method, Version, Http}
import java.net.InetSocketAddress
import com.twitter.util.Future

/**
 * Scala HTTP Client Implementation
 *
 * @author zhulx
 */
object HttpClientScala {

  def main(args: Array[String]) {

    val address = new InetSocketAddress(10000)

    val client: Service[HttpRequest, HttpResponse] = ClientBuilder()
      .codec(Http())
      .hosts(Array(address))
      .hostConnectionLimit(1)
      .build()

    val request: HttpRequest = new DefaultHttpRequest(Version.Http11, Method.Get, "/")
    val responseFuture: Future[HttpResponse] = client(request)

    responseFuture onSuccess {
      response => println("Received response: " + response)
    } onFailure {
      case e => println(1)
    }


  }

}
