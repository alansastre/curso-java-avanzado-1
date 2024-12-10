package com.certidevs.repository;

import com.certidevs.entity.Manufacturer;
import com.certidevs.entity.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
@DataR2dbcTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    Manufacturer manufacturer;
    Product product1;
    Product product2;
    Product product3;

    @BeforeEach
    void setUp() {
        manufacturer = manufacturerRepository.save(Manufacturer.builder()
                        .name("CertiDevs")
                        .foundationYear(2000)
                        .country("Spain")
                .build()).block();


        product1 = Product.builder()
                .title("Product 1")
                .price(10.0)
                .quantity(50)
                .active(true)
                .creationDate(LocalDateTime.now().minusDays(10))
                .manufacturerId(manufacturer.getId())
                .manufacturer(manufacturer)
                .build();
        product2 = Product.builder()
                .title("Product 2")
                .price(20.0)
                .quantity(5)
                .active(false)
                .creationDate(LocalDateTime.now().minusDays(5))
                .manufacturerId(manufacturer.getId())
                .manufacturer(manufacturer)
                .build();
        product3 = Product.builder()
                .title("Product 3")
                .price(5.0)
                .quantity(1)
                .active(true)
                .creationDate(LocalDateTime.now().minusDays(10))
                .build();
        Flux<Product> products =  productRepository.saveAll(List.of(product1, product2 ,product3));
        products.collectList().block();

    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll()
                .then(manufacturerRepository.deleteAll()).block();
    }

    @Test
    void save() {
        var product = Product.builder()
                .title("Producto Test")
                .price(5.0)
                .quantity(1)
                .active(true)
                .creationDate(LocalDateTime.now().minusDays(10))
                .build();

        Mono<Product> productMono = productRepository.save(product);

        // Opción 1: bloquear y usar aserciones de junit
        Product savedProduct = productMono.block();
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());

        // Opción 2: StepVerifier expects
//        StepVerifier.create(productMono)
//                .expectNextMatches(p -> p.getId() != null && p.getTitle().equals("Producto Test"))
//                .verifyComplete();

        // Opción 3: versión compacta:
//        productRepository.save(product)
////                .as(p -> StepVerifier.create(p))
//                .as(StepVerifier::create)
//                .expectNextMatches(p -> p.getId() != null && p.getTitle().equals("Producto Test"))
//                .verifyComplete();
    }
}