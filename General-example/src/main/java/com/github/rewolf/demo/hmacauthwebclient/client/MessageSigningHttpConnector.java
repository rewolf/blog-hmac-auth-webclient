package com.github.rewolf.demo.hmacauthwebclient.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Mono;

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
    private final Set<HttpMethod> BODYLESS_METHODS = Set.of(GET, DELETE, TRACE, HEAD, OPTIONS);
    private final ThreadLocal<ClientHttpRequest> request = new ThreadLocal<>();
    private final Signer signer;

    public MessageSigningHttpConnector(final Signer signer) {
        this.signer = signer;
    }

    @Override
    public Mono<ClientHttpResponse> connect(final HttpMethod method, final URI uri,
                                            final Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {
        return super.connect(method, uri, incomingRequest -> {
            sign(incomingRequest);
            return requestCallback.apply(incomingRequest);
        });
    }

    /**
     * Given the ClientHttpRequest, depending on whether a body is required or not, decide to sign the request
     * immediately or defer until the serialized body is provided.
     *
     * @param request current http request to sign
     */
    private void sign(final ClientHttpRequest request) {
        if (BODYLESS_METHODS.contains(request.getMethod())) {
            signer.injectHeader(request, null);
        } else {
            deferSignWith(request);
        }
    }

    /**
     * Save message for signing later when body has been encoded
     * @param request ongoing request for this thread.
     */
    private void deferSignWith(final ClientHttpRequest request) {
        this.request.set(request);
    }

    /**
     * Will perform the signing and injection on the request once the bytes are provided. Also releases the request
     * from the ThreadLocal
     *
     * @param bodyData
     */
    public void signWithBody(byte[] bodyData) {
        signer.injectHeader(request.get(), bodyData);
        // release the request from the thread-local
        request.remove();
    }
}
