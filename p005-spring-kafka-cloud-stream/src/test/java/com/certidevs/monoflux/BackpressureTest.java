package com.certidevs.monoflux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/*

onBackpressureBuffer(): El productor puede sobreproducir, y el consumidor es lento. Se almacenan los elementos en un buffer interno hasta que el consumidor pueda procesarlos.
onBackpressureDrop(): Si el consumidor no puede mantener el ritmo, los mensajes que lleguen sin demanda se descartan directamente.
onBackpressureLatest(): Cuando el consumidor es lento, se mantienen solo los últimos mensajes, descartando los anteriores para que el consumidor siempre reciba la información más reciente.
onBackpressureError(): Si el consumidor es lento y el productor genera más de lo que se puede demandar, se lanza un error inmediatamente.

 */
public class BackpressureTest {


    /**
     * Escenario:
     * - Productor emite 100 mensajes muy rápido (cada 1ms).
     * - Consumidor procesa un mensaje cada 10ms, más lento que el productor.
     *
     * Operador: onBackpressureBuffer()
     * Significado: Los mensajes se almacenan en un buffer interno hasta que el consumidor pueda procesarlos.
     * Resultado esperado: El consumidor terminará recibiendo los 100 mensajes, aunque tarde más, sin perder ninguno.
     */
    @Test
    void testOnBackpressureBufferScenario() {
        Flux<Integer> producer = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(1)) // Emite rápido
                .publishOn(Schedulers.boundedElastic()) // Publica en otro hilo

                // Aplica el operador de backpressure (buffer)
                .onBackpressureBuffer()

                .doOnSubscribe(s -> System.out.println("[Buffer] Subscrito al flujo de producción"))
                .doOnNext(i -> System.out.println("[Buffer] Recibido en consumidor: " + i))
                .doOnComplete(() -> System.out.println("[Buffer] Todos los mensajes procesados"))
                .doOnError(e -> System.err.println("[Buffer] Error: " + e));

        // Consumidor lento: Simulamos que cada mensaje tarda 10ms en procesarse.
        Flux<Integer> consumer = producer
                .delayElements(Duration.ofMillis(10));

        // si queremos backpressure a nivel de consumer se puede usar también rateLimit

        // Ejecutar el flujo hasta que termine
        consumer.blockLast();
    }


    /**
     * Escenario:
     * - Productor: 100 mensajes a 1ms.
     * - Consumidor: lento (10ms por mensaje).
     *
     * Operador: onBackpressureDrop()
     * Significado: Si no hay demanda del consumidor, se descartan los mensajes que siguen llegando.
     * Resultado esperado: El consumidor recibirá sólo unos cuantos mensajes (los primeros y algunos intercalados
     * si el timing lo permite), el resto se perderá cuando el consumidor no tenga demanda.
     */
    @Test
    void testOnBackpressureDropScenario() {
        Flux<Integer> producer = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(1))
                .publishOn(Schedulers.boundedElastic())

                // onBackpressureDrop descarta elementos si el consumidor va lento
                .onBackpressureDrop(dropped -> System.out.println("[Drop] Mensaje descartado: " + dropped))

                .doOnSubscribe(s -> System.out.println("[Drop] Subscrito al flujo de producción"))
                .doOnNext(i -> System.out.println("[Drop] Recibido en consumidor: " + i))
                .doOnComplete(() -> System.out.println("[Drop] Flujo completado"))
                .doOnError(e -> System.err.println("[Drop] Error: " + e));

        Flux<Integer> consumer = producer
                .delayElements(Duration.ofMillis(10));

        consumer.blockLast();
    }

    /**
     * Escenario:
     * - Productor: 100 mensajes a 1ms.
     * - Consumidor: lento (10ms por mensaje).
     *
     * Operador: onBackpressureLatest()
     * Significado: Si no hay demanda suficiente, se descartan mensajes antiguos y se conserva el más reciente.
     * Resultado esperado: El consumidor verá unos pocos mensajes iniciales y luego saltará a recibir sólo
     * los más recientes que han ido llegando durante la "falta de demanda", perdiendo los intermedios.
     */
    @Test
    void testOnBackpressureLatestScenario() {
        Flux<Integer> producer = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(1))
                .publishOn(Schedulers.boundedElastic())

                // onBackpressureLatest mantiene sólo el último mensaje recibido si no hay demanda
                .onBackpressureLatest()

                .doOnSubscribe(s -> System.out.println("[Latest] Subscrito al flujo de producción"))
                .doOnNext(i -> System.out.println("[Latest] Recibido en consumidor: " + i))
                .doOnComplete(() -> System.out.println("[Latest] Flujo completado"))
                .doOnError(e -> System.err.println("[Latest] Error: " + e));

        Flux<Integer> consumer = producer
                .delayElements(Duration.ofMillis(10));

        consumer.blockLast();
    }

    /**
     * Escenario:
     * - Productor: 100 mensajes a 1ms.
     * - Consumidor: lento (10ms por mensaje).
     *
     * Operador: onBackpressureError()
     * Significado: Si el consumidor no puede con la carga, se lanza un error.
     * Resultado esperado: El flujo fallará en algún momento, lanzando una excepción porque el consumidor no pudo
     * consumir a la velocidad necesaria.
     */
    @Test
    void testOnBackpressureErrorScenario() {
        Flux<Integer> producer = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(1))
                .publishOn(Schedulers.boundedElastic())

                // onBackpressureError lanza un error cuando no se puede soportar la presión
                .onBackpressureError()

                .doOnSubscribe(s -> System.out.println("[Error] Subscrito al flujo de producción"))
                .doOnNext(i -> System.out.println("[Error] Recibido en consumidor: " + i))
                .doOnComplete(() -> System.out.println("[Error] Flujo completado"))
                .doOnError(e -> System.err.println("[Error] Se produjo un error por backpressure: " + e));

        Flux<Integer> consumer = producer
                .delayElements(Duration.ofMillis(10));

        try {
            consumer.blockLast();
        } catch (Exception e) {
            // Esperamos un error en algún momento.
            System.out.println("[Error] Capturamos la excepción del flujo: " + e.getMessage());
        }
    }
}
