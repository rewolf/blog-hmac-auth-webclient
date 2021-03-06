package com.github.rewolf.demo.hmacauthwebclient;

import com.github.rewolf.demo.hmacauthwebclient.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Component
public class WebClientUsageExample implements CommandLineRunner {
    private final WebClient webClient;

    @Override
    public void run(final String... args) throws Exception {
        // Build some data
        final User testUser = new User("Someone Nobody", "someone@example.com");

        // Use the client to post our data
        final String result = webClient.post()
                                       .uri(args[0] + "/users")
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .body(BodyInserters.fromValue(testUser))
                                       .exchangeToMono(r -> r.bodyToMono(String.class))
                                       .block();

        System.out.println("Response: " + result);
        System.out.println("Navigate to http://requestbin.net/r" + args[0] + "?inspect to see your request");
    }
}
