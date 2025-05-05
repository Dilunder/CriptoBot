package org.example.criptobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CryptoBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(CryptoBotApplication.class, args);
    }
}

// sheduller - для обновления кеша и reddis