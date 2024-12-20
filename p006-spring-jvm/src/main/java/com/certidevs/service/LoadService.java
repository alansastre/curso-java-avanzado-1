package com.certidevs.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class LoadService {

    private final List<byte[]> memoryHog = new ArrayList<>();
    private final List<Thread> threadList = new ArrayList<>();
    private final ExecutorService ioExecutor = Executors.newFixedThreadPool(10);
    private final ExecutorService exceptionExecutor = Executors.newFixedThreadPool(10);

    // Metodo que genera carga en la CPU durante un número específico de segundos
    public void generateCPULoadWithNormalThreads(int seconds) {
        int numThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads); // Pool de hilos

        // Tiempo de finalización
        Instant endTime = Instant.now().plusSeconds(seconds);

        // Crear tareas para cada hilo
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                while (Instant.now().isBefore(endTime)) {
                    findPrimesUpTo(10_000); // Cada hilo realiza el cálculo intensivo
                }
            });
        }

        // Apagar el pool de hilos después de terminar
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Espera a que todos los hilos terminen
        }
    }

    // Método para encontrar números primos (cálculo intensivo)
    private void findPrimesUpTo(int limit) {
        for (int i = 2; i <= limit; i++) {
            isPrime(i);
        }
    }

    // Verifica si un número es primo
    private boolean isPrime(int number) {
        if (number < 2) return false;
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) return false;
        }
        return true;
    }

    public void generateIOBoundLoadWithNormalThreads(int seconds, int numTasks) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10); // Pool limitado de 10 hilos

        // Tiempo de finalización
        Instant endTime = Instant.now().plusSeconds(seconds);

        for (int i = 0; i < numTasks; i++) {
            executor.submit(() -> {
                while (Instant.now().isBefore(endTime)) {
                    simulateNetworkRequest(); // Simula una tarea I/O-bound
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Espera a que las tareas terminen
        }
    }

    public void generateIOBoundLoadWithVirtualThreads(int seconds, int numTasks) {
        var executor = Executors.newVirtualThreadPerTaskExecutor(); // Virtual Threads

        // Tiempo de finalización
        Instant endTime = Instant.now().plusSeconds(seconds);

        for (int i = 0; i < numTasks; i++) {
            executor.submit(() -> {
                while (Instant.now().isBefore(endTime)) {
                    simulateNetworkRequest(); // Simula una tarea I/O-bound
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Espera a que las tareas terminen
        }
    }

//    private void simulateNetworkRequest() {
//        try {
//            // Simula una espera como una llamada a un servicio externo
//            Thread.sleep(100); // Espera de 100ms
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }


    public void simulateNetworkWithNormalThreads(int numTasks) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10); // Pool limitado a 10 hilos

        for (int i = 0; i < numTasks; i++) {
            executor.submit(this::simulateNetworkRequest); // Cada tarea realiza una operación de red simulada
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Espera a que todas las tareas terminen
        }
    }

    public void simulateNetworkWithVirtualThreads(int numTasks) {
        var executor = Executors.newVirtualThreadPerTaskExecutor(); // Usamos hilos virtuales

        for (int i = 0; i < numTasks; i++) {
            executor.submit(this::simulateNetworkRequest); // Cada tarea realiza una operación de red simulada
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Espera a que todas las tareas terminen
        }
    }

    private void simulateNetworkRequest() {
        try {
            // Simula una llamada HTTP a un servicio externo
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://httpbin.org/delay/0.1")) // Simula un retraso de 100ms en la respuesta
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            throw new RuntimeException("Error durante la simulación de la red", e);
        }
    }


    /**
     * Calcula el factorial de un número de forma iterativa para generar carga de CPU.
     * @param n Número para calcular el factorial.
     * @return Factorial de n.
     */
    public long calculateFactorial(int n) {
        if (n < 0) throw new IllegalArgumentException("n debe ser no negativo");
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Asigna una cantidad de memoria específica para generar carga de memoria.
     * @param size Tamaño en megabytes.
     */
    public void allocateMemory(int size) {
        // Cada elemento del array representa 1 MB
        for (int i = 0; i < size; i++) {
            memoryHog.add(new byte[1024 * 1024]); // 1 MB
        }
    }

    /**
     * Crea una cantidad específica de hilos que ejecutan una tarea infinita.
     * @param n Número de hilos a crear.
     */
    public void createThreads(int n) {
        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    // Hilo en espera activa
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "LoadThread-" + i);
            thread.start();
            threadList.add(thread);
        }
    }

    /**
     * Realiza operaciones de I/O simples para generar carga de I/O.
     * @param n Número de operaciones de I/O a realizar.
     */
    public void performIoOperations(int n) {
        for (int i = 0; i < n; i++) {
            ioExecutor.submit(() -> {
                File file = new File("temp_io_file.txt");
                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write("Escribiendo datos para generar carga de I/O.\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Genera una cantidad específica de excepciones para generar carga de logs y stack traces.
     * @param n Número de excepciones a generar.
     */
    public void generateExceptions(int n) {
        for (int i = 0; i < n; i++) {
            exceptionExecutor.submit(() -> {
                try {
                    throw new RuntimeException("Excepción generada para pruebas de monitorización.");
                } catch (RuntimeException e) {
                    // Manejar la excepción para evitar detener la aplicación
                    // Puedes registrar la excepción si lo deseas
                    // e.g., Logger
                }
            });
        }
    }

    /**
     * Genera carga de Garbage Collection mediante asignación y liberación rápida de memoria.
     * @param n Número de ciclos de asignación y liberación de memoria.
     */
    public void generateGcLoad(int n) {
        for (int i = 0; i < n; i++) {
            byte[] array = new byte[10 * 1024 * 1024]; // 10 MB
            // No guardamos referencia, permitiendo que sea elegible para GC
        }
    }
}
