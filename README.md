# Flight Network App

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![Python](https://img.shields.io/badge/Python-3.12-3776AB?logo=python)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql)
![NetworkX](https://img.shields.io/badge/Graph-NetworkX-blue)
![License](https://img.shields.io/badge/License-MIT-green)

A full-stack distributed system for searching real-time flight data and visualizing flight routes as an interactive network graph.

This project demonstrates:

* backend engineering
* distributed system architecture
* data processing pipelines
* caching strategies
* persistence layers
* graph analytics
* interactive frontend visualization

Built using Spring Boot, React, PostgreSQL, Flask, and NetworkX.

---

# System Architecture

```text id="9b6f8e"
Frontend (React)
       ↓
Spring Boot Backend
       ↓
FlightLabs API
       ↓
PostgreSQL (Search History)
       ↓
Python Graph Service (NetworkX)
       ↓
Graph JSON → React Visualization
```

---

# Architecture Overview

## Frontend (React)

Handles:

* user interactions
* flight search forms
* result rendering
* graph visualization

Displays interactive force-directed flight route graphs.

---

## Spring Boot Backend

Responsible for:

* REST API endpoints
* flight search orchestration
* DTO transformation
* caching
* persistence
* communication with external services

---

## FlightLabs API

Provides:

* real-time flight information
* route data
* airport details

---

## PostgreSQL

Stores:

* flight search history
* query persistence
* analytics-ready historical records

---

## Python Graph Service

Built with:

* Flask
* NetworkX

Responsible for:

* graph generation
* flight network modeling
* node/edge transformation
* graph JSON generation for frontend visualization

---

# Project Structure

```bash id="s3q13x"
flight-network-app/
│
├── backend/                  # Spring Boot API
│   ├── controller/
│   ├── service/
│   ├── client/
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── frontend/                 # React Application
│   ├── components/
│   └── App.js
│
├── graph-service/            # Python Graph Microservice
│   └── app.py
│
└── README.md
```

---

# Features

## Flight Search

* Search flights by:

  * origin
  * destination
  * departure date
* Real-time data retrieval using FlightLabs API
* Clean DTO-based API responses

---

## Performance Optimization

* Spring Cache integration
* Reduced external API requests
* Faster repeated queries
* Improved backend responsiveness

---

## Persistence Layer

* PostgreSQL integration
* Flight search history storage
* Historical query tracking

---

## Graph Visualization

Transforms flight data into a directed network graph:

* Airports → Nodes
* Flights → Directed edges

Features:

* interactive graph rendering
* airline route visualization
* network exploration

---

# Frontend UI

React-based dashboard interface featuring:

* search forms
* responsive layouts
* interactive flight results
* force-directed graph rendering
* modern visualization workflows

---

# Tech Stack

## Backend

* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Cache

## Frontend

* React
* Axios
* React Force Graph

## Graph Engine

* Flask
* NetworkX

## Database

* PostgreSQL

---

# Engineering Concepts Demonstrated

This project demonstrates practical experience with:

* distributed systems
* RESTful APIs
* microservice communication
* graph data modeling
* caching strategies
* database persistence
* DTO architecture
* frontend/backend integration
* real-time API consumption
* interactive data visualization

---

# Future Improvements

* JWT authentication
* Redis distributed caching
* Docker containerization
* Kubernetes deployment
* WebSocket live flight updates
* Graph analytics algorithms
* Route optimization features
* Airport centrality analysis
* CI/CD pipelines
* Cloud deployment

---

# Example Use Cases

* Airline route analytics
* Airport connectivity visualization
* Flight network exploration
* Transportation graph analysis
* Real-time aviation dashboards

---

# License

This project is open source and available under the MIT License.
