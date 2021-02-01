package com.github.rewolf.demo.hmacauthwebclient;

import com.github.rewolf.demo.hmacauthwebclient.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@Component
public class WebClientUsageExample implements CommandLineRunner {
    private final WebClient webClient;

    @Override
    public void run(final String... args) throws Exception {
        final String requestBinPath = args[0];
        postExample(requestBinPath);
        getExample(requestBinPath);
    }

    private void getExample(final String pathPart) {
        // Use the client to post our data
        final List result = webClient.get()
                                     .uri("http://worldtimeapi.org/api/timezone")
                                     .header(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE)
                                     .exchange()
                                     .block()
                                     .bodyToMono(List.class)
                                     .block();

        System.out.println("Response: " + result);
    }

    private void postExample(final String pathPart) {
        // Build some data
        final User testUser = new User("Someone Nobody", "someone@example.com");

        // Use the client to post our data
        final String result = webClient.post()
                                       .uri("https://requestbin.net" + pathPart + "/users")
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .body(BodyInserters.fromValue(testUser))
                                       .exchange()
                                       .block()
                                       .bodyToMono(String.class)
                                       .block();

        System.out.println("Response: " + result);
        System.out.println("Navigate to https://requestbin.net/r" + pathPart + "?inspect to see your request");
    }
}
