package com.github.amkaras.tweetus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.github.amkaras.tweetus")
@EnableScheduling
public class TweetUsRunner {

    public static void main(String[] args) {
        SpringApplication.run(TweetUsRunner.class, args);
    }
}
