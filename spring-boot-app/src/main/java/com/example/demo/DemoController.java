package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api")
@Slf4j
public class DemoController {

    private final Random random = new Random();

    @GetMapping("/hello")
    public String hello() {
        log.info("Hello API called at {}", LocalDateTime.now());
        return "Hello OpenSearch!";
    }

    @GetMapping("/product/{id}")
    public String getProduct(@PathVariable Long id) {
        log.info("Get product id: {}", id);
        if (random.nextBoolean()) {
            log.warn("Product {} is low stock", id);
        }
        if (random.nextInt(10) == 0) {
            log.error("Database timeout when fetching product {}", id);
            throw new RuntimeException("Demo error");
        }
        return "Product " + id;
    }

    @PostMapping("/order")
    public String createOrder() {
        log.info("Order created successfully");
        return "Order created";
    }
}
