package com.github.rewolf.demo.hmacauthwebclient.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.URI;
import java.util.Set;
import java.util.function.Function;

import static org.springframework.http.HttpMethod.*;

/**
 * Http Connector that acts as a hook to sign the ClientHttpRequest potentially with HTTP body data provided from
 * some encoder. Should be added as an ExchangeStrategy on a WebClient
 *
 * @author rewolf
 */
public class MessageSigningHttpConnector extends ReactorClientHttpConnector {
    public static final String REQUEST_CONTEXT_KEY = "REQUEST_CONTEXT_KEY";
    private final Set<HttpMethod> BODYLESS_METHODS = Set.of(GET, DELETE, TRACE, HEAD, OPTIONS);
    private final Signer signer;

    public MessageSigningHttpConnector(final Signer signer) {
        this.signer = signer;
    }

    @Override
    public Mono<ClientHttpResponse> connect(final HttpMethod method, final URI uri,
                                            final Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {
        return super.connect(method, uri, incomingRequest -> {
            signBodyless(incomingRequest);
            return requestCallback.apply(incomingRequest).contextWrite(Context.of(REQUEST_CONTEXT_KEY, incomingRequest));
        });
    }

    /**
     * Given the ClientHttpRequest, if a body is not required, sign the request immediately.
     *
     * @param request current http request to sign
     */
    private void signBodyless(final ClientHttpRequest request) {
        if (BODYLESS_METHODS.contains(request.getMethod())) {
            signer.injectHeader(request, null);
        }
    }
}
