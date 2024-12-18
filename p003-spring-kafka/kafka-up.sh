#!/bin/bash

docker-compose -f src/main/docker/kafka/docker-kafka.yml up -d
# docker logs -f broker