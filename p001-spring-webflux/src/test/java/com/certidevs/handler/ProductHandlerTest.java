package com.certidevs.handler;

import com.certidevs.entity.Manufacturer;
import com.certidevs.entity.Product;
import com.certidevs.repository.ManufacturerRepository;
import com.certidevs.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
    Test de integración, no usa mocks
    Para probar el API se usa un cliente HTTP especializado para pruebas:
    * Spring Web: MockMvc
    * Spring WebFlux: WebTestClient
 */
@SpringBootTest
@AutoConfigureWebTestClient
class ProductHandlerTest {

    @Autowired
    private WebTestClient client;

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
    void findAll() {
//        client.get().uri("/api/products")
        client.get().uri("/api/route/products")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Product.class)
                .hasSize(3)
                .contains(product1, product2, product3)
                .consumeWith(response -> {
                    var products = response.getResponseBody();
                    assertNotNull(products);
                    assertEquals("PRODUCT 1", products.getFirst().getTitle());
                });

        client.get().uri("/api/route/products")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("PRODUCT 1");
    }

    @Test
    void findById() {
//        client.get().uri("/api/route/products/" + product1.getId())
        client.get().uri("/api/route/products/{id}", product1.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(product1)
                .value(product -> {
                    assertNotNull(product);
                    assertEquals(product1.getId(), product.getId());
                    assertEquals(product1.getTitle(), product.getTitle());
                });
    }

    @Test
    void findById_notFound() {
//        client.get().uri("/api/route/products/" + product1.getId())
        client.get().uri("/api/route/products/{id}", 99)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .isEmpty();
    }

    @Test
    void create() {
        var product = Product.builder()
                .title("Product Test")
                .price(20.0)
                .quantity(5)
//                .active(false)
//                .creationDate(LocalDateTime.now().minusDays(5))
                .manufacturerId(manufacturer.getId())
                .manufacturer(manufacturer)
                .build();

        client.post().uri("/api/route/products")
                .bodyValue(product)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("location", location -> {
                    assertNotNull(location);
                    assertTrue(location.startsWith("/api/route/products/"));
                })
                .expectBody(Product.class)
                .value(p -> {
                    assertNotNull(p);
                    assertNotNull(p.getId());
                    assertEquals(product.getTitle(), p.getTitle());
                    assertTrue(p.getActive());
                    assertNotNull(p.getCreationDate());
                });
    }

    @Test
    void update () {
        product1.setPrice(20.0);
        product1.setQuantity(60);
        client.put().uri("/api/route/products/{id}", product1.getId())
                .bodyValue(product1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .value(p -> {
                    assertNotNull(p);
                    assertEquals(product1.getId(), p.getId());
                    assertEquals(product1.getPrice(), p.getPrice());
                    assertEquals(product1.getQuantity(), p.getQuantity());
                });

        productRepository.findById(product1.getId()).subscribe(p -> {
            assertEquals(product1.getPrice(), p.getPrice());
            assertEquals(product1.getQuantity(), p.getQuantity());
        });

    }

    @Test
    void createAndUpdate() {
        var product = Product.builder()
                .title("Product Test")
                .price(20.0)
                .quantity(5)
                .build();

        var createdProduct = client.post().uri("/api/route/products")
                .bodyValue(product)
                .exchange()
                .expectBody(Product.class)
                .returnResult()
                .getResponseBody();

        createdProduct.setQuantity(10);
        createdProduct.setPrice(1500.0);

        client.put().uri("/api/route/products/{id}", createdProduct.getId())
                .bodyValue(createdProduct)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .value(p -> {
                    assertNotNull(p);
                    assertEquals(createdProduct.getId(), p.getId());
                    assertEquals(createdProduct.getPrice(), p.getPrice());
                    assertEquals(createdProduct.getQuantity(), p.getQuantity());
                });
    }

    @Test
    void delete() {
        client.delete()
                .uri("/api/route/products/{id}", product1.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();

        client.delete()
                .uri("/api/route/products/{id}", product1.getId())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .isEmpty();

        productRepository.existsById(product1.getId()).subscribe(Assertions::assertFalse);
    }



}