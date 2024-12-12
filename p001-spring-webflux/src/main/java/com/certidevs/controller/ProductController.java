package com.certidevs.controller;


import com.certidevs.entity.Product;
import com.certidevs.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 *
 * Ideal si estamos migrando de Spring Web a spring WebFlux y queremos adaptar los controladores que ya tenemos hechos de Spring Web
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

    //    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE) // devuelve los productos como un flujo stream
    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> findAll() {
        log.debug("REST request to find all products");
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


    @PutMapping("{id}")
    public Mono<ResponseEntity<Product>> update(@PathVariable Long id, @RequestBody Product product) {
        if (product.getId() == null)
            return Mono.just(ResponseEntity.badRequest().build()); // 400

        return productService.update(id, product)
                .map(ResponseEntity::ok) // 200
                .defaultIfEmpty(ResponseEntity.notFound().build()) // 404
                .onErrorResume(e -> {
                    log.warn("Error updating product", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()); // 409
                });

    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return productService.findById(id)
                .flatMap(p -> productService.deleteById(p.getId()).then(
                        Mono.just(ResponseEntity.noContent().<Void>build()) // 204
                ))
                .defaultIfEmpty(ResponseEntity.notFound().build()) // 404
                .onErrorResume(e -> {
                    log.warn("Error updating product", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()); // 409
                });

    }

    @PutMapping("{id}/reduce-quantity")
    public Mono<ResponseEntity<Product>> reduceQuantity(@PathVariable Long id, @RequestParam Integer amount) {
        return productService.reduceQuantity(id, amount)
                .map(ResponseEntity::ok) // 200
                .defaultIfEmpty(ResponseEntity.notFound().build()) // 404
                .onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()))
                .onErrorResume(e -> {
                    log.warn("Error updating product", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()); // 409
                });
    }

}
