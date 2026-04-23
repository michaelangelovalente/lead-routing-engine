package com.casavo.leadrouting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LeadRoutingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeadRoutingApplication.class, args);
    }
}
