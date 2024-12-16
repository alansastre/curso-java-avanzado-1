package com.certidevs.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
Permite leer la respuesta JSON recibida de Keycloak y procesarla de forma personalizada para crear un Authentication

Por ejemplo necesitamos que los roles tengan prefijo ROLE_ para que Spring Security los pueda manejar
 */
public class KeycloakRolesConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt source) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        Map<String, Object> realmAccess = source.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Object rolesObject = realmAccess.get("roles");
            if (rolesObject instanceof List) {
                List<String> roles = (List<String>) rolesObject;
                authorities.addAll(
                        roles.stream()
                                .map(role -> "ROLE_" + role.toUpperCase())
                                .map(SimpleGrantedAuthority::new)
                                .toList())
                ;
            }
        }

        // También se puede extraer otros claims y procesarlos para agregar otros authorities
        // authorities.add()
        // authorities.add()

        return Mono.just(new JwtAuthenticationToken(source, authorities));
    }
}
