import csv
import math
import os
import networkx as nx
from pathlib import Path


def haversine(lat1, lon1, lat2, lon2):
    """Great-circle distance between two coordinates in kilometres."""
    R = 6371.0
    d_lat = math.radians(lat2 - lat1)
    d_lon = math.radians(lon2 - lon1)
    a = (
        math.sin(d_lat / 2) ** 2
        + math.cos(math.radians(lat1))
        * math.cos(math.radians(lat2))
        * math.sin(d_lon / 2) ** 2
    )
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))


class FlightGraph:
    def __init__(self, routes_file, airports_file=None):
        self.graph = nx.DiGraph()
        self.routes_file = Path(routes_file)
        self.airports_file = (
            Path(airports_file)
            if airports_file
            else Path(os.environ.get("AIRPORTS_FILE", "../data/raw/airports.dat"))
        )

    def load(self):
        coords = self._load_airport_coordinates()
        with_distance = 0
        fallback_distance = 0

        with open(self.routes_file, encoding="utf-8") as file:
            reader = csv.reader(file)

            for row in reader:
                if len(row) < 8:
                    continue

                airline = row[0]
                source = row[2].strip().upper()
                destination = row[4].strip().upper()

                if (
                    source == "\\N"
                    or destination == "\\N"
                    or not source
                    or not destination
                ):
                    continue

                if source in coords and destination in coords:
                    distance_km = haversine(*coords[source], *coords[destination])
                    with_distance += 1
                else:
                    # Fallback for the rare airport without valid coordinates.
                    # Uses a mid-range estimate rather than letting NetworkX treat
                    # the edge as weight 1 (which would make it look nearly free).
                    distance_km = 500.0
                    fallback_distance += 1

                self.graph.add_edge(
                    source, destination, airline=airline, distance_km=distance_km
                )

        print(
            f"Loaded graph: "
            f"{self.graph.number_of_nodes()} airports, "
            f"{self.graph.number_of_edges()} unique routes "
            f"({with_distance} with computed distance, "
            f"{fallback_distance} with fallback)"
        )

    def _load_airport_coordinates(self):
        """Returns {IATA: (lat, lon)} for airports with valid coordinates."""
        coords = {}
        if not self.airports_file.exists():
            print(f"Warning: airports file not found at {self.airports_file}")
            return coords

        with open(self.airports_file, encoding="utf-8") as file:
            reader = csv.reader(file)
            for row in reader:
                if len(row) < 8:
                    continue
                iata = row[4].strip().upper()
                if not iata or iata == "\\N":
                    continue
                try:
                    lat = float(row[6])
                    lon = float(row[7])
                except (ValueError, IndexError):
                    continue
                coords[iata] = (lat, lon)

        return coords

    def neighbours(self, airport):
        airport = airport.upper()
        if airport not in self.graph:
            return []
        return list(self.graph.successors(airport))

    def shortest_path(self, source, destination):
        source = source.upper()
        destination = destination.upper()
        try:
            return nx.shortest_path(
                self.graph, source, destination, weight="distance_km"
            )
        except (nx.NetworkXNoPath, nx.NodeNotFound):
            return []

    def alternative_paths(self, source, destination, limit=10):
        source = source.upper()
        destination = destination.upper()
        try:
            paths = nx.shortest_simple_paths(
                self.graph, source, destination, weight="distance_km"
            )
            return [path for _, path in zip(range(limit), paths)]
        except (nx.NetworkXNoPath, nx.NodeNotFound):
            return []
