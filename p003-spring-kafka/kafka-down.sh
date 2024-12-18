#!/bin/bash

docker-compose -f src/main/docker/kafka/docker-kafka.yml down
# docker-compose -f src/main/docker/kafka/docker-kafka.yml down -v