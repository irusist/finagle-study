package com.irusist.finagle.study.thrift;

import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.thrift.ThriftServerFramedCodec;
import com.twitter.util.Future;
import org.apache.thrift.protocol.TBinaryProtocol;
import thrift.Hello;

import java.net.InetSocketAddress;

/**
 * Java Thrift Server Implementation
 *
 * @author zhulx
 */
public class ThriftServerJava {

    public static void main(String[] args) {
        Hello.FutureIface processor = new Hello.FutureIface() {
            @Override
            public Future<String> hi() {
                return Future.value("hi");
            }
        };

        ServerBuilder.safeBuild(new Hello.FinagledService(processor, new TBinaryProtocol.Factory()),
                ServerBuilder.get().name("HelloService")
                        .codec(ThriftServerFramedCodec.get()).bindTo(new InetSocketAddress(8080)));
    }
}
