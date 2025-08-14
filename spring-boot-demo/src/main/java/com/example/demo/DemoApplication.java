package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class DemoApplication {

    @Value("${spring.application.name}")
    private String appName;

	@Value("${testName:testName}")
    private String testName;

    @PostConstruct
	void init() {
		log.info("appName: " + appName);
		log.info("testName: " + testName);
	}

	public static void sum(int  n, int m){
		int a = 1;
		int b = 2;
		int c = a + b;
		int d = c + n + m;
		System.out.println("c: " + c);
		System.out.println("d: " + d);
	}


    public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}




}
