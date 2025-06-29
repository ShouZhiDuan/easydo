package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExternalController {

    @GetMapping("/external-hello")
    public String externalHello() {
        return "Hello from the external HTTPS server!";
    }
} 