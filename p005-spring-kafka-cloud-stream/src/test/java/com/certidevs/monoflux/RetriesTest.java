package com.certidevs.monoflux;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/*
retry() / retry(n): Reintenta la suscripción al flujo n veces si se produce un error. Sin usar retryWhen, reintenta inmediatamente sin delay.
retryWhen(Retry retrySpec): Permite mayor control sobre las condiciones de reintento, tiempos de espera (backoff), límite de reintentos, etc.
onErrorResume(...): Permite proporcionar un fallback si después de los reintentos falla definitivamente.
repeat(): Repite el flujo tras completarse con éxito. No es exactamente un reintento de error, sino una repetición del flujo exitoso, pero se menciona como parte del ecosistema de reintentos y repeticiones.
 */
public class RetriesTest {

    // Simulación de un servicio remoto. En un caso real usarías WebClient, algo así:
    // private WebClient webClient;
    // Aquí simularemos un "endpoint" con un contador de intentos para fallar algunas veces.
    private AtomicInteger attemptCounter;
    private AtomicInteger callCounter;
    private WebClient webClient;

    @BeforeEach
    void setup() {
        callCounter = new AtomicInteger(0);
        attemptCounter = new AtomicInteger(0);
        webClient = WebClient.create("http://localhost:8080"); // Ejemplo, si tuvieras un endpoint real
    }

    @AfterEach
    void teardown() {
        // Limpieza si fuese necesario
    }

    /**
     * Simula una llamada a un servicio remoto que falla las 2 primeras veces y a la 3ª funciona.
     * En un entorno real, esto sería un .get() con WebClient:
     * webClient.get().uri("/data")
     *   .retrieve()
     *   .bodyToMono(String.class)
     */
    private Mono<String> simulatedRemoteCall() {
        int attempt = callCounter.incrementAndGet();
        if (attempt <= 2) {
            return Mono.error(new RuntimeException("Simulated remote error, attempt " + attempt));
        } else {
            return Mono.just("Success on attempt " + attempt);
        }
    }

    /**
     * Ejemplo 1: Reintento simple con retry(n)
     * Reintenta 2 veces antes de fallar definitivamente.
     */
    @Test
    void testSimpleRetry() {
        Mono<String> result = simulatedRemoteCall()
                .retry(2) // Hasta 2 reintentos adicionales
                .doOnError(e -> System.err.println("Error definitivo tras 2 reintentos: " + e.getMessage()))
                .doOnNext(v -> System.out.println("Recibido: " + v));

        StepVerifier.create(result)
                .expectNext("Success on attempt 3") // Falló en attempt 1 y 2, reintenta y en attempt 3 success
                .expectComplete()
                .verify();
    }

    /**
     * Ejemplo 2: Reintento con backoff exponencial
     * Usa retryWhen y un Retry backoff: reintenta hasta 3 veces, aumentando el tiempo entre reintentos.
     */
    @Test
    void testRetryBackoff() {
        // Reiniciamos el contador para simular fallos en las 2 primeras llamadas
        callCounter.set(0);

        Mono<String> result = simulatedRemoteCall()
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100)) // hasta 3 reintentos, con backoff inicial de 100ms
                                .maxBackoff(Duration.ofSeconds(1)) // máximo de 1s entre reintentos
                                .doBeforeRetry(retrySignal ->
                                        System.out.println("Reintentando, intento: " + (retrySignal.totalRetries() + 1))
                                )
                )
                .doOnNext(v -> System.out.println("Recibido (con backoff): " + v));

        StepVerifier.create(result)
                .expectNext("Success on attempt 3")
                .expectComplete()
                .verify();
    }

    /**
     * Ejemplo 3: Reintentar indefinidamente hasta el primer éxito
     *
     * Uso de retryWhen con un Retry.infinite() (o un retryWhen personalizado) que no limite el número de reintentos.
     * Ojo: esto puede llevar a reintentos infinitos si el servicio nunca responde correctamente.
     */
    @Test
    void testInfiniteRetryUntilSuccess() {
        // Para este ejemplo, haremos que la 5ª llamada funcione.
        callCounter.set(0);

        Mono<String> result = Mono.defer(this::simulatedRemoteCall)
                .retryWhen(Retry.indefinitely().doBeforeRetry(rs -> System.out.println("Reintento infinito...")))
                .doOnNext(v -> System.out.println("Recibido (infinito): " + v));

        StepVerifier.create(result)
                .expectNext("Success on attempt 5") // espera hasta el quinto intento exitoso
                .expectComplete()
                .verify();
    }

    /**
     * Ejemplo 4: Fallback si todos los reintentos fallan
     *
     * Si tras N reintentos sigue fallando, recurrimos a un fallback local con onErrorResume.
     */
    @Test
    void testFallbackOnFailure() {
        // Fuerza el error: nunca llega al éxito (por ejemplo, hacemos que falle siempre)
        callCounter.set(0);
        Mono<String> alwaysFail = Mono.defer(() -> Mono.error(new RuntimeException("Permanent failure")));

        Mono<String> result = alwaysFail
                .retry(3) // Intenta 3 veces
                .onErrorResume(e -> {
                    System.err.println("Todas las reintentos fallaron, usando fallback local.");
                    return Mono.just("Fallback data");
                })
                .doOnNext(v -> System.out.println("Recibido (fallback): " + v));

        StepVerifier.create(result)
                .expectNext("Fallback data")
                .expectComplete()
                .verify();
    }

    /**
     * Ejemplo 5: Uso de WebClient simulado
     *
     * Suponiendo que tuviésemos un endpoint real, podríamos hacer:
     *
     * WebClient webClient = WebClient.create("http://localhost:8080");
     * Mono<String> remoteCall = webClient.get().uri("/data")
     *     .retrieve()
     *     .bodyToMono(String.class)
     *     .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
     *     .onErrorResume(WebClientResponseException.class, e -> {
     *         System.err.println("HTTP error: " + e.getStatusCode() + " - fallback");
     *         return Mono.just("Fallback response due to HTTP error");
     *     });
     *
     * Este bloque es sólo ilustrativo, sin StepVerifier porque dependería de un servidor real.
     * Aquí mostraremos un test simulado, asumiendo que el remoteCall es nuestro "producer".
     */
    @Test
    void testWebClientRetryScenario() {
        // Ejemplo simulado: vamos a crear una fuente que falle 2 veces con error HTTP y luego devuelva éxito.
        // En la vida real, esto sería el resultado de webClient.
        callCounter.set(0);
        Mono<String> simulatedWebClientCall = Mono.defer(() -> {
            int attempt = callCounter.incrementAndGet();
            if (attempt <= 2) {
                // Simulamos un error HTTP (ej: 500 Internal Server Error)
                return Mono.error(new RuntimeException("Simulated 500 Internal Server Error"));
            } else {
                return Mono.just("OK from server on attempt " + attempt);
            }
        });

        Mono<String> result = simulatedWebClientCall
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(50))) // Reintenta 2 veces con un delay fijo de 50ms
                .onErrorResume(e -> {
                    System.err.println("Tras reintentos, fallo definitivo: " + e.getMessage());
                    return Mono.just("WebClient fallback response");
                })
                .doOnNext(v -> System.out.println("Recibido (webClient scenario): " + v));

        StepVerifier.create(result)
                .expectNext("OK from server on attempt 3")
                .expectComplete()
                .verify();
    }

    /**
     * Ejemplo 6: repeat() no es un reintento de error, sino que repite el flujo tras completarse con éxito.
     * Aquí lo mostramos para completar la visión de reintentos/repeticiones.
     */
    @Test
    void testRepeatAfterSuccess() {
        // Este flujo no falla nunca.
        Mono<String> successfulCall = Mono.fromSupplier(() -> {
            int attempt = callCounter.incrementAndGet();
            return "Data " + attempt;
        });

        Flux<String> repeated = successfulCall
                .repeat(2) // Repite el flujo 2 veces después del primer éxito, total 3 emisiones
                .doOnNext(v -> System.out.println("Recibido (repeat): " + v));

        StepVerifier.create(repeated)
                .expectNext("Data 1", "Data 2", "Data 3")
                .expectComplete()
                .verify();
    }

    /**
     * Este test asume un endpoint remoto en "http://localhost:9999/data".
     * En un escenario real:
     * - Podrías usar un MockWebServer o WireMock para simular:
     *   - Las dos primeras peticiones devuelven 500 (Internal Server Error).
     *   - La tercera petición devuelve 200 con un cuerpo "OK".
     *
     * Lógica del test:
     * - Hacer GET a /data.
     * - En caso de error 5xx, reintentar hasta 2 veces con un retraso fijo.
     * - Si tras 2 reintentos sigue fallando, hacer fallback.
     * - Verificar que finalmente se recibe el contenido esperado.
     *
     * Como no tenemos un servidor real en este ejemplo,
     * explicamos el flujo esperando que se adaptaría a un entorno con servidor mock.
     */
    @Test
    void testWebClientRetryWithBackoffAndFallback() {
        // Simularemos la lógica interna usando onErrorResume para generar errores simulados.
        // En un escenario real, simplemente llamas al endpoint y este responde con error HTTP real.

        Mono<String> call = Mono.defer(() -> {
            int attempt = attemptCounter.incrementAndGet();
            if (attempt <= 2) {
                // Simulamos una respuesta de error HTTP 500
                return Mono.error(new WebClientResponseException(
                        "Simulated Server Error", 500, "Internal Server Error", null, null, null));
            } else {
                // A partir del tercer intento, simulamos éxito con respuesta "OK"
                return Mono.just("OK");
            }
        });

        Mono<String> result = call
                // En un caso real:
                //   webClient.get().uri("/data")
                //       .retrieve()
                //       .bodyToMono(String.class)
                //       .<...> aquí abajo operadores ...

                .retryWhen(
                        Retry.fixedDelay(2, Duration.ofMillis(100))
                                .filter(ex -> ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError())
                                .onRetryExhaustedThrow((spec, signal) -> signal.failure())
                        // Filtramos para reintentar sólo si es 5xx
                        // Si tras 2 reintentos sigue fallando, se lanza el último error
                )
                .onErrorResume(WebClientResponseException.class, ex -> {
                    System.err.println("Todas las reintentos fallaron con error: " + ex.getStatusCode());
                    // Fallback local, por ejemplo devolver una respuesta cacheada o por defecto
                    return Mono.just("Fallback response due to persistent error");
                })
                .doOnNext(value -> System.out.println("Recibido: " + value));

        // Verificamos el comportamiento:
        // - Dado que en el tercer intento devolvemos "OK", el retry debe terminar con éxito en el tercer intento:
        StepVerifier.create(result)
                .expectNext("OK")
                .expectComplete()
                .verify();
    }

    /**
     * Ejemplo adicional: Reintento infinito hasta el primer éxito con backoff exponencial.
     *
     * En un entorno real, ten cuidado con los reintentos infinitos, podrían nunca acabar.
     * Este ejemplo es ilustrativo.
     */
    @Test
    void testWebClientInfiniteRetryWithExponentialBackoff() {
        // Simulamos que falla muchas veces hasta el intento 5 (en un entorno real el servidor tardaría en responder correctamente)
        attemptCounter.set(0);
        Mono<String> call = Mono.defer(() -> {
            int attempt = attemptCounter.incrementAndGet();
            if (attempt < 5) {
                return Mono.error(new WebClientResponseException(
                        "Simulated Server Error", 500, "Internal Server Error", null, null, null));
            } else {
                return Mono.just("Finally OK at attempt " + attempt);
            }
        });

        Mono<String> result = call
                //. Or, in a real scenario:
                // webClient.get().uri("/data")
                //   .retrieve()
                //   .bodyToMono(String.class)
                //   .retryWhen(...)

                .retryWhen(
                        Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100)) // reintenta indefinidamente con backoff exponencial
                                .maxBackoff(Duration.ofSeconds(5)) // limita el backoff máximo
                                .filter(ex -> ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError())
                )
                .doOnNext(value -> System.out.println("Recibido con retry infinito: " + value));

        StepVerifier.create(result)
                .expectNext("Finally OK at attempt 5")
                .expectComplete()
                .verify();
    }

    /**
     * Ejemplo: Reintentar un número fijo de veces ante errores 500,
     * y si es un error 400 (4xx), no reintentar y hacer fallback inmediato.
     */
    @Test
    void testWebClientConditionalRetryFor5xxOnly() {
        attemptCounter.set(0);

        Mono<String> call = Mono.defer(() -> {
            int attempt = attemptCounter.incrementAndGet();
            if (attempt == 1) {
                // Simula un error 400 Bad Request
                return Mono.error(new WebClientResponseException(
                        "Bad Request", 400, "Bad Request", null, null, null));
            } else {
                // Simula éxito en el segundo intento
                return Mono.just("OK on attempt " + attempt);
            }
        });

        Mono<String> result = call
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(50))
                        .filter(ex -> ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().is4xxClientError()) {
                        System.out.println("Error 4xx detectado, no se reintenta, fallback inmediato");
                        return Mono.just("Fallback due to client error");
                    }
                    return Mono.error(ex); // Si no es 4xx ni 5xx, dejar pasar el error
                })
                .doOnNext(value -> System.out.println("Recibido (condicional): " + value));

        StepVerifier.create(result)
                .expectNext("Fallback due to client error")
                .expectComplete()
                .verify();
    }

    // Método utilitario para logging (opcional)
    private void log(String message) {
        System.out.println(Thread.currentThread().getName() + " - " + message);
    }


    // Utilidad para simular latencia si se desea
    private void simulateLatency(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignorar
        }
    }
}
