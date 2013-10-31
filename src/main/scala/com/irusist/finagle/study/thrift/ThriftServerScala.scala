package com.irusist.finagle.study.thrift

import thrift.Hello
import thrift.Hello.FutureIface
import com.twitter.util.Future
import org.apache.thrift.protocol.TBinaryProtocol
import com.twitter.finagle.builder.{ServerBuilder, Server}
import java.net.InetSocketAddress
import com.twitter.finagle.thrift.ThriftServerFramedCodec

/**
 * Scala Thrift Server Implementation
 *
 * @author zhulx
 */
object ThriftServerScala {

  def main(args: Array[String]) {
    val processor = new FutureIface {
      def hi(): Future[String] = Future.value("hi")
    }

    val service = new Hello.FinagledService(processor, new TBinaryProtocol.Factory())

    val server: Server = ServerBuilder()
      .name("HelloService")
      .bindTo(new InetSocketAddress(8080))
      .codec(ThriftServerFramedCodec())
      .build(service)

  }
}
