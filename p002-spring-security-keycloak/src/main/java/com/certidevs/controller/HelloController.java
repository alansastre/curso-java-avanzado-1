package com.certidevs.controller;

import com.certidevs.service.HelloService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@AllArgsConstructor
public class HelloController {

    private final HelloService helloService;

    @GetMapping("/hello0")
    public Mono<Authentication> hello0(Mono<Authentication> authentication) {
        log.info("hello0: {}", authentication);
        return authentication;
    }

    @GetMapping("/hello1")
    public Mono<Authentication> hello1(Mono<Authentication> authentication) {
        log.info("hello1: {}", authentication);
        return authentication;
    }

    @GetMapping("/hello2")
    public Mono<Authentication> hello2(Mono<Authentication> authentication) {
        log.info("hello2: {}", authentication);
        return authentication;
    }

    @GetMapping("/hello3")
    public Mono<Authentication> hello3(Mono<Authentication> authentication) {
        log.info("hello3: {}", authentication);
        return authentication;
    }

    @GetMapping("/hello4")
    public Mono<Authentication> hello4() {
       return helloService.doSomething();
    }
}
