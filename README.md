# Flight Network App

A full-stack distributed system for searching real-time flight data and visualizing flight routes as an interactive network graph.
This project demonstrates backend engineering, data processing, caching, persistence, and graph visualization using Spring Boot, React, and NetworkX.

# Architecture

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

# Project Structure

flight-network-app/
│
├── backend (Spring Boot)
│   ├── controller
│   ├── service
│   ├── client
│   ├── dto
│   ├── entity
│   └── repository
│
├── frontend (React)
│   ├── components
│   └── App.js
│
├── graph-service (Python)
│   └── app.py
│
└── README.md

Frontend (React): Handles user interactions and renders interactive flight graphs.
Spring Boot Backend: Manages flight search requests, caching, and persistence.
FlightLabs API: Provides real-time flight data.
PostgreSQL: Stores flight search history.
Python Graph Service (NetworkX + Flask): Converts flight data into a directed graph JSON for visualization.

# Features

Flight Search
Search flights by origin, destination, and date.
Fetches real-time data via the FlightLabs API.
Returns clean DTO-based responses for frontend consumption.
Performance
Caching with Spring Cache to reduce external API calls.
Persistence
Stores flight search history in PostgreSQL.
Graph Visualization
Converts flight data into a directed graph:
Airports = nodes
Flights = edges
Interactive graph rendering in React.

# Frontend UI

React-based interface for search and visualization.
Displays flight results in a readable format.
Interactive force-directed graph rendering of flight routes.

# Tech Stack

Backend: Spring Boot, Spring Web, Spring Data JPA, Spring Cache
Frontend: React, Axios, React Force Graph
Graph Engine: NetworkX, Flask (microservice)
Database: PostgreSQL
