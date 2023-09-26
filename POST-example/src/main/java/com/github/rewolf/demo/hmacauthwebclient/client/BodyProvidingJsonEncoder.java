package com.github.rewolf.demo.hmacauthwebclient.client;

import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A Wrapper around the default Jackson2JsonEncoder that captures the serialized body and supplies it to a consumer
 *
 * @author rewolf
 */
@RequiredArgsConstructor
public class BodyProvidingJsonEncoder extends Jackson2JsonEncoder {
    private final Signer signer;

    @Override
	public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory,
			ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        return super.encode(inputStream, bufferFactory, elementType, mimeType, hints)
                .flatMap(db -> Mono.deferContextual(Mono::just)
                        .map(sc -> {
                            ClientHttpRequest clientHttpRequest = sc.get(MessageSigningHttpConnector.REQUEST_CONTEXT_KEY);

                            signer.injectHeader(clientHttpRequest, extractBytes(db));
                            return db;
                        }));
    }

    /**
     * Extracts bytes from the DataBuffer and resets the buffer so that it is ready to be re-read by the regular
     * request sending process.
     * @param data data buffer with encoded data
     * @return copied data as a byte array.
     */
    private byte[] extractBytes(final DataBuffer data) {
        final byte[] bytes = new byte[data.readableByteCount()];
        data.read(bytes);
        data.readPosition(0);
        return bytes;
    }
}
