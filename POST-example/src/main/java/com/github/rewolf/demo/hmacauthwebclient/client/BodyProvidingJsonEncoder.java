package com.github.rewolf.demo.hmacauthwebclient.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A Wrapper around the default Jackson2JsonEncoder that captures the serialized body and supplies it to a consumer
 *
 * @author rewolf
 */
@RequiredArgsConstructor
public class BodyProvidingJsonEncoder extends Jackson2JsonEncoder {
    private final Consumer<byte[]> bodyConsumer;

    @Override
    public DataBuffer encodeValue(final Object value, final DataBufferFactory bufferFactory,
                                  final ResolvableType valueType, @Nullable final MimeType mimeType, @Nullable final Map<String, Object> hints) {

        // Encode/Serialize data to JSON
        final DataBuffer data = super.encodeValue(value, bufferFactory, valueType, mimeType, hints);

        // Interception: Generate Signature and inject header into request
        bodyConsumer.accept(extractBytes(data));

        // Return the data as normal
        return data;
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
