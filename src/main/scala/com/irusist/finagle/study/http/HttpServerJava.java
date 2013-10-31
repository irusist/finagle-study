package com.irusist.finagle.study.http;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.util.Future;
import org.jboss.netty.handler.codec.http.*;

import java.net.InetSocketAddress;

/**
 * Java HTTP Server Implementation
 *
 *
 * @author zhulx
 */
public class HttpServerJava {

    public static void main(String[] args) {
        Service<HttpRequest, HttpResponse> service = new Service<HttpRequest, HttpResponse>() {
            /**
             * This is the method to override/implement to create your own Service.
             */
            @Override
            public Future<HttpResponse> apply(HttpRequest request) {
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                return Future.value(response);
            }
        };

        ServerBuilder.safeBuild(service, ServerBuilder.get()
                .codec(Http.get()).name("HttpServer").bindTo(new InetSocketAddress(20000)));

    }
}
