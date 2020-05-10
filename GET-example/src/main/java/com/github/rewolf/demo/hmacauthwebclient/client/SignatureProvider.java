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
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for generating HMAC-based signatures and injecting the signature into an Authorization header
 *
 * @author rewolf
 */
@Slf4j
public class SignatureProvider {
    private static final String HEX_ENCODED_EMPTY_STRING_SHA256_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final String clientId;
    private final Environment environment;
    private final Mac sha256Hmac;
    private final MessageDigest sha256Hasher;


    public SignatureProvider(final Environment environment, final String clientId, final String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        this.environment = environment;
        this.clientId = clientId;

        // Prepare the encoder to be used to
        final SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureMethod.HMAC_SHA256);
        sha256Hasher = MessageDigest.getInstance("SHA-256");
        sha256Hmac = Mac.getInstance("HmacSHA256");
        sha256Hmac.init(secretKeySpec);
    }

    public Mono<ClientRequest> injectHeader(final ClientRequest clientRequest) {
        System.out.println("injectHeader");
        final String dateString = ZonedDateTime.now().toString();
        final String authHeader = buildAuthHeaderForRequest(
                clientRequest.url().getPath(),
                clientRequest.method().name(),
                dateString,
                toQueryString(Collections.emptyMap()),
                null);

        return Mono.just(ClientRequest.from(clientRequest)
                                      .header(HttpHeaders.DATE, dateString)
                                      .header(HttpHeaders.AUTHORIZATION, authHeader)
                                      .build());
    }

    /**
     * Build the Authorization header value for the given request
     *
     * @param uriPath
     * @param httpMethod
     * @param dateHeaderValue
     * @param body
     * @return
     */
    private String buildAuthHeaderForRequest(final String uriPath,
                                             final String httpMethod,
                                             final String dateHeaderValue,
                                             final String queryString,
                                             final byte[] body) {
        final String stringToSign = String.join("\n",
                                                environment.getHost(),
                                                uriPath,
                                                dateHeaderValue,
                                                clientId,
                                                queryString,
                                                httpMethod,
                                                hash(body)
        );


        log.debug("\nString-to-sign:\n----\n{}\n----------", stringToSign);
        final String signature = sign(stringToSign);


        return String.format("Custom-Auth-v1.0 client=%s, signature=%s", clientId, signature);
    }

    private String toQueryString(final Map<String, String> parameters) {
        return parameters == null || parameters.isEmpty() ? "" : parameters.entrySet()
                                                                           .stream()
                                                                           .map(pair -> String.format("%s=%s", pair.getKey(), pair.getValue()))
                                                                           .collect(Collectors.joining("&"));
    }


    /**
     * Sign a string by encode using the HMac based on the secret key generated in the constructor
     *
     * @param stringToSign the message to be signed
     * @return the signature
     */
    private synchronized String sign(final String stringToSign) {
        final byte[] hmacEncode = sha256Hmac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacEncode);
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