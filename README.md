# resolog

## Introduction

resolog comes from resonance and catalog. It comes from the noise that happens when an
event is put in the catalog, and how that ends up with every listener.

It is an event-driven backend service for a music product catalog. Built using
Java, Spring Boot, Kafka, Redis, and MariaDB for persistence. It exposes
a RESTful API for managing music products and publishes domain events through Kafka,
when state changes occur in the catalog.