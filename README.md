# FlightNetworkExplorer

![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-7-646CFF?logo=vite&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.x-3776AB?logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-API-009688?logo=fastapi&logoColor=white)
![NetworkX](https://img.shields.io/badge/Graph-NetworkX-blue)
![Leaflet](https://img.shields.io/badge/Maps-Leaflet-199900?logo=leaflet&logoColor=white)
![Database](https://img.shields.io/badge/Database-H2%20%7C%20PostgreSQL-blue)

An interactive airline network exploration platform that transforms global flight data into dynamic graph structures using geographic visualisation, graph algorithms, and route analysis.

FlightNetworkExplorer allows users to explore airport connections, expand flight networks interactively, discover routes between destinations, and analyse airline connectivity through an interactive world map.

---

# Overview

FlightNetworkExplorer is a full-stack aviation network analysis system built around graph-based modelling of airline routes.

Instead of treating flights as isolated records, the system represents the global airline network as a directed graph:

- Airports become graph nodes
- Flight routes become directed edges
- Graph algorithms discover paths and connections
- The frontend provides interactive geographic exploration

The application combines:

- React and Leaflet for map-based visualisation
- Spring Boot for REST APIs and backend services
- Python and NetworkX for graph computation

## System Workflow

```text
User Interaction
        |
        ↓
React Interactive Map
        |
        ↓
Spring Boot REST API
        |
        ├───────────────┐
        ↓               ↓
 Flight Database   Python Graph Service
                    (NetworkX)
                         |
                         ↓
                Graph Algorithms
                         |
                         ↓
              Routes and Connections
                         |
                         ↓
             Interactive Visualisation

# Problem

Global airline networks contain thousands of airports and millions of possible connections.

Traditional flight databases represent routes as independent records, making it difficult to understand:

- How airports connect globally
- Which airports act as major hubs
- What paths exist between destinations
- How routes relate through intermediate airports
- How network structures evolve

FlightNetworkExplorer models airline data as a graph, enabling users to explore connectivity, analyse routes, and visualise relationships between airports.

---

# Architecture

```text
React + Leaflet Client
          |
          |
     Spring Boot API
          |
          |
  Flight Data Services
          |
          |
 Python Graph Engine
          |
          |
     NetworkX Graph
```

---

# Frontend

Built with React and Vite.

## Responsibilities

- Interactive world map rendering
- Airport selection
- Route visualisation
- Dynamic graph expansion
- Route highlighting
- Airport information panels

## Technologies

- React
- Vite
- JavaScript
- React Leaflet
- CSS

---

# Backend

Built with Java 21 and Spring Boot.

## Responsibilities

- REST API layer
- Airport and route management
- Database communication
- Data processing
- Graph service integration

## Technologies

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- H2 Database
- PostgreSQL support

## Backend Structure

```text
backend

├── controller
│       REST endpoints
│
├── service
│       Business logic
│
├── repository
│       Database access
│
├── model
│       JPA entities
│
└── dto
        API response objects
```

---

# Graph Service

The graph service is a dedicated Python service responsible for graph analysis and route computation.

## Technologies

- Python
- FastAPI
- NetworkX

The airline network is represented as a directed graph:

```text
Airport = Node

Flight Route = Directed Edge
```

Example:

```text
        JFK
         |
         ↓
        LHR
         |
         ↓
        AMS
```

Each edge stores route information including airline and connection data.

---

# API

## Expand Airport Network

```
GET /api/network/{iata}
```

Example:

```
GET /api/network/AMS
```

Returns connected airports and routes around a selected airport.

Used for dynamic map expansion.

---

## Get Route Details

```
GET /api/routes/{from}/{to}
```

Example:

```
GET /api/routes/AMS/JFK
```

Returns route information between two airports.

Example response:

```json
{
  "from": "AMS",
  "to": "JFK",
  "routes": [
    {
      "via": "LHR",
      "airline": "Example Airline"
    }
  ]
}
```

---

## Compare Routes

```
GET /api/routes/compare/{from}/{to}
```

Example:

```
GET /api/routes/compare/AMS/JFK
```

Returns available route options.

Example:

```text
AMS

├── LHR
│     |
│     ↓
│    JFK
│
└── CDG
      |
      ↓
     JFK
```

---

## Airport Statistics

```
GET /api/airport/{iata}/stats
```

Example:

```
GET /api/airport/AMS/stats
```

Returns statistics for a specific airport.

---

## Graph Connections

```
GET /api/graph/connections/{airport}
```

Example:

```
GET /api/graph/connections/JFK
```

Returns neighbouring airports from the graph service.

---

## Graph Path Finding

```
GET /api/graph/path/{from}/{to}
```

Example:

```
GET /api/graph/path/JFK/LHR
```

Returns a route path between airports.

---

# Core Features

## Interactive World Map

The application provides an interactive global map for exploring airline networks.

### Features

- Airport exploration
- Route rendering
- Geographic visualisation
- Connection inspection
- Network discovery

The map allows users to navigate the airline graph visually rather than through static tables.

---

## Dynamic Network Expansion

The complete global network is not loaded immediately.

Instead, airports are expanded on demand:

```text
Select Airport

      |
      ↓

Request Connections

      |
      ↓

Add Airports and Routes

      |
      ↓

Continue Exploration
```

### Benefits

- Improved performance
- Reduced visual complexity
- Scalable exploration
- Focused graph rendering

---

## Route Comparison

Users can compare possible journeys between airports.

### Features

- Multiple route options
- Multi-leg journeys
- Route highlighting
- Connection discovery
- Alternative path exploration

Example:

```text
Amsterdam

     |
     |
    LHR
     |
     |
   New York
```

---

## Graph-Based Analysis

The Python graph engine uses NetworkX to analyse the airline network.

### Implemented Operations

- Airport neighbour lookup
- Shortest path discovery
- Alternative route generation
- Graph traversal

### Algorithms

```python
networkx.shortest_path
networkx.shortest_simple_paths
```

---

# Airport Network Analysis

The system supports:

- Airport connectivity exploration
- Hub discovery
- Route analysis
- Network traversal
- Graph-based aviation research

Example:

```text
            LHR

             |
             |

JFK ---- AMS ---- CDG

             |
             |

            FRA
```

---

# Technical Highlights

- Built a full-stack airline graph exploration platform
- Modelled flight routes as directed graph structures
- Created an interactive geographic visualisation system
- Integrated Spring Boot with a Python graph processing service
- Implemented graph-based route discovery
- Built reusable React map components
- Designed modular backend services
- Separated visualisation, API logic, and graph computation

---

# Design Decisions

## Directed Graph Model

Airline routes are represented as directed edges.

Example:

```text
London → Amsterdam
```

does not automatically imply:

```text
Amsterdam → London
```

This reflects real airline networks where routes can differ by direction.

---

## Separate Graph Service

Graph computation is isolated from the main backend.

### Benefits

- Dedicated graph processing layer
- Independent algorithm development
- Clear separation of responsibilities
- Easier future scaling

---

## Local Graph Expansion

Large global networks quickly become difficult to display.

The explorer expands locally:

```text
User selects airport

        |
        ↓

Fetch connections

        |
        ↓

Add nodes and routes

        |
        ↓

Continue exploration
```

This keeps the visualisation manageable while supporting large datasets.

---

# Tech Stack

## Frontend

- React
- Vite
- React Leaflet
- JavaScript
- CSS

## Backend

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- H2
- PostgreSQL

## Graph Service

- Python
- FastAPI
- NetworkX

## Data Processing

- CSV route imports
- Airport datasets
- Airline datasets

## Algorithms

- Graph traversal
- Shortest path discovery
- Alternative path generation

---

# How It Works

1. User opens the interactive world map
2. Frontend requests airport network data
3. Spring Boot processes API requests
4. Graph service performs graph analysis
5. Routes and connections are returned as JSON
6. React renders airports and flight paths
7. Users continue expanding the network

---

# Example Exploration

Starting from:

```text
Amsterdam (AMS)
```

The explorer discovers:

```text
AMS

├── London (LHR)
├── Paris (CDG)
├── Frankfurt (FRA)
└── New York (JFK)
```

Selecting another airport allows route comparison and path discovery.

---

# Example Use Cases

- Airline network exploration
- Graph algorithm demonstrations
- Route discovery
- Airport connectivity analysis
- Geographic data visualisation
- Aviation research
- Network science experiments

---

# Challenges

## Network Size

Global flight datasets contain thousands of airports and routes.

### Solution

- Dynamic expansion
- Selective loading
- Local graph exploration

---

## Graph Complexity

Flight networks contain many possible paths.

### Solution

- Directed graph modelling
- Dedicated graph service
- NetworkX algorithms

---

## Visualization Complexity

Large graphs can become difficult to interpret.

### Solution

- Interactive exploration
- Route highlighting
- Focused rendering
- Incremental expansion

---

# Future Improvements

- Add weighted routes using distance or travel time
- Add airport centrality calculations
- Add graph caching
- Add Docker deployment
- Add authentication
- Add richer analytics
- Add historical flight data
- Improve route ranking algorithms

---

# Running Locally

## Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/FlightNetworkExplorer

cd FlightNetworkExplorer
```

---

## Start Graph Service

```bash
cd graph-service

pip install -r requirements.txt

uvicorn app.main.py --reload --port 8000
```

Graph service:

```
http://localhost:8000
```

---

## Start Backend

```bash
cd backend

./mvnw spring-boot:run
```

Backend:

```
http://localhost:8080
```

---

## Start Frontend

```bash
cd frontend

npm install

npm run dev
```

Frontend:

```
http://localhost:5173
```

---

# Project Structure

```text
FlightNetworkExplorer

├── frontend
│   ├── src
│   │   ├── components
│   │   ├── api
│   │   └── assets
│   └── package.json
│
├── backend
│   ├── src/main/java
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── model
│   │   └── dto
│   └── pom.xml
│
└── graph-service
    ├── app
    │   ├── main.py
    │   └── graph_loader.py
    └── requirements.txt
```

---


```markdown
<img width="1366" height="768" alt="Screenshot From 2026-07-28 09-45-38" src="https://github.com/user-attachments/assets/20b64194-c329-4834-a7e9-166ee7996784" />
<img width="1366" height="768" alt="Screenshot From 2026-07-28 09-45-15" src="https://github.com/user-attachments/assets/0f1c5a7a-cac1-4cab-8c5f-646d81c50b99" />
<img width="1366" height="768" alt="Screenshot From 2026-07-28 09-43-49" src="https://github.com/user-attachments/assets/7684d804-6e23-436c-9269-fe33cb1776b9" />
<img width="1366" height="768" alt="Screenshot From 2026-07-28 09-43-35" src="https://github.com/user-attachments/assets/ec2bc120-b5f2-4dfe-98e8-7aa95945594d" />

```

---

# License

MIT License
