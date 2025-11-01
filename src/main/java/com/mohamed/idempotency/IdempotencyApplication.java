package com.mohamed.idempotency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IdempotencyApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdempotencyApplication.class, args);
    }

}
