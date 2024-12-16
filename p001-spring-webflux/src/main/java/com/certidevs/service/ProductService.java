package com.certidevs.service;

import com.certidevs.dto.RatingDTO;
import com.certidevs.entity.Manufacturer;
import com.certidevs.entity.Product;
import com.certidevs.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final WebClient manufacturerClient;
    private final WebClient ratingClient;

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

        // Suponemos que manufacturer estuviera en otro microservicio y hay que traerlo con webclient
//        // Trae un manufacturer por cada producto por tanto podría no ser eficiente si hay muchos productos
        return productRepository.findAll()
                .flatMap(
                        product -> manufacturerClient.get()
                                .uri("/manufacturers/{id}", product.getManufacturerId())
                                .retrieve()
                                .bodyToMono(Manufacturer.class)
                                // .map(product::manufacturer) // si tenemos @Accessors(fluent = true)
                                .map(manufacturer -> {
                                    product.setManufacturer(manufacturer);
                                    return product;
                                })
                                .defaultIfEmpty(product)
                );

        // traer todos los fabricantes en una sola petición y asignarlos a los productos:
        // código un poco más complejo
//        return productRepository.findAll()
//                .collectMultimap(Product::getManufacturerId)
//                .flatMapMany(manufacturerIdWithProductsMap -> {
//                    Set<Long> manufacturerIds = manufacturerIdWithProductsMap.keySet();
//                    return manufacturerClient.post()
//                            .uri("/manufacturers/search")
//                            .bodyValue(manufacturerIds)
//                            .retrieve()
//                            .bodyToFlux(Manufacturer.class)
//                            .flatMap(manufacturer -> {
//                                // asignar el fabricante a todos los productos que tengan ese fabricante
//                                Collection<Product> products = manufacturerIdWithProductsMap.get(manufacturer.getId());
//                                products.forEach(product -> product.setManufacturer(manufacturer));
//                                return Flux.fromIterable(products);
//                            });
//                });



    }

    public Flux<Product> findAllWithManufacturerAndRatings() {

        // Product tendría ManyToOne con Manufacturer
        // Product tendría OneToMany con Rating
        // Manufacturer y Rating estarían en otros microservicios

        AtomicReference<Object> resource = new AtomicReference<>(new Object());
        return productRepository.findAll()
                .flatMap(product -> {
                    // get manufacturer
                    Mono<Manufacturer> manufacturerMono = manufacturerClient.get()
                            .uri("manufactures/{id}", product.getManufacturerId())
                            .retrieve()
                            .bodyToMono(Manufacturer.class)
                            .onErrorResume(e -> Mono.empty());

                    // get ratings
                    Flux<RatingDTO> ratingDTOFlux = ratingClient.get()
                            .uri("ratings?productId={id}", product.getId())
                            .retrieve()
                            .bodyToFlux(RatingDTO.class)
                            .onErrorResume(e -> Flux.empty());

                    // zip

                    return Mono.zip(manufacturerMono, ratingDTOFlux.collectList())
                            .map(tuple -> {
                                product.setManufacturer(tuple.getT1());
                                product.setRatings(tuple.getT2());
                                return product;
                            }).defaultIfEmpty(product)
                            .doFinally(signalType ->  {
                                resource.set(null);
                            });

                });

        // alternativa a collectList:
        // collectMap, collectMultimap, reduce, reduceWith, collect()
        // trocear con buffer
        // usar resource y cambiarlo a null doFinally

    }

//    public Flux<Product> findAllWithManufacturerAndRatingsOneQuery() {
//        return productRepository.findAll()
//                .collectMultimap(Product::getManufacturerId)
//                .flatMapMany(manufacturerIdToProducts -> {
//                    List<Long> manufacturerIds = new ArrayList<>(manufacturerIdToProducts.keySet());
//                    return manufacturerClient.post()
//                            .uri("/api/manufacturers/search")
//                            .bodyValue(manufacturerIds)
//                            .retrieve()
//                            .bodyToFlux(Manufacturer.class)
//                            .flatMap(manufacturer -> {
//                                Collection<Product> products = manufacturer == null ? new ArrayList<>() : manufacturer.getProducts();
//                                products.forEach(product -> product.setManufacturer(manufacturer));
//                                return Flux.fromIterable(products);
//                            })
//                            // obtener todas las reviews de todos los product en una sola query
//                            .collectList()
//                            .flatMapMany(products -> {
//                                List<Long> productIds = products.stream()
//                                        .map(Product::getId)
//                                        .toList();
//                                return ratingClient.post()
//                                        .uri("/api/ratings/search")
//                                        .bodyValue(productIds)
//                                        .retrieve()
//                                        .bodyToFlux(RatingDTO.class)
//                                        .collectMultimap(RatingDTO::product)
//                                        .map(productIdToReviews -> {
//                                            products.forEach(product -> {
//                                                Collection<RatingDTO> reviews = productIdToReviews.get(product.getId());
//                                                if (reviews != null) {
//                                                    product.setRatings(new ArrayList<>(reviews));
//                                                } else {
//                                                    product.setRatings(new ArrayList<>());
//                                                }
//                                            });
//                                            return products;
//                                        })
//                                        .flatMapMany(Flux::fromIterable);
//                            });
//
//                });
//    }

    public Mono<Product> findRemoteById(Long id) {
        return null;
    }


    public Mono<Long> count() {
        return productRepository.count();
    }

    // Probar a liberar resource de collectList desde un test: doFinally (no permite cambiar la lista a null clear)
}
