package com.certidevs.config;


import com.certidevs.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;


/*
Probar con Kafka en TestContainers a crear un test para probar la propagación de las headers
 */
@Slf4j
@Configuration
public class KafkaMessagesConfig {


    @Bean
    public Supplier<Message<Product>> productProducer() {
        return () -> {
            var product = new Product(1L, "Laptop", 500d);
            return MessageBuilder.withPayload(product).setHeader("X-Custom-Header", "ValorPrueba").build();
        };
    }

    @Bean
    public Consumer<Message<Product>> productConsumer() {
        return productMessage -> {
            var product = productMessage.getPayload();
            String header = (String) productMessage.getHeaders().get("X-Custom-Header");
            log.info("productConsumer recibido payload {} y header {}", product, header);
        };
    }

    @Bean
    public Supplier<Flux<Message<Product>>> productProducerFlux() {
        return () -> Flux.interval(Duration.ofSeconds(5)).map(tick -> {
            var product = new Product(1L, "Laptop", 500d);
            return MessageBuilder.withPayload(product).setHeader("X-Custom-Header", "ValorPrueba").build();
        }).doOnNext(productMessage -> {
            log.info("");
        });
    }

    @Bean
    public Consumer<Flux<Message<Product>>> productConsumerFlux() {
        return flux -> flux
                .concatMap(message -> {
                    var product = message.getPayload();
                    String header = (String) message.getHeaders().get("X-Custom-Header");

                    return WebClient.create("").get().retrieve()
                            .bodyToMono(String.class)
                            .doOnNext(s -> log.info(""))
                            .doOnError(throwable -> log.error(""))
//                            .retry(2)
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).doBeforeRetry(retrySignal -> log.info("reintento")))
                            .onErrorContinue((throwable, o) -> log.error(""))
                            .map(response -> {
                                return MessageBuilder.withPayload(product).build();
                            }).then();
                }).subscribe(ok -> log.info(""), error -> log.error(""), () -> log.info(""));
    }

}
