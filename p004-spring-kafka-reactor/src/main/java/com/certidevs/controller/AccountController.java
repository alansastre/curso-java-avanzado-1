package com.certidevs.controller;

import com.certidevs.entity.Account;
import com.certidevs.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

// http://localhost:8080/webjars/swagger-ui/index.html#
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private AccountService accountService;

    @GetMapping("{id}")
    public Mono<ResponseEntity<Account>> findById(@PathVariable Long id) {
        return accountService.findById(id).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Account>> create(@RequestBody Account account) {
        return accountService.create(account).map(ResponseEntity::ok);
    }


}
