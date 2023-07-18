#!/usr/bin/env bash

mkdir microservices
cd microservices

spring init \
--boot-version=2.1.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=post-service \
--package-name=se.magnus.microservices.core.post \
--groupId=se.magnus.microservices.core.post \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
post-service

spring init \
--boot-version=2.1.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=comment-service \
--package-name=se.magnus.microservices.core.comment \
--groupId=se.magnus.microservices.core.comment \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
comment-service

spring init \
--boot-version=2.1.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=reaction-service \
--package-name=se.magnus.microservices.core.reaction \
--groupId=se.magnus.microservices.core.reaction \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
reaction-service

spring init \
--boot-version=2.1.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=image-service \
--package-name=se.magnus.microservices.core.image \
--groupId=se.magnus.microservices.core.image \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
image-service

spring init \
--boot-version=2.1.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=post-composite-service \
--package-name=se.magnus.microservices.composite.post \
--groupId=se.magnus.microservices.composite.post \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
post-composite-service

cd ..