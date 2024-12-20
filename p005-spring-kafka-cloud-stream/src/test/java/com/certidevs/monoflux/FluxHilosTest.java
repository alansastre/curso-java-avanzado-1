package com.certidevs.monoflux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/*
Schedulers.boundedElastic() proporciona un Scheduler elástico con un pool de hilos que crece según la demanda hasta cierto límite. Está pensado para tareas bloqueantes o con latencias variables, como llamadas a sistemas externos o I/O no reactivo.
Al usar publishOn(Schedulers.boundedElastic()), le estamos diciendo a Reactor que a partir de ese operador, las señales (onNext, onComplete, onError) se procesen en el pool elástico de hilos.
Esto es útil, por ejemplo, si antes veníamos generando datos en un Scheduler distinto (como uno inmediante o paralelo) y queremos, a partir de cierto punto, realizar operaciones que puedan bloquear, sin afectar otros flujos.


Schedulers.parallel(): Un pool de hilos limitado y diseñado para operaciones no bloqueantes, CPU-bound.
Schedulers.single(): Un único hilo dedicado para tareas secuenciales.
Schedulers.boundedElastic(): Un pool elástico para tareas potencialmente bloqueantes.
Schedulers.immediate(): Ejecuta en el mismo hilo que llama (sin cambio real de hilos).

publishOn vs subscribeOn:
subscribeOn determina el Scheduler en el que se inicia la suscripción y por ende, el pipeline entero desde la fuente hacia abajo (a menos que luego se cambie con un publishOn).
publishOn cambia el Scheduler a partir del punto en el que se inserta el operador, sin afectar la fuente anterior.

 */
public class FluxHilosTest {

    /**
     * Ejemplo 1:
     * Producer -> emit rápido
     * Consumer -> procesa lento
     * publishOn(Schedulers.parallel()) para procesar el stream en un pool paralelo
     *
     * Observamos en qué hilo se ejecutan las acciones doOnNext.
     */
    @Test
    void testPublishOnParallel() {
        Flux<Integer> flux = Flux.range(1, 5)
                .doOnNext(i -> log("Emitiendo: " + i)) // Se ejecuta antes del publishOn
                .publishOn(Schedulers.parallel()) // Cambia a hilos del pool paralelo
                .doOnNext(i -> log("Procesando (parallel): " + i))
                .delayElements(Duration.ofMillis(100)) // Simula consumo lento
                .doOnComplete(() -> log("Completado"));

        flux.blockLast(); // Bloqueamos para ver la ejecución
    }

    /**
     * Ejemplo 2:
     * Uso de boundedElastic:
     * Producer rápido, consumer lento.
     * publishOn(Schedulers.boundedElastic()) para tareas potencialmente bloqueantes.
     *
     * Imaginemos que el consumo implica una operación bloqueante (simulada con sleep).
     */
    @Test
    void testPublishOnBoundedElastic() {
        Flux<Integer> flux = Flux.range(1, 5)
                .doOnNext(i -> log("Emitiendo: " + i))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    log("Procesando (boundedElastic): " + i);
                    simulateBlockingOperation(50); // simula bloqueo
                })
                .doOnComplete(() -> log("Completado"));

        flux.blockLast();
    }

    /**
     * Ejemplo 3:
     * Uso de single:
     * publishOn(Schedulers.single()) mueve el procesamiento a un único hilo dedicado.
     */
    @Test
    void testPublishOnSingle() {
        Flux<Integer> flux = Flux.range(1, 5)
                .doOnNext(i -> log("Emitiendo: " + i))
                .publishOn(Schedulers.single())
                .doOnNext(i -> log("Procesando (single): " + i))
                .doOnComplete(() -> log("Completado"));

        flux.blockLast();
    }

    /**
     * Ejemplo 4:
     * Sin publishOn pero con subscribeOn:
     * subscribeOn(Schedulers.parallel()) hará que todo el pipeline, incluyendo la emisión, se ejecute en hilos del pool paralelo.
     * Pero si luego usamos publishOn, podemos cambiar nuevamente el contexto de ejecución.
     */
    @Test
    void testSubscribeOnAndPublishOn() {
        Flux<Integer> flux = Flux.range(1, 5)
                .doOnNext(i -> log("Emitiendo (antes de subscribeOn): " + i))
                .subscribeOn(Schedulers.parallel()) // fuerza la suscripción y emisión en hilos paralelos
                .doOnNext(i -> log("Después de subscribeOn (parallel): " + i))
                .publishOn(Schedulers.boundedElastic()) // cambiamos el contexto a boundedElastic a mitad del flujo
                .doOnNext(i -> {
                    log("Procesando (boundedElastic): " + i);
                    simulateBlockingOperation(20);
                })
                .doOnComplete(() -> log("Completado"));

        flux.blockLast();
    }

    /**
     * Ejemplo 5:
     * Uso de immediate:
     * publishOn(Schedulers.immediate()) en realidad no cambia de hilo,
     * se ejecutará todo en el hilo actual. Suele usarse para testing o debugging.
     */
    @Test
    void testPublishOnImmediate() {
        Flux<Integer> flux = Flux.range(1, 5)
                .doOnNext(i -> log("Emitiendo: " + i))
                .publishOn(Schedulers.immediate()) // no cambia de hilo, todo en el mismo hilo
                .doOnNext(i -> log("Procesando (immediate): " + i))
                .doOnComplete(() -> log("Completado"));

        flux.blockLast();
    }

    /**
     * Ejemplo 6:
     * Combinación de varios publishOn en cadena:
     * Primero parallel, luego boundedElastic, se irá aplicando el scheduler más reciente.
     * Esto muestra cómo el contexto puede cambiar varias veces a lo largo del flujo.
     */
    @Test
    void testMultiplePublishOn() {
        Flux<Integer> flux = Flux.range(1, 5)
                .doOnNext(i -> log("Emitiendo: " + i))
                .publishOn(Schedulers.parallel())
                .doOnNext(i -> log("Procesando (parallel): " + i))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    log("Procesando (boundedElastic): " + i);
                    simulateBlockingOperation(10);
                })
                .doOnComplete(() -> log("Completado"));

        flux.blockLast();
    }

    // Utilidades para logging y simulación

    private void simulateBlockingOperation(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Ignorar en test
        }
    }

    private void log(String message) {
        System.out.println(Thread.currentThread().getName() + " - " + message);
    }

}
