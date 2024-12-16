#!/bin/bash

docker-compose -f src/main/docker/keycloak/docker-keycloak.yml up -d
# docker logs -f spring-keycloak