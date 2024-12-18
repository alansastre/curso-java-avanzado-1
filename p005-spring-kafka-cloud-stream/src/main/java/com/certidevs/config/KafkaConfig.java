package com.certidevs.config;

import com.certidevs.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
Interfaces funcionales de Java:
* Supplier --> Productor de Kafka
* Consumer --> Consumidor de Kafka
* Function --> Consumir y producir de Kafka
 */
@Slf4j
@Configuration
public class KafkaConfig {

    // Producer: emite texto hacia kafka cada 5 segundos
//    @Bean
//    public Supplier<Flux<String>> stringProducer() {
//        return () -> Flux.interval(Duration.ofSeconds(5))
//                .map(tick -> "Texto número " + tick)
//                .doOnNext(texto -> log.info("stringProducer: Emitido texto {}", texto))
//                .onErrorResume(e -> {
//                    log.error("stringProducer ha ocurrido un error", e);
//                    return Flux.empty();
//                });
//    }
//
//    // Consumer: consume texto
//    @Bean
//    public Consumer<Flux<String>> stringConsumer() {
//        return stringFlux -> stringFlux
//                .doOnNext(texto -> {
//                log.info("stringConsumer doOnNext ha recibido texto: {}", texto);
//
//                // sacar datos de base de datos, operaciones, guardar en base de datos
//                // llamar con WebClient
//
//            }).onErrorContinue((e, o) -> {
//                log.error("stringConsumer Error ", e);
//            }).subscribe(texto -> {
//                log.info("stringConsumer subscribe ha recibido texto: {}", texto);
//
//                });
//    }

    // Emite productos, al quinto da un fallo, pero no se crashea y sigue emitiendo productos
    @Bean
    public Supplier<Flux<Product>> productProducer() {
        return () -> Flux.interval(Duration.ofSeconds(5))
                .map(tick -> {
                    if (tick == 5) throw new RuntimeException("productProducer Excepción simulada");

                    var product = new Product(tick, "Product_" + tick, 50.0 + tick);
                    log.info("productProducer creado Product {}", product);
                    return product;

                }).onErrorContinue((e, o) -> {
                    log.error("productProducer onErrorContinue ha lanzado excepción", e);
                });
    }

    @Bean
    public Consumer<Flux<Product>> productConsumer() {
        return productFlux -> productFlux
                .index()
                .doOnNext(tuple -> {
                    Long index = tuple.getT1();
                    Product product = tuple.getT2();

                    if(index == 5) throw new RuntimeException("productConsumer Excepción simulada");

                    log.info("productConsumer doOnNext ha recibido Product {}", product);

                    // guardar en base de datos, hacer operaciones ,etc

                    // Para propagar la excepción a Spring cloud Stream y usar DLQ entonces no usaríamos el onErrorContinue
                }).onErrorContinue((e, o) -> {
                    log.error("productConsumer onErrorContinue ha lanzado excepción", e);
                }).subscribe(tuple-> {
                    log.info("productConsumer subscribe procesado Product {}", tuple.getT2());

                });
    }


}
