

# APACHE KAFKA

* Apache Kafka middleware plataforma distribuida para publicar, suscribir y procesar flujos de datos en tiempo real.

Componentes:

* Broker: servidor kafka almacena y distribuye los mensajes
* Topics: sería como una tabla, donde se publican y consumen los mensajes. Se crean manualmente o se crean desde Spring.
* Partición: subdivisión de un topic que tiene un registro ordenado de mensajes. Distribuir la carga.
* Mensaje: datos que enviamos y consumimos de kafka, habitualmente suele ser un objeto json. Se produce una serialización y una deserialización.
* Producers: un componente que envía datos a kafka
* Consumers: un componente que recibe datos de kafka
* Grupos de consumidores: conjunto de consumidores que comparten la carga de leer mensajes de un topic.



## INSTALACIÓN

### OPCIÓN 1

Descargarse los binarios y ejecutar los scripts:

https://kafka.apache.org/downloads

https://kafka.apache.org/documentation/#configuration

### OPCIÓN 2

Docker compose que levante un Kafka all in con todo montado.

https://github.com/confluentinc/cp-all-in-one/blob/7.8.0-post/cp-all-in-one-kraft/docker-compose.yml

./kafka-up.sh

Entrar en http://localhost:9021 esto accede a Control Center la UI para Kafka

Otra alternativa es entrar con IntelliJ IDEA Ultimate con el Plugin de Big Data Tools


## DEPENDENCIAS PARA JAVA - SPRING

Si solamente usamos Java sin Spring:

* https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
* https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams

Dependencia para Spring Web (no reactivo):

* https://mvnrepository.com/artifact/org.springframework.kafka/spring-kafka
* https://mvnrepository.com/artifact/org.springframework.kafka/spring-kafka-test

Dependencia para Spring WebFlux (reactivo):

* https://mvnrepository.com/artifact/io.projectreactor.kafka/reactor-kafka

Dependencias para Spring Cloud (microservicio):

* https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-stream
* https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-stream-binder-kafka

Dependencias para Testing con Docker:

* https://mvnrepository.com/artifact/org.testcontainers/kafka

## SPRING KAFKA:

* Producer: KafkaTemplate<K, V>
* Consumer: @KafkaListener
