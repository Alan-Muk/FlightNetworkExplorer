from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException

from app.graph_loader import FlightGraph



graph = FlightGraph(
    "../data/raw/routes.dat"
)



@asynccontextmanager
async def lifespan(app: FastAPI):

    graph.load()

    yield



app = FastAPI(
    title="Flight Network Graph Service",
    lifespan=lifespan
)



@app.get("/")
def root():

    return {
        "service": "flight graph",
        "status": "running",
        "airports": graph.graph.number_of_nodes(),
        "routes": graph.graph.number_of_edges()
    }



@app.get("/connections/{airport}")
def connections(
        airport: str
):

    airport = airport.upper()


    return {
        "airport": airport,
        "connections": graph.neighbours(
            airport
        )
    }



@app.get("/path/{source}/{destination}")
def path(
        source: str,
        destination: str
):

    source = source.upper()

    destination = destination.upper()


    result = graph.shortest_path(
        source,
        destination
    )


    if not result:

        raise HTTPException(
            status_code=404,
            detail=f"No path found between {source} and {destination}"
        )


    return {
        "from": source,
        "to": destination,
        "path": result
    }

    
@app.get("/paths/{source}/{destination}")
def paths(
        source: str,
        destination: str
):

    source = source.upper()

    destination = destination.upper()


    result = graph.alternative_paths(
        source,
        destination,
        limit=10
    )


    if not result:

        raise HTTPException(
            status_code=404,
            detail=f"No paths found between {source} and {destination}"
        )


    return {

        "from": source,

        "to": destination,

        "paths": result

    }