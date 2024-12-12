package com.certidevs.controller;


import com.certidevs.entity.Product;
import com.certidevs.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST con enfoque tradicional usando @RestController, @GetMapping, @PostMapping... y ResponseEntity
 *
 * Probar desde OPEN API SWAGGER:
 *
 * http://localhost:8080/webjars/swagger-ui/index.html
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;

    // findAll
//    @GetMapping
//    public ResponseEntity<List<Product>> findAll() {
//        return ResponseEntity.ok(productService.findAll());
//    }

//    public Flux<Product> findAll() {
//        return productService.findAll();
//    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> findAll() {
        return Mono.just(ResponseEntity.ok(productService.findAll()));
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> findById(@PathVariable Long id) {
        return productService.findById(id)
//                .map(product -> ResponseEntity.ok(product))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Product>> create (@RequestBody Product product) {
        if (product.getId() != null)
            return Mono.just(ResponseEntity.badRequest().build()); // 400

        return productService.save(product)
                .map(p -> ResponseEntity.created(URI.create("/api/product/" + p.getId())).body(product)) // 201
                .onErrorResume(e -> {
                   log.warn("Error creating product", e);
                   // return Mono.just(ResponseEntity.internalServerError().build()); // 500
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()); // 409
                });
    }




}
