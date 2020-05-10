package com.github.rewolf.demo.hmacauthwebclient.client;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A Wrapper around the default Jackson2JsonEncoder that augments the supplied ClientHttpRequest with an Authorization
 * header containing an HMAC signature generated using the encoded data.
 * @author rewolf
 */
public class RequestSigningJsonEncoder extends Jackson2JsonEncoder {
    private final SignatureProvider signatureProvider;
    private final Supplier<Optional<ClientHttpRequest>> requestSupplier;

    public RequestSigningJsonEncoder(final SignatureProvider signatureProvider, final Supplier<Optional<ClientHttpRequest>> requestSupplier) {
        this.signatureProvider = signatureProvider;
        this.requestSupplier = requestSupplier;
    }

    @Override
    public DataBuffer encodeValue(final Object value, final DataBufferFactory bufferFactory,
                                  final ResolvableType valueType, @Nullable final MimeType mimeType, @Nullable final Map<String, Object> hints) {

        // Encode/Serialize data to JSON
        final DataBuffer data = super.encodeValue(value, bufferFactory, valueType, mimeType, hints);

        // Interception: Generate Signature and inject header into request
        injectSignatureWithBody(data);

        // Return the data as normal
        return data;
    }

    private void injectSignatureWithBody(final DataBuffer data) {
        // Get the ClientHttpRequest from the supplier (it comes from our Http Connector)
        final ClientHttpRequest clientHttpRequest = requestSupplier.get().orElseThrow();

        // Generate the signature and inject as a header
        signatureProvider.injectHeader(clientHttpRequest, extractBytes(data));
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
