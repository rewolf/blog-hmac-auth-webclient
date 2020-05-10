package com.github.rewolf.demo.hmacauthwebclient;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class HmacAuthWebclientApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HmacAuthWebclientApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
