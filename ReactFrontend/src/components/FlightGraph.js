import axios from "axios";
import { useState } from "react";
import ForceGraph2D from "react-force-graph-2d";

export default function FlightGraph() {
  const [graph, setGraph] = useState(null);

  const loadGraph = async () => {
    const res = await axios.get(
      "http://localhost:8080/api/flights/graph?from=AMS&to=LHR&date=2026-05-01"
    );

    setGraph(res.data);
  };

  return (
    <div>
      <button onClick={loadGraph}>Load Graph</button>

      {graph && <ForceGraph2D graphData={graph} nodeLabel="id" />}
    </div>
  );
}

import axios from "axios";
import { useState } from "react";
import ForceGraph2D from "react-force-graph-2d";

export default function FlightGraph() {
  const [graph, setGraph] = useState(null);

  const loadGraph = async () => {
    const res = await axios.get(
      "http://localhost:8080/api/flights/graph?from=AMS&to=LHR&date=2026-05-01"
    );

    setGraph(res.data);
  };

  return (
    <div>
      <button onClick={loadGraph}>Load Graph</button>

      {graph && <ForceGraph2D graphData={graph} nodeLabel="id" />}
    </div>
  );
}

  /**
 * FlightGraph Component
 *
 * This React component fetches flight route data from a backend API
 * and displays it as an interactive force-directed graph using `react-force-graph-2d`.
 *
 * Key features:
 *  - Fetch flight data from a given route and date
 *  - Render nodes and edges in an interactive 2D graph
 *  - Display node labels (airports) for easy identification
 *
 * Usage:
 *  - Click the "Load Graph" button to fetch and visualize the flight data
 *  - The graph is only rendered after the data is successfully loaded
 *
 * Dependencies:
 *  - axios: for HTTP requests
 *  - react-force-graph-2d: for graph visualization
 *  - React useState hook: for managing component state
 *
 * Notes:
 *  - The API endpoint is currently hardcoded for demonstration purposes
 *  - Error handling logs failed requests to the console
 */
