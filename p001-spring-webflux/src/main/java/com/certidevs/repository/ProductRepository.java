package com.certidevs.repository;

import com.certidevs.entity.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {


    Mono<Product> findByTitle(String title);

    Mono<Boolean> existsByManufacturerId(Long manufacturerId);

    Flux<Product> findByManufacturerId(Long manufacturerId);
    Flux<Product> findByActiveTrue();
    Flux<Product> findByActiveFalse();

    @Query("""
    SELECT * FROM product WHERE quantity < :quantity
    """)
    Flux<Product> findByQuantityLessThan(Integer quantity);


}
