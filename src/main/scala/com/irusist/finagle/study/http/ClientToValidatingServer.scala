package com.irusist.finagle.study.http

import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest}
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{RequestBuilder, Http}
import com.twitter.util.Future
import org.jboss.netty.util.CharsetUtil

/**
 * HTTP Client To A Standard Web Server
 *
 * @author zhulx
 */
object ClientToValidatingServer {

  def main(args: Array[String]) {
    val hostNamePort = "www.baidu.com:80"
    val client : Service[HttpRequest, HttpResponse] = ClientBuilder()
    .codec(Http())
    .hosts(hostNamePort)
    .hostConnectionLimit(1)
    .build()

    // 使用RequestBuilder来生成Request，这样HTTP/1.1协议就不用单独加上request.setHeader("Host", hostname)
    val httpRequest = RequestBuilder().url("http://" + hostNamePort).buildGet()
    val responseFuture : Future[HttpResponse] = client(httpRequest)
    responseFuture onSuccess( response => println("Received response: " + response.getContent.toString(CharsetUtil.UTF_8)))
  }

}
