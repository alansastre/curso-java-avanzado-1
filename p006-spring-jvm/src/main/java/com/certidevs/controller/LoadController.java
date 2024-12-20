package com.certidevs.controller;

import com.certidevs.service.LoadService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadController {

    private final LoadService loadService;

    public LoadController(LoadService loadService) {
        this.loadService = loadService;
    }

    /**
     * Endpoint específico para lanzar peticiones con Apache JMeter usando el plan.jmx
     * para ver el performance de la aplicación con 1000 usuarios concurrentes durante 100 segundos
     *
     * Probar con hilos normales, probar con la configuración de Spring boot de hilos virtuales
     *
     * Con hilos normales, configuración configuración normal, spring tarda 5s de latencia en las peticiones
     * Con hilos virtuales, configuración virtual threads enabled true, spring se mantiene en 1s de latencia que es lo que hemos puesto aquí
     * @throws InterruptedException
     */
    @GetMapping("jmeter")
    public void doSomething() throws InterruptedException {
        Thread.sleep(1000);
    }

    /**
     * Endpoint para generar carga de CPU.
     * @param n Número de iteraciones para el cálculo.
     * @return Resultado del cálculo.
     */
    @GetMapping("/cpu/{n}")
    public String generateCpuLoad(@PathVariable int n) {
        loadService.generateCPULoadWithNormalThreads(n);
        return "Calculando primos durante " + n + " segundos.";
    }

    // http://localhost:8080/load/normal?seconds=10&numTasks=50
    @GetMapping("/load/normal")
    public String generateNormalThreadLoad(
            @RequestParam int seconds,
            @RequestParam int numTasks) {
        loadService.generateIOBoundLoadWithNormalThreads(seconds, numTasks);
        return "Load with normal threads completed!";
    }

    // Reinciar app desde cero, se observa que consumen menos memoria
    // http://localhost:8080/load/virtual?seconds=10&numTasks=50
    @GetMapping("/load/virtual")
    public String generateVirtualThreadLoad(
            @RequestParam int seconds,
            @RequestParam int numTasks) {
        loadService.generateIOBoundLoadWithVirtualThreads(seconds, numTasks);
        return "Load with virtual threads completed!";
    }


    /**
     * Hilos Tradicionales:
     *
     *     Usan un pool limitado (10 en este caso), por lo que el sistema procesa un número pequeño de tareas concurrentemente.
     *     Esto hace que la ejecución sea más lenta y uniforme, con un menor impacto en la memoria y el CPU.
     *     Sin embargo, si el número de tareas aumentara significativamente, los hilos tradicionales no escalarían bien (verías colas de espera y mayores tiempos de respuesta).
     *
     * Hilos Virtuales:
     *
     *     Los hilos virtuales permiten ejecutar miles de tareas en paralelo sin restricciones de un pool fijo.
     *     Esto lleva a un uso más intensivo de la CPU y la memoria en un periodo corto, pero permite completar las tareas más rápidamente.
     *     El alto número de threads reportados no corresponde a hilos del sistema operativo, sino a hilos virtuales manejados eficientemente por la JVM.
     * @param numTasks
     * @return
     */
    // http://localhost:8080/load/network/normal?numTasks=500
    @GetMapping("/load/network/normal")
    public String simulateNetworkWithNormalThreads(@RequestParam int numTasks) {
        loadService.simulateNetworkWithNormalThreads(numTasks);
        return "Network simulation with normal threads completed!";
    }

    // http://localhost:8080/load/network/virtual?numTasks=500
    @GetMapping("/load/network/virtual")
    public String simulateNetworkWithVirtualThreads(@RequestParam int numTasks) {
        loadService.simulateNetworkWithVirtualThreads(numTasks);
        return "Network simulation with virtual threads completed!";
    }

    /**
     * Endpoint para generar consumo de memoria.
     * @param size Tamaño del array a crear en MB.
     * @return Mensaje de éxito.
     */
    // http://localhost:8080/memory/100
    @GetMapping("/memory/{size}")
    public String generateMemoryLoad(@PathVariable int size) {
        loadService.allocateMemory(size);
        return "Asignación de memoria de tamaño " + size + " MB completada.";
    }

    /**
     * Endpoint para generar carga de hilos.
     * @param n Número de hilos a crear.
     * @return Mensaje de creación de hilos.
     */
    // http://localhost:8080/threads/100
    @GetMapping("/threads/{n}")
    public String generateThreadLoad(@PathVariable int n) {
        loadService.createThreads(n);
        return "Creación de " + n + " hilos completada.";
    }

    /**
     * Endpoint para generar carga de I/O.
     * @param n Número de operaciones de I/O a realizar.
     * @return Mensaje de finalización de I/O.
     */
    // http://localhost:8080/io/100
    @GetMapping("/io/{n}")
    public String generateIoLoad(@PathVariable int n) {
        loadService.performIoOperations(n);
        return "Realizadas " + n + " operaciones de I/O.";
    }

    /**
     * Endpoint para generar excepciones.
     * @param n Número de excepciones a lanzar.
     * @return Mensaje de finalización de excepciones.
     */
    // http://localhost:8080/exceptions/2000
    @GetMapping("/exceptions/{n}")
    public String generateExceptions(@PathVariable int n) {
        loadService.generateExceptions(n);
        return "Generadas " + n + " excepciones.";
    }

    /**
     * Endpoint para generar carga de Garbage Collection.
     * @param n Número de ciclos de asignación y liberación de memoria.
     * @return Mensaje de finalización de carga de GC.
     */
    // http://localhost:8080/gc/2000
    @GetMapping("/gc/{n}")
    public String generateGcLoad(@PathVariable int n) {
        loadService.generateGcLoad(n);
        return "Generada carga de GC con " + n + " ciclos.";
    }
}