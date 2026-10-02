import os
from contextlib import asynccontextmanager

import networkx as nx
from fastapi import FastAPI, HTTPException, Query
from typing import Literal

from app.graph_loader import FlightGraph

DATA_PATH = os.environ.get("ROUTES_FILE", "../data/raw/routes.dat")
graph = FlightGraph(DATA_PATH)

CENTRALITY_METRICS = {
    "degree": nx.degree_centrality,
    "betweenness": nx.betweenness_centrality,
    "closeness": nx.closeness_centrality,
}

_centrality_cache: dict = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    graph.load()

    print("Computing centrality metrics (may take ~15 seconds)...")
    for name, fn in CENTRALITY_METRICS.items():
        _centrality_cache[name] = fn(graph.graph)
    print(f"Centrality precomputed for {len(_centrality_cache)} metrics")

    yield


app = FastAPI(title="Flight Network Graph Service", lifespan=lifespan)


@app.get("/")
def root():
    return {
        "service": "flight graph",
        "status": "running",
        "airports": graph.graph.number_of_nodes(),
        "routes": graph.graph.number_of_edges(),
    }


@app.get("/connections/{airport}")
def connections(airport: str):
    airport = airport.upper()
    return {"airport": airport, "connections": graph.neighbours(airport)}


@app.get("/path/{source}/{destination}")
def path(source: str, destination: str):
    source = source.upper()
    destination = destination.upper()

    result = graph.shortest_path(source, destination)

    if not result:
        raise HTTPException(
            status_code=404, detail=f"No path found between {source} and {destination}"
        )

    return {"from": source, "to": destination, "path": result}


@app.get("/paths/{source}/{destination}")
def paths(source: str, destination: str):
    source = source.upper()
    destination = destination.upper()

    result = graph.alternative_paths(source, destination, limit=10)

    if not result:
        raise HTTPException(
            status_code=404, detail=f"No paths found between {source} and {destination}"
        )

    return {"from": source, "to": destination, "paths": result}


@app.get("/centrality")
def centrality(
    metric: Literal["degree", "betweenness", "closeness"] = "degree",
    limit: int = Query(default=50, ge=1, le=500),
):
    """
    Returns the top-N airports by the given centrality metric.

    Metrics are precomputed at startup (see lifespan) so requests return instantly.
    """
    scores = _centrality_cache.get(metric)
    if scores is None:
        raise HTTPException(status_code=400, detail=f"Unknown metric: {metric}")

    ranked = sorted(scores.items(), key=lambda kv: kv[1], reverse=True)[:limit]

    return {
        "metric": metric,
        "results": [{"iata": iata, "score": round(score, 6)} for iata, score in ranked],
    }
