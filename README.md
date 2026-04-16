# resolog

## Introduction

resolog comes from resonance and catalog. It comes from the noise that happens when an event is put in the catalog, and
how that ends up with every listener.

It is an event-driven backend service for a music product catalog. Built using Java, Spring Boot, Kafka, Redis, and
MariaDB for persistence. It exposes a RESTful API for managing music products and publishes domain events through Kafka,
when state changes occur in the catalog.

## Scope

The scope of this service is to be the central repository for artists and their music products. It is responsible for
the following:
* Registering artists and their music products
* Managing music product lifecycle (creation, update, deletion)
* Validating the music product is complete before being published and rejecting incomplete products
* Emitting domain events for when state changes happen
* Consumers that react to domain events are stubbed as logs unless otherwise noted.

### Out of scope

The scope of this service does not include:
* Music product content moderation (copyright, deepfakes, explicit content)
* Music product publishing to other streaming platforms (Apple, Spotify, YouTube, SoundCloud)

## Success criteria

## Architectural decisions

### Model

The catalog here holds the state of all the music products. Each music product can be of type Album, EP or Single.
An Album is a collection of Tracks, with no strict duration constraint. An EP is a collection of Tracks with a
duration of maximum 30 minutes. A Single is a single Track. It also holds the release date, label, genre, and copyright.

A Track is the data model that holds the metadata and audio content of a music product. It contains the artists that
are featured on the track, the track duration, the track number, and the title.

An Artist is the entity that represents a person or group that created the music product. It contains the artist's name,
biography, label, and a list of music products they have created.

## What I would do differently
