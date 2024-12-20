

## CONCURRENCIA

* Concurrencia básica:
  * https://github.com/certidevs/java-se-jakarta-ee/tree/main/11.%20Concurrencia/java-concurrency

* Thread (>= JDK 1.0)
  * Extendiendo la clase Thread
  * Implementar la clase Runnable: creando una clase o utilizando una lambda 
* start, run, join, isAlive, interrupt, sleep

A partir de >= JDK 21 la clase Thread tiene nuevos métodos para tratamiendo de hilos virtuales:

* Thread.ofVirtual().start(task);
* isVirtual

* synchronized o volatile

* Variables o datos Atomic: java.util.concurrent.atomic ejemplo AtomicInteger

* Colecciones concurrentes:
  * Framework Collections tiene también estructuras concurrentes
  * https://github.com/certidevs/java-se-jakarta-ee/tree/main/12.%20Colecciones%20concurrentes/java-concurrency-collections


* Concurrencia avanzada:
  * https://github.com/certidevs/java-se-jakarta-ee/tree/main/21.%20Concurrencia%20avanzada/java-concurrency-advanced

* Callable (>= JDK 1.5)
  * Puede lanzar excepción
  * Puede retornar un resultado

* Ejecución paralela:
  * https://github.com/certidevs/java-se-jakarta-ee/tree/main/22.%20Paralelizaci%C3%B3n/java-parallelism
  * Framework ForkJoin: ForkJoinPool, RecursiveAction, RecursiveTask
  * Stream parallel: Stream.parallel()

* Executors
* ExecutorService: awaitTermination, shutdown 

* Future: get(), resultNow...

* CompletableFuture (>= JDK 1.8)
  * Permite operaciones más avanzadas combinando distintos future y consumiendo sus resultados

* Flow (>= JDK 9) para implementar Reactive Streams

* RxJava: Observable, Maybe, Single (2012)

* Mono y Flux (Project Reactor 2015) Integrado de forma nativa en Spring WebFlux
  * subscribeOn
  * publishOn

* Schedulers de Reactor

## SPRING ASYNC

Spring Web normal, sin webflux.

* Crear Beans de tipo Executor
* Ejecutar métodos en Executors utilizando @Async
* Por ejemplo:
  * Llega una petición a un API REST, para realizar un reporte en PDF que tarda 5 minutos en generarse.
  * Lo habitual es llamar a un servicio método @Async que mueve la ejecución a otro hilo worker que no sea el mismo de la request, así no bloqueamos el hilo request y puede seguir atendiendo peticiones.
  * El resultado luego se puede mandar a kafka, o se puede mandar a un websocket, o se puede mandar por correo smtp.

* La tareas con @Async pueden devolver CompletableFuture.