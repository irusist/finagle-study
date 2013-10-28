import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.Http
import com.twitter.finagle.netty3.Ok
import com.twitter.finagle.Service
import com.twitter.util.Future
import java.net.{InetSocketAddress, SocketAddress}
import org.jboss.netty.handler.codec.http.{DefaultHttpResponse, HttpResponse, HttpRequest, DefaultHttpRequest}
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

/**
 * description
 *
 * @author Administrator
 */


object Server {

  def main(args: Array[String]) {
    val  service : Service[HttpRequest, HttpResponse] = new Service[HttpRequest, HttpResponse] {
      def apply(request : HttpRequest) = Future(new DefaultHttpResponse(HTTP_1_1, OK))
    }
    val address : SocketAddress = new InetSocketAddress(10000)

    val server : Server = ServerBuilder()
                          .codec(Http())
                          .bindTo(address)
                          .name("httpServer")
                          .build(service)
  }
}
