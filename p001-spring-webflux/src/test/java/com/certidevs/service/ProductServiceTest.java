package com.certidevs.service;

import com.certidevs.entity.Manufacturer;
import com.certidevs.entity.Product;
import com.certidevs.repository.ManufacturerRepository;
import com.certidevs.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private ProductService productService; // SUT

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
                .price(30.0)
                .quantity(1)
                .active(true)
                .creationDate(LocalDateTime.now().minusDays(10))
                .build();
        Flux<Product> products = productRepository.saveAll(List.of(product1, product2, product3));
        products.collectList().block();

    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll()
                .then(manufacturerRepository.deleteAll()).block();
    }

    @Test
    void findAll() {
        productService.findAll().as(StepVerifier::create)
                .expectNextMatches(p -> p.getTitle().equals("PRODUCT 1"))
                .expectNextMatches(p -> p.getTitle().equals("PRODUCT 2"))
                .expectNextMatches(p -> p.getTitle().equals("PRODUCT 3"))
                .verifyComplete();
    }


    @Test
    void reduceQuantity() {

        productService.reduceQuantity(product1.getId(), 10)
                .as(StepVerifier::create)
                .expectNextMatches(p -> p.getQuantity().equals(40))
                .verifyComplete();

        productService.reduceQuantity(product1.getId(), 60)
                .as(StepVerifier::create)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}