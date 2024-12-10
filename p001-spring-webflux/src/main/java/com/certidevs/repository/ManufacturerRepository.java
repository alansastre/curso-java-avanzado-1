package com.certidevs.repository;

import com.certidevs.entity.Manufacturer;
import com.certidevs.entity.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ManufacturerRepository extends ReactiveCrudRepository<Manufacturer, Long> {

    Mono<Manufacturer> findByName(String name);
}
