from flask import Flask, request, jsonify
import networkx as nx

app = Flask(__name__)

@app.route("/graph", methods=["POST"])
def build_graph():
    data = request.json

    G = nx.DiGraph()

    # Build graph from flights
    for f in data["flights"]:
        source = f["departure"]
        target = f["arrival"]
        airline = f["airline"]

        G.add_edge(source, target, airline=airline)

    nodes = [{"id": n} for n in G.nodes()]

    edges = []
    for u, v, d in G.edges(data=True):
        edges.append({
            "source": u,
            "target": v,
            "airline": d.get("airline")
        })

    return jsonify({
        "nodes": nodes,
        "links": edges
    })

if __name__ == "__main__":
    app.run(port=5001)

"""
Flask API: Flight Graph Builder

This Flask application provides an endpoint to build a flight graph
from JSON input. The graph represents airports as nodes and flights as
directed edges, with the airline as edge metadata.

Endpoint:
    POST /graph
        - Request body: JSON object with a "flights" list
          Example:
          {
              "flights": [
                  {"departure": "AMS", "arrival": "LHR", "airline": "KLM"},
                  {"departure": "LHR", "arrival": "JFK", "airline": "British Airways"}
              ]
          }
        - Response: JSON object with nodes and edges suitable for visualization
          {
              "nodes": [{"id": "AMS"}, {"id": "LHR"}, ...],
              "links": [{"source": "AMS", "target": "LHR", "airline": "KLM"}, ...]
          }

Dependencies:
    - Flask: for the web API
    - NetworkX: for building and handling the graph structure
"""
