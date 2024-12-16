package com.certidevs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class HelloService {

    public Mono<Authentication> doSomething() {

        // imprimir o accede al Authentication
        // Spring Web: SecurityContextHolder
        // Spring WebFlux: ReactiveSecurityContextHolder
//        ReactiveSecurityContextHolder.getContext().subscribe(securityContext -> {
//            log.info("isAuthenticated: {}", securityContext.getAuthentication().isAuthenticated());
//            log.info("getAuthorities: {}", securityContext.getAuthentication().getAuthorities());
//
//        });

        // Se puede crear una securityutils class que devuelva directamente por ejemplo un Mono<User>
        return ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication);
    }
}
