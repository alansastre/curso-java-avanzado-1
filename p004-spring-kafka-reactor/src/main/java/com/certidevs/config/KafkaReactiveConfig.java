package com.certidevs.config;

import com.certidevs.entity.Transaction;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

@Configuration
public class KafkaReactiveConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, Transaction> transactionProducer(
            KafkaProperties properties,
            SslBundles sslBundles
    ) {

        // leer las application.properties:
        var producerProperties = properties.buildProducerProperties(sslBundles);

        // alternativa:
//        var producerProperties = Map.of(
//                "bootstrap.servers", "localhost:9092",
//                "key.serializer", "org...",
//                "value.serializer", "org..."
//        );
        SenderOptions<String, Transaction> senderOptions = SenderOptions.create(producerProperties);
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, Transaction> transactionConsumer(
            KafkaProperties properties,
            SslBundles sslBundles
    ){

        var consumerProperties = properties.buildConsumerProperties(sslBundles);

        ReceiverOptions<String, Transaction> receiverOptions =
                ReceiverOptions.<String, Transaction>create(consumerProperties)
                        .subscription(Collections.singleton("topic-transactions"));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);

    }
}
