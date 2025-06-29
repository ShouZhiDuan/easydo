package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class HelloController {

    private final RestTemplate restTemplate;

    public HelloController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, HTTPS!";
    }

    @GetMapping("/call-external")
    public String callExternal() {
        return restTemplate.getForObject("https://localhost:8444/external-hello", String.class);
    }
} 