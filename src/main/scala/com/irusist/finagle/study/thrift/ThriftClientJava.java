package com.irusist.finagle.study.thrift;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.stats.InMemoryStatsReceiver;
import com.twitter.finagle.thrift.ThriftClientFramedCodec;
import com.twitter.finagle.thrift.ThriftClientRequest;
import com.twitter.util.FutureEventListener;
import org.apache.thrift.protocol.TBinaryProtocol;
import thrift.Hello;

import java.net.InetSocketAddress;

/**
 * Java Thrift Client Implementation
 *
 * @author zhulx
 */
public class ThriftClientJava {

    public static void main(String[] args) {
        Service<ThriftClientRequest, byte[]> service = ClientBuilder.safeBuild(ClientBuilder.get()
                .hosts(new InetSocketAddress(8080))
                .codec(ThriftClientFramedCodec.get())
                .hostConnectionLimit(1));

        Hello.FinagledClient client = new Hello.FinagledClient(service, new TBinaryProtocol.Factory(), "HelloService", new InMemoryStatsReceiver());

        client.hi().addEventListener(new FutureEventListener<String>() {
            /**
             * Invoked if the computation completes successfully
             */
            @Override
            public void onSuccess(String value) {
                System.out.println(value);
            }

            /**
             * Invoked if the computation completes unsuccessfully
             */
            @Override
            public void onFailure(Throwable cause) {
                System.out.println("Exception! " + cause);
            }
        });
    }
}
