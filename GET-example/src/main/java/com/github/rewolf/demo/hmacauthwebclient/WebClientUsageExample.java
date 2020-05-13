package com.github.rewolf.demo.hmacauthwebclient;

import com.github.rewolf.demo.hmacauthwebclient.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
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
        final String result = webClient.get()
                                       .uri(args[0] + "?fun=2&not=cool")
                                       .exchange()
                                       .block()
                                       .bodyToMono(String.class)
                                       .block();

        System.out.println("Response: " + result);
        System.out.println("Navigate to http://requestbin.net/r" + args[0] + "?inspect to see your request");
    }
}
