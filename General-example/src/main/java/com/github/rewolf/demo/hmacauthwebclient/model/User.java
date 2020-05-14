package com.github.rewolf.demo.hmacauthwebclient.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class User {
    private final String name;
    private final String email;
}
