package com.certidevs.controller;

import com.certidevs.entity.Product;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequestMapping("/api/products")
@RestController
@AllArgsConstructor
public class ProductController {

    private final StreamBridge streamBridge;

    @PostMapping
    public Mono<ResponseEntity<Void>> create(@RequestBody Product product) {
        // productRepository.save(producto).map(p -> )
        streamBridge.send("topic-products", product);
        return Mono.just(ResponseEntity.ok().build());
    }
}
