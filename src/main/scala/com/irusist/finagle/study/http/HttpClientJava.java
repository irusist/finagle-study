package com.irusist.finagle.study.http;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.util.FutureEventListener;
import org.jboss.netty.handler.codec.http.*;

/**
 * Java HTTP Client Implementation
 *
 * @author zhulx
 */
public class HttpClientJava {

    public static void main(String[] args) {
        // 多个request可以复用一个client
        Service<HttpRequest, HttpResponse> client = ClientBuilder.safeBuild(ClientBuilder.get()
                .codec(Http.get())
                .hosts("localhost:20000")
                .hostConnectionLimit(1));

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        client.apply(request).addEventListener(new FutureEventListener<HttpResponse>() {
            /**
             * Invoked if the computation completes successfully
             */
            @Override
            public void onSuccess(HttpResponse response) {
                System.out.println("received response: " + response);
            }

            /**
             * Invoked if the computation completes unsuccessfully
             */
            @Override
            public void onFailure(Throwable cause) {
                System.out.println("failed with cause: " + cause);
            }
        });
    }
}
