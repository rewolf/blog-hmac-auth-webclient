package com.github.rewolf.demo.hmacauthwebclient.client;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Wrapper around the default Jackson2JsonEncoder that captures the serialized body and supplies it to a consumer
 *
 * @author rewolf
 */
public class JsonProvidingDecoder extends Jackson2JsonDecoder {
    @Override
    public Mono<Object> decodeToMono(final Publisher<DataBuffer> input, final ResolvableType elementType, final MimeType mimeType, final Map<String, Object> hints) {
        List<byte[]> buffers = new ArrayList<>();
        Flux<DataBuffer> interceptor = Flux.from(input)
                                           .doOnNext(buffer -> buffers.add(extractBytes(buffer)))
                                           .doOnComplete(() -> printResult(buffers));
        return super.decodeToMono(interceptor, elementType, mimeType, hints);
    }

    private void printResult(final List<byte[]> buffers) {
        int length = buffers.stream().mapToInt(b -> b.length).sum();
        byte[] result = new byte[length];
        int index = 0;
        for (byte[] buffer : buffers) {
            System.arraycopy(buffer, 0, result, index, buffer.length);
            index += buffer.length;
        }
        System.out.println("RESULT: " + new String(result));
    }

    private byte[] extractBytes(final DataBuffer data) {
        final byte[] bytes = new byte[data.readableByteCount()];
        data.read(bytes);
        data.readPosition(0);
        return bytes;
    }
}
