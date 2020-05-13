package com.github.rewolf.demo.hmacauthwebclient.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.dsig.SignatureMethod;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

/**
 * Utility class for generating HMAC-based signatures and injecting the signature into an Authorization header
 *
 * @author rewolf
 */
@Slf4j
public class SignatureProvider {
    private static final String HEX_ENCODED_EMPTY_STRING_SHA256_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final String clientId;
    private final MessageDigest sha256Hasher;
    private final SecretKeySpec secretKeySpec;

    public SignatureProvider(final String clientId, final String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        this.clientId = clientId;
        sha256Hasher = MessageDigest.getInstance("SHA-256");
        secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureMethod.HMAC_SHA256);
    }

    public Mono<ClientRequest> injectHeader(final ClientRequest clientRequest) {
        log.debug("injectHeader for: [" + clientRequest.url() +"]");
        final String dateString = ZonedDateTime.now().toString();
        final String authHeader = buildAuthHeaderForRequest(
                clientRequest,
                dateString,
                new byte[0]);

        return Mono.just(ClientRequest.from(clientRequest)
                                      .header(HttpHeaders.DATE, dateString)
                                      .header(HttpHeaders.AUTHORIZATION, authHeader)
                                      .build());
    }

    /**
     * Build the Authorization header value for the given request
     *
     * @param clientRequest the request from which to pull fields for signing
     * @param dateHeaderValue date string used in date header, for signing
     * @param body the byte data for the body, for signing
     * @return the Authorization header value including the signature
     */
    private String buildAuthHeaderForRequest(final ClientRequest clientRequest,
                                             final String dateHeaderValue,
                                             final byte[] body) {
        final String queryString = clientRequest.url().getQuery();
        final String stringToSign = String.join("\n",
                                                clientRequest.url().getHost(),
                                                clientRequest.url().getPath(),
                                                dateHeaderValue,
                                                clientId,
                                                queryString == null ? "" : queryString,
                                                clientRequest.method().name(),
                                                hash(body)
        );


        log.debug("\nString-to-sign:\n----\n{}\n----------", stringToSign);
        final String signature = sign(stringToSign);


        return String.format("Custom-Auth-v1.0 client=%s, signature=%s", clientId, signature);
    }

    /**
     * Sign a string by encode using the HMac based on the secret key generated in the constructor
     *
     * @param stringToSign the message to be signed
     * @return the signature
     */
    private synchronized String sign(final String stringToSign) {
        final byte[] hmacEncode = getMac().doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacEncode);
    }

    /**
     * Return a MAC capable of signing our request. Note that it is not thread safe and has state
     * @return the Mac
     */
    private Mac getMac() {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("JDK Does not support the auth scheme", e);
        }
    }


    /**
     * Hash the given string with sha256
     *
     * @return sha256-hashed message
     */
    private synchronized String hash(final byte[] bytes) {
        if (bytes==null || bytes.length==0) {
            return HEX_ENCODED_EMPTY_STRING_SHA256_HASH;
        }
        final byte[] byteData = sha256Hasher.digest(bytes);
        return bytesToHex(byteData);
    }

    /*
     * Use Hex.encodeHexString from Apache Commons instead if you don't mind including the bulky dependency
     * Below is from https://stackoverflow.com/a/9855338/343759
     */
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}