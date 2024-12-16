package com.certidevs.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/*
* Spring Web: SecurityFilterChain, HttpSecurity
* Spring WebFlux: SecurityWebFilterChain, ServerHttpSecurity
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

//    @Bean
//    public MapReactiveUserDetailsService userDetailsService() {
//        UserDetails user = User. withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//        return new MapReactiveUserDetailsService(user);
//    }

    /*
    Centraliza la seguridad y acceso seguro a cada ruta y metodo HTTP con roles específicos.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http.csrf(csrf -> csrf.disable())
                .authorizeExchange(
                        exchanges -> exchanges
                                .pathMatchers("/hello0", "/hello1").permitAll()
                                .pathMatchers("/hello2").hasRole("USER")
                                .pathMatchers("/hello3").hasRole("ADMIN")
                                .pathMatchers("/hello4").hasAnyRole("USER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/api/products/**").hasRole("USER")
                                .pathMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.GET, "/api/ratings/**").hasRole("USER")
                                .pathMatchers(HttpMethod.POST, "/api/ratings/**").hasRole("USER")
                                .pathMatchers(HttpMethod.PUT, "/api/ratings/**").hasRole("USER")
                                .pathMatchers(HttpMethod.DELETE, "/api/ratings/**").hasRole("ADMIN")
                                .anyExchange()
                                .authenticated() // Para acceder a todas las demás rutas tienes que estar autenticado, da igual si es USER o ADMIN
                )
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtSpec ->
//                        jwtSpec.jwkSetUri("http://localhost:8080/realms/myrealm/protocol/openid-connect/certs")
                                jwtSpec.jwtAuthenticationConverter(keycloakRolesConverter())
                ));

        return http.build();
    }

    /*
    Este convertidor que hemos creado es específico para Keycloak, si se usa otro proveedor como Okta quizás haya que modificarlo para adaptarse a las respuestas del nuevo Authorization Server
     */
    // Convertidor para tener roles con prefijo ROLE_ como GrantedAuthority
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> keycloakRolesConverter() {
        return new KeycloakRolesConverter();
    }

}
