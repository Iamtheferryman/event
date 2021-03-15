package com.cyril.event;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.cyri.event"})
@EnableConfigurationProperties()
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
