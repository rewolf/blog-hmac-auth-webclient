package com.github.rewolf.demo.hmacauthwebclient.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.URI;
import java.util.function.Function;

/**
 * Http Connector that acts as a hook to supply the ClientHttpRequest to the data encoder for the purpose of
 * signature header injection.
 *
 * @author rewolf
 */
public class MessageSigningHttpConnector extends ReactorClientHttpConnector {
  
	public static final String REQUEST_CONTEXT_KEY = "REQUEST_CONTEXT_KEY";

    @Override
    public Mono<ClientHttpResponse> connect(final HttpMethod method, final URI uri,
                                            final Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {
        // execute the super-class method as usual, but insert an interception into the requestCallback that can
        // capture the request to be saved for this thread.
    	  return super.connect(method, uri, incomingRequest -> {      
              return requestCallback.apply(incomingRequest)
                                    .subscriberContext(Context.of(REQUEST_CONTEXT_KEY, incomingRequest));
          });
    }

   
}
