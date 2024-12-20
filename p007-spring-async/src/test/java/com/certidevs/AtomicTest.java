package com.certidevs;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicTest {

    private int contador = 0;

    @Test
    void incrementarSinSincronizacion() throws InterruptedException {
        Runnable tarea = () -> {
            for (int i = 0; i < 1000; i++) {
                contador++;
            }
        };

        Thread hilo1 = new Thread(tarea);
        Thread hilo2 = new Thread(tarea);

        hilo1.start();
        Thread.sleep(5L);
        hilo2.start();

        hilo1.join();
        hilo2.join();

        // El valor podría ser diferente de 2000 debido a la condición de carrera.
        System.out.println("Valor del contador: " + contador);
    }


    // SOLUCION 1: Synchronized


    @Test
    void incrementarConSincronizacion() throws InterruptedException {
        Runnable tarea = () -> {
            for (int i = 0; i < 1000; i++) {
                incrementarContador();
            }
        };

        Thread hilo1 = new Thread(tarea);
        Thread hilo2 = new Thread(tarea);

        hilo1.start();
        hilo2.start();

        hilo1.join();
        hilo2.join();

        System.out.println("Valor del contador sincronizado: " + contador);
    }

    private synchronized void incrementarContador() {
        contador++;
    }


    //SOLUCION 2: Atomic, es thread safe por defecto, no necesitamos sincronizar manualmente
    private final AtomicInteger atomicCounter = new AtomicInteger(0);

    @Test
    void incrementarConAtomic() throws InterruptedException {
        Runnable tarea = () -> {
            for (int i = 0; i < 1000; i++) {
                atomicCounter.incrementAndGet();
            }
        };

        Thread hilo1 = new Thread(tarea);
        Thread hilo2 = new Thread(tarea);

        hilo1.start();
        hilo2.start();

        hilo1.join();
        hilo2.join();

        System.out.println("Valor del contador atomic: " + contador);
    }

}
