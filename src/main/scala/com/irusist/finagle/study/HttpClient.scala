package com.irusist.finagle.study

import com.twitter.finagle.{Service, SimpleFilter}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import com.twitter.util.Future
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http
import java.net.InetSocketAddress
import org.jboss.netty.util.CharsetUtil

/**
 * description
 *
 * @author zhulx
 */
object HttpClient {

  class InvalidRequest extends Exception

  class HandleErrors extends SimpleFilter[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      service(request) flatMap {
        response =>
          response.getStatus match {
            case OK => println(1); Future.value(response)
            case FORBIDDEN => println(2); Future.exception(new InvalidRequest)
            case _ => println(response); Future.exception(new Exception(response.getStatus.getReasonPhrase))
          }
      }
    }
  }

  def main(args : Array[String]) {
    val clientWithoutErrorHandling : Service[HttpRequest, HttpResponse] = ClientBuilder()
    .codec(Http())
    .hosts("www.jd.com:80")
    .hostConnectionLimit(2)
    .build()

    val handleErrors = new HandleErrors

    val client : Service[HttpRequest, HttpResponse] = handleErrors andThen clientWithoutErrorHandling

    println("))) Issuing two request is parallel: ")
    val request1 = makeAuthorizedRequest(client)
    val request2 = makeUnAuthorizedRequest(client)

    (request1 join request2) ensure {
      client.close()
    }
  }


  private[this] def makeAuthorizedRequest(client : Service[HttpRequest, HttpResponse]) = {
    val authorizedRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")

    authorizedRequest.addHeader("Authorization", "open seame")
   // authorizedRequest.addHeader("Host", "www.jd.com")

    client(authorizedRequest) onSuccess {
      response =>
        val responseString = response.getContent.toString(CharsetUtil.UTF_8)
        println("))) Received result for authorized request: " + responseString)
    }
  }

  private[this] def makeUnAuthorizedRequest(client: Service[HttpRequest, HttpResponse]) = {
    val unauthorizedRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")

    client(unauthorizedRequest) onFailure {
      error =>
        println("))) Unauthorized request errored (as desired): " + error.getCause)
    }
  }
}

