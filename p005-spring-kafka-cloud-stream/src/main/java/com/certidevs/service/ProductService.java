package com.certidevs.service;

import com.certidevs.entity.Product;
import lombok.AllArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ProductService {

    private final StreamBridge streamBridge;

    public Mono<Void> create(Product product) {
        // productRepository.save(producto).map(p -> )
        streamBridge.send("topic-products", product);
        return Mono.empty();
    }
}
