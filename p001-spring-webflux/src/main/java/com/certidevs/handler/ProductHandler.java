package com.certidevs.handler;

import com.certidevs.entity.Product;
import com.certidevs.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
Manejador para las rutas de API de Product
 */
@Slf4j
@AllArgsConstructor
@Component
public class ProductHandler {

    private ProductService productService;

    public Mono<ServerResponse> findAll(ServerRequest request) {

        return ServerResponse.ok().body(
                productService.findAll(), Product.class
        );
    }
}
