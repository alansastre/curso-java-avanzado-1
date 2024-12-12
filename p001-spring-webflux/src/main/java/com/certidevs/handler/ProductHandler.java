package com.certidevs.handler;

import com.certidevs.dto.PaginatedProductResponse;
import com.certidevs.entity.Product;
import com.certidevs.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

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
    // paginación
    public Mono<ServerResponse> findAllPaginated(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(1);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        int offset = (page - 1) * size;

        return ServerResponse.ok().body(
                productService.count()
                .flatMap(total -> productService
                    .findAll()
                    .skip(offset)
                    .take(size)
                    .collectList()
                    .map(products -> new PaginatedProductResponse(
                            products,
                            page,
                            size,
                            total
                    ))
        ), PaginatedProductResponse.class);


    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return productService.findById(id)
                .flatMap(p -> ServerResponse.ok().bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(Product.class)
                .flatMap(p -> productService.save(p))
                .flatMap(p ->
                        ServerResponse
                                .created(URI.create("/api/route/products/" + p.getId()))
                                .bodyValue(p)
                ).onErrorResume(e -> ServerResponse.status(HttpStatus.CONFLICT).build());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return request.bodyToMono(Product.class)
                .flatMap(p -> productService.update(id, p))
                .flatMap(p -> ServerResponse.ok().bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(e -> ServerResponse.status(HttpStatus.CONFLICT).build());

    }

    public Mono<ServerResponse> deleteById(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return productService.findById(id)
                .flatMap(p -> productService.deleteById(id)
                        .then(ServerResponse.noContent().build())
                )
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(e -> ServerResponse.status(HttpStatus.CONFLICT).build());
    }





}
