package com.certidevs.route;

import com.certidevs.handler.ProductHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Rutas programáticas
 *
 * Al ser programático permite un mayor de personalización con respecto a las anotaciones tradicionales
 *
 * * Anidar rutas
 * * Crear filtros logging
 * * Crear filtros seguridad
 */
@Slf4j
@Configuration
public class ProductRouter {

    @Bean
    public RouterFunction<ServerResponse> route(ProductHandler handler) {

        HandlerFilterFunction<ServerResponse, ServerResponse> loggingFilter = (request, next) -> {
            log.info("Request: {} {}", request.method(), request.uri());
            return next.handle(request).doOnNext(response -> {
                log.info("Response status {}", response.statusCode());
            });
        };

        HandlerFilterFunction<ServerResponse, ServerResponse> authFilter = (request, next) -> {
            log.info("Request: {} {}", request.method(), request.uri());
            String authHeader = request.headers().firstHeader("Authorization");
            if(authHeader != null && authHeader.startsWith("Bearer ")) {
                // validar token JWT....
                return next.handle(request);
            } else {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue("Unauthorized");
            }
        };

        return RouterFunctions.route()
                .path("/api/route/products", builder -> builder
//                        .GET("", request -> handler.findAll(request)) // con lambda
                        .GET("", handler::findAll) // con metodo referenciado
                        .GET("/paginated", handler::findAllPaginated) // con metodo referenciado
                        .GET("/paginated-with-generics", handler::findAllPaginatedWithGeneric) // con metodo referenciado
                        .GET("{id}", handler::findById)
                        .POST("", handler::create)
                        .PUT("{id}", handler::update)
                        .DELETE("{id}", handler::deleteById)
                        .filter(loggingFilter)
                        .onError(IllegalArgumentException.class, (e, serverRequest) -> ServerResponse.badRequest().bodyValue("Invalid input"))
                        .onError(Exception.class, (e, serverRequest) -> ServerResponse.badRequest().bodyValue("Invalid input"))

                ).build();
    }
}
