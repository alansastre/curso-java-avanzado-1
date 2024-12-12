package com.certidevs.service;

import com.certidevs.entity.Product;
import com.certidevs.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Flux<Product> findAll() {
        return productRepository.findAll().map(p -> {
            p.setTitle(p.getTitle().toUpperCase());
            return p;
        });
    }

    public Mono<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Mono<Boolean> existsById(Long id) {
        return productRepository.existsById(id);
    }

    public Mono<Product> save(Product product) {
        product.setActive(true);
        product.setCreationDate(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Mono<Product> update(Long id, Product product) {
        return productRepository.findById(id)
                .flatMap(productDB -> {
                    productDB.setPrice(product.getPrice());
                    productDB.setQuantity(product.getQuantity());
                    // BeanUtils.copyProperties(product, productDB);
                    return productRepository.save(productDB);
                });
    }

    public Mono<Void> deleteById(Long id) {
        return productRepository.deleteById(id);
    }

    public Flux<Product> increasePriceOfActiveProducts(Double percentage) {
//        return productRepository.findAll()
//                .filter(Product::getActive)
//                .flatMap(p -> {
//                    double price = p.getPrice() != null ? p.getPrice() : 0.0;
//                    double newPrice = price * (1 + percentage / 100);
//                    p.setPrice(newPrice);
//                    return productRepository.save(p);
//                });

        return productRepository.findByActiveTrue()
                .map(p -> {
                    double price = p.getPrice() != null ? p.getPrice() : 0.0;
                    double newPrice = price * (1 + percentage / 100);
                    p.setPrice(newPrice);
                    return p;
                }).collectList()
                .flatMapMany(productRepository::saveAll);

//        return productRepository.findByActiveTrue()
//                .map(p -> {
//                    double price = p.getPrice() != null ? p.getPrice() : 0.0;
//                    double newPrice = price * (1 + percentage / 100);
//                    p.setPrice(newPrice);
//                    return p;
//                }).buffer(100)
//                .flatMap(productRepository::saveAll);

    }

    public Mono<Void> increasePriceOfActiveProductsVoid(Double percentage) {
        return productRepository.findByActiveTrue()
                .map(p -> {
                    double price = p.getPrice() != null ? p.getPrice() : 0.0;
                    double newPrice = price * (1 + percentage / 100);
                    p.setPrice(newPrice);
                    return p;
                }).collectList()
                .flatMapMany(productRepository::saveAll)
                .then();
    }

    public Mono<Product> reduceQuantity(Long id, Integer amount) {
        return productRepository.findById(id)
                .flatMap(product -> {
                    if (product.getQuantity() >= amount) {
                        product.setQuantity(product.getQuantity() - amount);
                        return productRepository.save(product);
                    } else {
                        log.warn("No se puede decrementar cantidad de producto {}", id);
                        return Mono.error(new IllegalArgumentException("Cantidad erróneo"));
                    }
                });
    }

    public Mono<Product> findByIdWithManufacturer(Long id) {
        return null;
    }
    public Flux<Product> findAllWithManufacturer() {
        return null;
    }
    public Mono<Product> findByIdWithManufacturerAndRatings(Long id) {
        return null;
    }
    public Mono<Product> findRemoteById(Long id) {
        return null;
    }


    public Mono<Long> count() {
        return productRepository.count();
    }
}
