package com.certidevs.concepts;


import com.certidevs.dto.PaginatedProductResponse;
import com.certidevs.dto.PaginatedResponse;
import com.certidevs.dto.ProductStoreDTO;
import com.certidevs.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
Clientes HTTP:

* HttpClient (+ Java 11)
* RestTemplate (Spring 3 síncrono)
* RestClient (Spring 6.1 síncrono)
* WebClient (Spring 5.0 reactivo)
* WebTestClient (Spring 5.0 reactivo envoltorio de WebClient específico para testing)
* OpenFeign (microservicios usando interfaces)

 */

public class WebClientTest {

//    private WebClient client = WebClient.create();
    private WebClient client = WebClient.create("https://fakestoreapi.com");
//    private WebClient client = WebClient.builder().defaultHeader("X-microservicio", "AAAAAA").build();

    // traer products

    @Test
    void findAll() {
//        Flux<Product> productFlux =  client.get().uri("/products")
//                .retrieve()
//                .bodyToFlux(Product.class)
//                .doOnNext(System.out::println);

        Flux<ProductStoreDTO> productFlux =  client.get().uri("/products")
                .retrieve()
                .bodyToFlux(ProductStoreDTO.class)
                .doOnNext(System.out::println);

        StepVerifier.create(productFlux)
                .expectNextCount(20)
                .verifyComplete();

    }

    @Test
    void findById() {
        // https://fakestoreapi.com/products/1
        Mono<Product> productMono = client.get().uri("/products/3")
                .retrieve()
                .bodyToMono(ProductStoreDTO.class)
//                .map(mapper::toEntity)
                .map(dto -> Product.builder()
                        .id(dto.id())
                        .title(dto.title())
                        .price(dto.price())
                        .quantity(0)
                        .active(false)
                        .build());

        productMono.subscribe(p -> {
            assertEquals(3, p.getId());
            assertEquals("Mens Cotton Jacket", p.getTitle());
        });
    }

    @Test
    void create() {
        var product = new ProductStoreDTO(
                null, "Product Test", 33.1, "test", "test", "test", null
        );

        Mono<ProductStoreDTO> createdProductMono = client.post().uri("/products")
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductStoreDTO.class)
                .onErrorResume(e -> Mono.empty());

        StepVerifier.create(createdProductMono)
                .expectNextMatches(p -> p.id().equals(21L))
                .verifyComplete();

    }

    @Test
    void createAndUpdate() {
        var product = new ProductStoreDTO(
                null, "Product Test", 33.1, "test", "test", "test", null
        );

        Mono<ProductStoreDTO> updated = client.post().uri("/products")
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductStoreDTO.class)
                .onErrorResume(e -> Mono.empty())
                .flatMap(p ->
                     client.put().uri("/products/{id}", p.id())
                            .bodyValue(new ProductStoreDTO(
                                    p.id(), "Product Test Editado", null, null, null, null, null
                            ))
                            .retrieve()
                            .bodyToMono(ProductStoreDTO.class)
                );

        StepVerifier.create(updated)
                .expectNextMatches(p -> p.title().equals("Product Test Editado"))
                .verifyComplete();

    }

    @Test
    void delete() {
//        Mono<Void> deleteMono = client.delete().uri("/products/1")
//                .retrieve()
//                .bodyToMono(Void.class)
//                .onErrorResume(e -> Mono.empty());

//        Mono<ResponseEntity<Void>> deleteMono = client.delete().uri("/products/1")
//                .retrieve()
//                .toBodilessEntity()
//                .onErrorResume(e -> Mono.empty());
//
//        StepVerifier.create(deleteMono)
//                .verifyComplete();

        Mono<Void> mono = client.delete()
                .uri("/products/1")
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new RuntimeException("Error al eliminar producto"));
                    }
                });

        StepVerifier.create(mono)
//                .expectError(RuntimeException.class)
                .verifyComplete();

//        StepVerifier.create(mono)
//                .expectError(RuntimeException.class)
//                .verify();


    }


    @Test
    void findAllPaginated() {
        Mono<PaginatedProductResponse> mono = client.get()
//                .uri("/api/route/products/paginated?page=2&size=5")
                .uri(
                        builder -> builder.path("/api/route/products/paginated").queryParam("page", 2).queryParam("size", 5).build()
                ).retrieve()
                .bodyToMono(PaginatedProductResponse.class)
                .doOnNext(res -> {
                    System.out.println(res.page());
                    System.out.println(res.size());
                });

        // TODO : necesario crear un setup en el que haya al menos 5 o más productos para probar la paginación
        StepVerifier.create(mono)
                .expectNextMatches(res -> res.page().equals(2) && res.size().equals(5) && res.products().size() == 5)
                .verifyComplete();
    }

    @Test
    @DisplayName("Cuando tenemos respuestas que usan Generics o que son mas complejas")
    void findAllPaginatedWithGenerics() {

        // Usamos tipo parametrizado para poder capturar los tipos con Generics
        var typeRef = new ParameterizedTypeReference<PaginatedResponse<Product>>() {};

        Mono<PaginatedResponse<Product>> mono = client.get()
//                .uri("/api/route/products/paginated?page=2&size=5")
                .uri(
                        builder -> builder.path("/api/route/products/paginated-with-generics").queryParam("page", 2).queryParam("size", 5).build()
                ).retrieve()
//                .bodyToMono(typeRef)
                .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<Product>>() {})
                .doOnNext(res -> {
                    List<Product> products = res.getItems();
                    System.out.println(res.getSize());
                    System.out.println(res.getTotal());
                });

        StepVerifier.create(mono)
                .expectNextMatches(res -> res.getPage().equals(2) && res.getSize().equals(5) && res.getItems().size() == 5)
                .verifyComplete();
    }



}
