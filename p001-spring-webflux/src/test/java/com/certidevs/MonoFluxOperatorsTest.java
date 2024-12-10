package com.certidevs;

import com.certidevs.entity.Manufacturer;
import com.certidevs.entity.Product;
import com.certidevs.repository.ManufacturerRepository;
import com.certidevs.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataR2dbcTest
public class MonoFluxOperatorsTest {

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


    /*
     *  Operaciones map, flatMap, flatMapMany
     *  Se usan para transformaciones sobre los datos o extraer datos
     *
     * Los flat fusiona los Mono/Flux que devuelven las operaciones internas que se hacen en map al mismo contexto reactivo
     */


    @Test
    void monoMap() {
//        Mono<Product> productMono = productRepository.findById(product1.getId());
//        Mono<Product> productMonoEdited = productMono.map(product -> {
//            double price = product.getPrice() != null ? product.getPrice() : 0.0;
//            product.setPrice(price + 1); // simular una operación sobre el precio
//            return product;
//        });

//        var productMonoEdited = productRepository.findById(product1.getId())
//                .map(product -> {
//                    double price = product.getPrice() != null ? product.getPrice() : 0.0;
//                    product.setPrice(price + 1); // simular una operación sobre el precio
//                    return product;
//                });
//
//        StepVerifier.create(productMonoEdited)
//                .expectNextMatches(p -> p.getPrice().equals(11.0))
//                .verifyComplete();
        productRepository.findById(product1.getId())
                .map(p -> {
                    double price = p.getPrice() != null ? p.getPrice() : 0.0;
                    p.setPrice(price + 1); // simular una operación sobre el precio
                    return p;
                }).map(p -> {
                    int quantity = p.getQuantity() != null ? p.getQuantity() : 0;
                    p.setQuantity(quantity + 1); // simular una operación sobre la cantidad
                    return p;
                }).as(StepVerifier::create)
                .expectNextMatches(p -> p.getPrice().equals(11.0) && p.getQuantity().equals(51))
                .verifyComplete();
    }

    /**
     * repositorio devuelve cold publisher por lo que te puedes suscribir varias veces
     * Icluso se puede cachear con cache() el resultado
     *
     * block: se suscribe, bloquea el hilo hasta el flujo emita el dato, devuelve el resultado, cada vez que se invoca
     * se fuerza una nueva suscripción al flujo, se reinicia de nuevo
     *
     * StepVerifier: consume y agota el flujo
     */
    @Test
    @DisplayName("flatMap se usa para encadenar otra operación que también devuelve un Mono")
    void monoFlatMap() {

        // map: Mono<Mono<Product>>
//        Mono<Product> productMono = productRepository.findById(product1.getId())
//                .map(p -> {
//                    double price = p.getPrice() != null ? p.getPrice() : 0.0;
//                    p.setPrice(price + 1); // simular una operación sobre el precio
//                    return productRepository.save(p); // Este devuelve Mono
//                });

        // flatMap: Mono<Product>
        Mono<Product> productMono = productRepository.findById(product1.getId())
                .flatMap(p -> {
                    double price = p.getPrice() != null ? p.getPrice() : 0.0;
                    p.setPrice(price + 1); // simular una operación sobre el precio
                    return productRepository.save(p); // Este devuelve Mono
                });

        StepVerifier.create(productMono)
                .expectNextMatches(p -> p.getPrice().equals(11.0))
            .verifyComplete();

    }


    @Test
    void fluxFlatMapMany() {

        // map: Mono<Flux<Product>>
//        Flux<Product> productFlux = manufacturerRepository.findByName(manufacturer.getName())
//                .map(manufacturer -> productRepository.findByManufacturerId(manufacturer.getId()));

        // flatMapMany: Flux<Product>
        Flux<Product> productFlux = manufacturerRepository.findByName(manufacturer.getName())
                .flatMapMany(manufacturer -> productRepository.findByManufacturerId(manufacturer.getId()));

        StepVerifier.create(productFlux)
                .expectNextCount(2)
                .verifyComplete();

        List<Product> products= productRepository.findByManufacturerId(manufacturer.getId()).collectList().block();
        assertNotNull(products);
        assertTrue(products.stream().allMatch(p -> p.getManufacturerId().equals(manufacturer.getId())));
    }


    @Test
    void fluxFilter() {

//        productRepository.findAllByActiveTrue()

        // Flux<Product> productFlux = productRepository.findAll().filter(p -> p.getActive());
        Flux<Product> productFlux = productRepository.findAll().filter(Product::getActive);
        StepVerifier.create(productFlux).expectNextCount(2).verifyComplete();
    }

    @Test
    void fluxFilterAndMap() {

        Flux<Double> productFlux = productRepository.findAll()
                .filter(Product::getActive)
                .map(Product::getPrice) // 10.0, 30.0
                .filter(price -> price > 10)
                .map(price -> price * 0.90);

        StepVerifier.create(productFlux).expectNextMatches(price -> price.equals(27.0)).verifyComplete();
    }




    // diferencia block, subscribe, stepverifier y agotar flujo, just, error

    // cache()
}
