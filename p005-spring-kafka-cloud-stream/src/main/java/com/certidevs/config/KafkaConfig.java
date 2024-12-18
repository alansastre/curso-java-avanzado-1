package com.certidevs.config;

import com.certidevs.entity.Notification;
import com.certidevs.entity.Order;
import com.certidevs.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
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
//    @Bean
//    public Supplier<Flux<Product>> productProducer() {
//        return () -> Flux.interval(Duration.ofSeconds(5))
//                .map(tick -> {
//                    if (tick == 5) throw new RuntimeException("productProducer Excepción simulada");
//
//                    var product = new Product(tick, "Product_" + tick, 50.0 + tick);
//                    log.info("productProducer creado Product {}", product);
//                    return product;
//
//                }).onErrorContinue((e, o) -> {
//                    log.error("productProducer onErrorContinue ha lanzado excepción", e);
//                });
//    }
//
//    @Bean
//    public Consumer<Flux<Product>> productConsumer() {
//        return productFlux -> productFlux
//                .index()
//                .doOnNext(tuple -> {
//                    Long index = tuple.getT1();
//                    Product product = tuple.getT2();
//
//                    if(index == 5) throw new RuntimeException("productConsumer Excepción simulada");
//
//                    log.info("productConsumer doOnNext ha recibido Product {}", product);
//
//                    // guardar en base de datos, hacer operaciones ,etc
//
//                    // Para propagar la excepción a Spring cloud Stream y usar DLQ entonces no usaríamos el onErrorContinue
//                }).onErrorContinue((e, o) -> {
//                    log.error("productConsumer onErrorContinue ha lanzado excepción", e);
//                }).subscribe(tuple-> {
//                    log.info("productConsumer subscribe procesado Product {}", tuple.getT2());
//
//                });
//    }


    // Producer
    @Bean
    public Supplier<Flux<Order>> orderProducer() {
        return () -> Flux.interval(Duration.ofSeconds(5))
                .map(tick -> {

                    int amount = (int) (10 + tick);

                    if (tick == 5) {
                        amount = -5; // valor negativo para forzar un fallo controlado en el consumer orderProcessor
                    }

                    return new Order(tick, "deposit", amount);

                }).doOnNext(order -> log.info("orderProducer emitido order {}", order))
                .onErrorResume(e -> {
                    log.error("orderProducer error {}", e.getMessage());
                    return Flux.empty();
                });
    }

    // Processor SIN DQL: no propaga la excepción a Spring Cloud Stream, la procesa en el onErrorContinue
//    @Bean
//    public Function<Flux<Order>, Flux<Notification>> orderProcessor() {
//        return orderFlux -> orderFlux
//                .flatMap(order -> {
//                    if (order.amount() < 0) throw new RuntimeException("orderProcessor excepción simulada");
//
//                    // logica de negocio
//                    // procesar el order
//                    var notification = new Notification("admin@localhost", "Order OK.");
//                    log.info("Procesado Order OK. Notification {}", notification);
//                    return Flux.just(notification);
//
//                }).onErrorContinue((e, o) -> {
//                    log.error("orderProcessor onErrorContinue", e);
//                });
//    }
    // Processor CON DLQ: sí propaga la excepción, no la captura con onErrorContinue ni onErrorResume y la propaga a Spring cloud stream para DLQ:
    @Bean
    public Function<Order, Notification> orderProcessor() {
        return order -> {
            // Lanza y propaga la excepción a DLQ
            if (order.amount() < 0) throw new RuntimeException("orderProcessor excepción simulada");

            var notification = new Notification("admin@localhost", "Order OK.");
            log.info("Procesado Order OK. Notification {}", notification);
            return notification;
        };
    }


    // Consumer
    @Bean
    public Consumer<Flux<Notification>> notificationConsumer() {
        return notificationFlux -> notificationFlux
                .doOnNext(notification -> {
                    log.info("notificationConsumer ha recibido una notification {}", notification);

                    // usar el Java Mail Sender para enviar un correo electrónico o hacer un envío por sms
                })
                .onErrorContinue((e, o) -> log.error("notificationConsumer onErrorContinue", e))
                .subscribe(notification -> log.info("Notification procesada {}", notification));
    }


}
