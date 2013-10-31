package com.irusist.finagle.study.thrift

import com.twitter.finagle.thrift.{ThriftClientFramedCodec, ThriftClientRequest}
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import java.net.InetSocketAddress
import thrift.Hello
import org.apache.thrift.protocol.TBinaryProtocol

/**
 * Scala Thrift Client Implementation
 *
 * @author zhulx
 */
object ThriftClientScala {

  def main(args: Array[String]) {
    val service: Service[ThriftClientRequest, Array[Byte]] = ClientBuilder()
      .hosts(new InetSocketAddress(8080))
      .codec(ThriftClientFramedCodec())
      .hostConnectionLimit(1)
      .build()

    val client = new Hello.FinagledClient(service, new TBinaryProtocol.Factory())

    client.hi() onSuccess {
      response =>
        println("Received response: " + response)
    } ensure {
      service.close()
    }
  }

}
