package com.github.rewolf.demo.hmacauthwebclient.config;

import com.github.rewolf.demo.hmacauthwebclient.client.Environment;
import com.github.rewolf.demo.hmacauthwebclient.client.SignatureProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfiguration {

    @Bean
    public Environment clientEnvironment() {
        return Environment.builder()
                          .protocol("http")
                          .host("requestbin.net")
                          .basePath("/r")
                          .build();
    }

    @Bean
    public WebClient webclient(final Environment environment,
                               @Value("${client.id}") final String clientId,
                               @Value("${client.secret}") final String secret) throws Exception {

        final SignatureProvider signatureProvider = new SignatureProvider(environment, clientId, secret);

        return WebClient
                .builder()
                .baseUrl(String.format("%s://%s/%s", environment.getProtocol(), environment.getHost(), environment.getBasePath()))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(ExchangeFilterFunction.ofRequestProcessor(signatureProvider::injectHeader))
                .build();
    }
}
