package com.certidevs.monoflux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/*

flatMap() lanza las operaciones en paralelo (o concurrentemente) y los resultados pueden llegar desordenados.
concatMap() procesa cada elemento secuencialmente, el siguiente elemento no se procesa hasta que el anterior haya terminado, por lo que mantiene el orden.
flatMapSequential(): paraleliza internamente el trabajo, pero emite los resultados al consumidor final en el orden original, combinando así paralelismo interno con orden garantizado en la salida.
parallel() (con runOn()) permite procesamiento en paralelo, pero si las operaciones tienen diferentes tiempos de respuesta, el orden de llegada puede variar. Normalmente, tras parallel() y runOn(), usar sequential() vuelve a un flujo secuencial pero en el orden que vayan llegando los resultados desde los diferentes "rails" paralelos.

 */
public class SequentialParallelTest {

    /**
     * Ejemplo 1: Procesamiento Secuencial (por defecto)
     */
    @Test
    void testSequentialProcessing() {
        Flux<Integer> source = Flux.range(1, 5)
                .map(i -> {
                    log("Procesando secuencial: " + i);
                    simulateWork(10);
                    return i * 10;
                });

        List<Integer> result = source.collectList().block();
        System.out.println("Resultado secuencial: " + result);
        // Orden conservado: [10, 20, 30, 40, 50]
    }

    /**
     * Ejemplo 2: Procesamiento paralelo con flatMap (no garantiza orden)
     */
    @Test
    void testParallelWithFlatMap() {
        Flux<Integer> source = Flux.range(1, 5);

        Flux<Integer> transformed = source
                .flatMap(i -> Flux.just(i)
                        .map(j -> {
                            simulateWork(i * 20);
                            log("Procesando flatMap: " + j);
                            return j * 10;
                        })
                        .subscribeOn(Schedulers.parallel())
                );

        List<Integer> result = transformed.collectList().block();
        System.out.println("Resultado con flatMap (orden no garantizado): " + result);
        // Ejemplo de posible salida desordenada: [30, 50, 40, 10, 20]
    }

    /**
     * Ejemplo 3: Procesamiento secuencial garantizando orden con concatMap
     */
    @Test
    void testSequentialWithConcatMap() {
        Flux<Integer> source = Flux.range(1, 5);

        Flux<Integer> transformed = source
                .concatMap(i -> Flux.just(i)
                        .map(j -> {
                            simulateWork(i * 20);
                            log("Procesando concatMap: " + j);
                            return j * 10;
                        })
                        .subscribeOn(Schedulers.parallel())
                );

        List<Integer> result = transformed.collectList().block();
        System.out.println("Resultado con concatMap (orden conservado): " + result);
        // Orden conservado: [10, 20, 30, 40, 50]
    }

    /**
     * Ejemplo 4: Procesamiento con parallel() y runOn(), el orden puede perderse
     */
    @Test
    void testParallelRunOn() {
        Flux<Integer> source = Flux.range(1, 5);

        List<Integer> result = source
                .parallel(2)
                .runOn(Schedulers.parallel())
                .map(i -> {
                    simulateWork(i * 20);
                    log("Procesando parallel: " + i);
                    return i * 10;
                })
                .sequential()
                .collectList()
                .block();

        System.out.println("Resultado con parallel/runOn: " + result);
        // El orden puede variar dependiendo de la velocidad de cada rail.
    }

    /**
     * Ejemplo 5: Mantener el orden tras procesar en paralelo re-ordenando manualmente
     */
    @Test
    void testParallelWithReordering() {
        Flux<Integer> source = Flux.range(1, 5);

        List<Integer> result = source
                .map(i -> new IndexedValue<>(i, i))
                .parallel(2)
                .runOn(Schedulers.parallel())
                .map(iv -> {
                    simulateWork(iv.value() * 20);
                    log("Procesando parallel con índice: " + iv.index() + " valor: " + iv.value());
                    return new IndexedValue<>(iv.index(), iv.value() * 10);
                })
                .sequential()
                .sort((a, b) -> Integer.compare(a.index(), b.index()))
                .map(IndexedValue::value)
                .collectList()
                .block();

        System.out.println("Resultado con parallel y reordenamiento: " + result);
        // Finalmente re-ordenamos para volver a [10, 20, 30, 40, 50].
    }

    /**
     * Ejemplo 6: Uso de flatMapSequential
     *
     * flatMapSequential procesa internamente de forma similar a flatMap (en paralelo),
     * pero garantiza emitir los resultados hacia abajo en orden.
     */
    @Test
    void testParallelWithFlatMapSequential() {
        Flux<Integer> source = Flux.range(1, 5);

        Flux<Integer> transformed = source.flatMapSequential(i ->
                Flux.just(i)
                        .map(j -> {
                            simulateWork(i * 20);
                            log("Procesando flatMapSequential: " + j);
                            return j * 10;
                        })
                        .subscribeOn(Schedulers.parallel())
        );

        List<Integer> result = transformed.collectList().block();
        System.out.println("Resultado con flatMapSequential (orden conservado): " + result);
        // Aunque haya procesamiento paralelo, la emisión final está en orden: [10, 20, 30, 40, 50]
    }

    // Utilidades

    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Ignorar en test
        }
    }

    private void log(String msg) {
        System.out.println(Thread.currentThread().getName() + " - " + msg);
    }

    // Clase auxiliar para mantener índice y valor
    static class IndexedValue<T> {
        private final int index;
        private final T value;

        IndexedValue(int index, T value) {
            this.index = index;
            this.value = value;
        }

        public int index() { return index; }
        public T value() { return value; }
    }

}
