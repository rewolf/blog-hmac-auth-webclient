package com.github.rewolf.demo.hmacauthwebclient.config;

import com.github.rewolf.demo.hmacauthwebclient.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfiguration {

    @Bean
    public Environment clientEnvironment() {
        return Environment.builder()
                          .protocol("https")
                          .basePath("/")
                          .build();
    }

    @Bean
    public WebClient webclient(final Environment environment,
                               @Value("${client.id}") final String clientId,
                               @Value("${client.secret}") final String secret) throws Exception {

        final Signer signer = new Signer(clientId, secret);
        final MessageSigningHttpConnector httpConnector = new MessageSigningHttpConnector(signer);
        final BodyProvidingJsonEncoder bodyProvidingJsonEncoder = new BodyProvidingJsonEncoder(signer);
        final JsonProvidingDecoder decoder = new JsonProvidingDecoder();

        return WebClient.builder()
                        .exchangeFunction(ExchangeFunctions.create(
                                httpConnector,
                                ExchangeStrategies
                                        .builder()
                                        .codecs(clientDefaultCodecsConfigurer -> {
                                            clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(bodyProvidingJsonEncoder);
                                            clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(decoder);
                                        })
                                        .build()
                        ))
                        .baseUrl(String.format("%s://%s/%s", environment.getProtocol(), environment.getHost(), environment.getBasePath()))
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();
    }
}
