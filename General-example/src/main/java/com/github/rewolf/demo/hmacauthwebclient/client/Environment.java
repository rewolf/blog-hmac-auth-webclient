package com.github.rewolf.demo.hmacauthwebclient.client;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Environment {
    private final String host;
    private final String protocol;
    private final String basePath;
}
