

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

*  

Clases de Spring Security para spring WebFlux:


## KEYCLOAK

* Creada carpeta src/main/docker con todo lo necesario para levantar un Keycloak servidor de autenticación con docker-compose.
  * Tiene dos usuarios ya creados: testuser, adminuser

* applications.properties: configurar acceso al realm de Keycloak

* SecurityConfig