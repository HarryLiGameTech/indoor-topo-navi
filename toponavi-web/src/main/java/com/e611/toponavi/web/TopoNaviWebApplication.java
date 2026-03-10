package com.e611.toponavi.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;

@SpringBootApplication(exclude = {OAuth2ClientAutoConfiguration.class})
public class TopoNaviWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(TopoNaviWebApplication.class, args);
    }
}

