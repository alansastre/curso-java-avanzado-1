

## Spring Security

* Backend Monolítico
  * Genera tokens JWT (librerías jjwt)
  * Firma tokens JWT
  * Valida el token JWT que le llega en las peticiones en la cabecera Authorization
  * Filtro de Spring

* Backend microservicios
  * Delega la autenticación a OAuth 2 a Keycloak, Okta
  * Te llega una petición y se le pasa a Keycloak que nos responde con la información del usuario


Clases de Spring Security para Spring Web:

* SecurityFilterChain
* SecurityContextHolder
* HttpSecurity
* OncePerRequestFilter
* UsernamePasswordAuthenticationFilter
* AuthenticationManager
* UserDetailsService

Clases de Spring Security para spring WebFlux:

* SecurityWebFilterChain
* ReactiveSecurityContextHolder
* ServerHttpSecurity
* WebFilter o HandlerFilterFunction<ServerResponse, ServerResponse> authFilter
* AuthenticationWebFilter
* ReactiveAuthenticationManager
* ReactiveUserDetailsService

## Seguridad con KEYCLOAK para microservicios:

* Dependencias:
  * spring-boot-starter-oauth2-resource-server
  * spring-boot-starter-security

* Creada carpeta src/main/docker con todo lo necesario para levantar un Keycloak servidor de autenticación con docker-compose.
  * Tiene dos usuarios ya creados: testuser, adminuser

* applications.properties: configurar acceso al realm de Keycloak

* SecurityConfig: creada seguridad de rutas centralizada

* KeycloakRolesConverter: converter customizado para leer roles de Keycloak y pasarlo a roles de Spring

* HelloController: ejemplo de obtención de Authentication, lo inyecta spring

* HelloService: ejemplo de obtención de Authentication de ReactiveSecurityContextHolder


## Seguridad con jjwt para monolítico:

* jjwt-api, jjwt-impl, jjwt-jackson
* spring-boot-starter-security

* UserController: 
  * register: crea usuario, cifra contraseña con BCryptPasswordEncoder, asignar rol y guarda usuario en base de datos
  * login: comprobar credenciales, crear token JWT y firmarlo con clave secreta y devolverlo

* El cliente, por ejemplo Angular, recibe el token, lo guarda en localStorage y lo envía en las peticiones http en la cabecera Authorization

* Spring Filtro para extraer token de las peticiones HTTP y validarlo y obtener usuario a partir del token y añadir el usuario al Contexto de seguridad para que esté accesible en toda la aplicación.

* SecurityConfig para seguridad centralizada

* SecurityUtils para sacar el usuario autenticado

* Seguridad a nivel de método: @Secured, @PreAuthorize, @PostAuthorize