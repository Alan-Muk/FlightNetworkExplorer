# Flight Network Map

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![Python](https://img.shields.io/badge/Python-3.12-3776AB?logo=python)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql)
![NetworkX](https://img.shields.io/badge/Graph-NetworkX-blue)
![License](https://img.shields.io/badge/License-MIT-green)

A distributed flight intelligence platform that aggregates aviation data, models flight routes as graph structures, and provides interactive network visualization.

The system combines backend services, external API integration, persistence, graph processing, and frontend visualization into a complete full-stack architecture.

Built with:

- Spring Boot
- React
- PostgreSQL
- Python Flask
- NetworkX

---

# Overview

Flight Network App explores how real-world aviation data can be transformed into a searchable and visual graph-based system.

The platform workflow:

```text
Flight Data Provider

        ↓

Spring Boot API Gateway

        ↓

Data Processing + Persistence

        ↓

Python Graph Processing Service

        ↓

Graph Data

        ↓

React Visualization Dashboard
```

---

# Problem

Flight networks represent complex relationships between locations.

Traditional search interfaces provide individual results but do not expose the underlying network structure.

This project models aviation data as a graph to enable:

- route exploration
- connectivity analysis
- network visualization
- graph-based reasoning

---

# System Architecture

```text
                    React Frontend

                          |
                          ↓

                 Spring Boot Backend

             /              |              \

            ↓               ↓               ↓

    Flight API        PostgreSQL      Graph Service

                        

                         ↓

                  NetworkX Graph Engine
```

---

# Components

## React Frontend

Responsible for:

- user interaction
- flight searches
- result presentation
- graph visualization

Features:

- responsive dashboard UI
- interactive route exploration
- network graph rendering

---

## Spring Boot Backend

Acts as the main application service.

Responsibilities:

- REST API design
- request orchestration
- external API communication
- DTO transformation
- caching
- database interaction

Technologies:

- Spring Web
- Spring Data JPA
- Spring Cache

---

## Flight Data Integration

The system integrates external aviation APIs to retrieve:

- flight information
- airport details
- route information

The backend normalizes external responses into application-specific DTOs.

---

## PostgreSQL Database

Stores:

- flight searches
- historical queries
- structured aviation records

Provides:

- persistent storage
- query capabilities
- analytics foundation

---

## Python Graph Service

A dedicated graph processing service built with:

- Flask
- NetworkX

Responsibilities:

- graph construction
- node/edge generation
- route relationship modeling
- graph serialization

Graph model:

```text
Airport → Node

Flight Route → Directed Edge
```

---

# Data Flow

```text
User Search

     ↓

React Request

     ↓

Spring Boot API

     ↓

Flight Data Provider

     ↓

Response Processing

     ↓

PostgreSQL Storage

     ↓

Graph Construction

     ↓

Network Visualization
```

---

# Features

## Flight Search

Users can search flights using:

- origin airport
- destination airport
- departure date

The system:

- retrieves external flight data
- transforms responses
- returns structured results

---

## Graph-Based Route Visualization

Flight data is converted into a network graph.

Features:

- airport nodes
- route connections
- interactive exploration
- network relationship visualization

---

## Caching Layer

Spring Cache improves application performance by:

- reducing repeated API calls
- lowering external dependency load
- improving response times

---

## Persistence Layer

Search activity is stored for:

- historical analysis
- future analytics
- system insights

---

# Project Structure

```text
flight-network-app/

├── backend/
│
│   ├── controller/
│   ├── service/
│   ├── client/
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── frontend/
│
│   ├── components/
│   └── App.js
│
├── graph-service/
│
│   └── app.py
│
└── README.md
```

---

# Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Cache

## Frontend

- React
- Axios
- React Force Graph

## Graph Processing

- Python
- Flask
- NetworkX

## Database

- PostgreSQL

---

# Engineering Highlights

This project demonstrates:

- distributed system architecture
- service-to-service communication
- REST API design
- external API integration
- database persistence
- caching strategies
- graph data modeling
- frontend/backend integration
- interactive visualization systems

---

# Design Decisions

## Separate Graph Processing Service

Graph operations are isolated from the main backend.

Benefits:

- independent scaling
- separation of responsibilities
- specialized graph processing environment

---

## DTO-Based API Layer

External API responses are transformed into internal models.

Benefits:

- cleaner contracts
- reduced coupling
- easier future provider changes

---

## Persistent Search History

Queries are stored instead of discarded.

This enables:

- analytics
- usage tracking
- future recommendations

---

# Example Use Cases

- Airport connectivity analysis
- Flight network visualization
- Transportation graph exploration
- Route relationship analysis
- Aviation analytics dashboards

---

# Future Improvements

## Infrastructure

- Docker containerization
- Kubernetes deployment
- CI/CD pipelines
- Cloud deployment

## Performance

- Redis distributed caching
- asynchronous processing
- message queues

## Real-Time Features

- WebSocket flight updates
- live aircraft tracking
- streaming aviation events

## Graph Analytics

- shortest route algorithms
- airport centrality scoring
- community detection
- network optimization

---

# What This Project Demonstrates

Flight Network App demonstrates the ability to design and build systems involving:

- backend engineering
- distributed architectures
- graph algorithms
- data processing
- API integration
- scalable application design

---

# License

MIT License
