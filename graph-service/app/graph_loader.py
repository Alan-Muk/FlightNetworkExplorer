import csv
import networkx as nx
from pathlib import Path


class FlightGraph:

    def __init__(self, routes_file):
        self.graph = nx.DiGraph()
        self.routes_file = Path(routes_file)


    def load(self):

        with open(
            self.routes_file,
            encoding="utf-8"
        ) as file:

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


                self.graph.add_edge(

                    source,

                    destination,

                    airline=airline

                )


        print(
            f"Loaded graph: "
            f"{self.graph.number_of_nodes()} airports, "
            f"{self.graph.number_of_edges()} routes"
        )



    def neighbours(
            self,
            airport
    ):

        airport = airport.upper()


        if airport not in self.graph:
            return []


        return list(
            self.graph.successors(airport)
        )



    def shortest_path(
            self,
            source,
            destination
    ):

        source = source.upper()

        destination = destination.upper()


        try:

            return nx.shortest_path(
                self.graph,
                source,
                destination
            )


        except (
            nx.NetworkXNoPath,
            nx.NodeNotFound
        ):

            return []


    def alternative_paths(
            self,
            source,
            destination,
            limit=10
    ):

        source = source.upper()

        destination = destination.upper()


        try:

            paths = nx.shortest_simple_paths(
                self.graph,
                source,
                destination
            )


            return [
                path
                for _, path in zip(
                    range(limit),
                    paths
                )
            ]


        except (
            nx.NetworkXNoPath,
            nx.NodeNotFound
        ):

            return []