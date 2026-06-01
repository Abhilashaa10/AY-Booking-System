package com.ticketing.cancellation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CancellationApplication {
    public static void main(String[] args) {
        SpringApplication.run(CancellationApplication.class, args);
    }
}