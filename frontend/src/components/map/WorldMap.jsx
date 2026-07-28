import {
    MapContainer,
    TileLayer
} from "react-leaflet";

import {
    useEffect,
    useMemo,
    useState,
    useRef
} from "react";

import client from "../../api/client";

import AirportNode from "./AirportNode";
import FlightArc from "./FlightArc";
import MapController from "./MapController";

import DetailsPanel from "../details/DetailsPanel";

import "./map.css";


export default function WorldMap() {


    const [
        airports,
        setAirports
    ] = useState([]);


    const airportsRef =
        useRef([]);



    const [
        expandedAirports,
        setExpandedAirports
    ] = useState([]);



    const [
        focusedAirport,
        setFocusedAirport
    ] = useState(null);



    const [
        originAirport,
        setOriginAirport
    ] = useState(null);



    const [
        destinationAirport,
        setDestinationAirport
    ] = useState(null);



    const [
        routes,
        setRoutes
    ] = useState([]);



    const [
        selectedRoute,
        setSelectedRoute
    ] = useState(null);





    useEffect(() => {

        loadAirports();

    }, []);





    async function loadAirports() {

        const response =
            await client.get(
                "/network/hubs"
            );


        setAirports(
            response.data
        );


        useEffect(() => {

            airportsRef.current =
                airports;

        }, [airports]);

    }





    async function expandAirport(iata) {


        const airport =
            airports.find(

                item =>
                    item.iata === iata

            );


        if (!airport) {

            return;

        }



        setFocusedAirport(
            airport
        );



        if (
            expandedAirports.includes(iata)
        ) {

            return;

        }



        const response =
            await client.get(
                `/network/${iata}`
            );



        const nodes =
            response.data.nodes ?? [];



        setAirports(

            previous => {

                const map = {};



                previous.forEach(

                    airport => {

                        map[
                            airport.iata
                        ] =
                            airport;

                    }

                );



                nodes.forEach(

                    airport => {

                        map[
                            airport.iata
                        ] =
                            airport;

                    }

                );



                return Object.values(map);

            }

        );



        setExpandedAirports(

            previous => [

                ...previous,

                iata

            ]

        );

    }








    async function selectAirport(iata) {


        const airport =
            airports.find(

                item =>
                    item.iata === iata

            );



        if (!airport) {

            return;

        }



        await expandAirport(iata);





        if (!originAirport) {


            setOriginAirport(
                airport
            );


            setDestinationAirport(
                null
            );


            setRoutes([]);


            setSelectedRoute(null);


            return;

        }






        if (

            originAirport &&
            !destinationAirport &&
            originAirport.iata !== airport.iata

        ) {


            setDestinationAirport(
                airport
            );



            const response =
                await client.get(

                    `/routes/compare/${originAirport.iata}/${airport.iata}`

                );



            console.log(
                "COMPARE RESPONSE",
                response.data
            );



            const routeResults =
                response.data.routes ?? [];


            setRoutes(
                routeResults
            );


            await expandRouteAirports(
                routeResults
            );

        }

    }




async function expandRouteAirports(routeResults) {

    const missingAirports = [];


    routeResults.forEach(route => {

        if (!route.airports) {
            return;
        }


        route.airports.forEach(iata => {

            const exists =
                airports.some(

                    airport =>
                        airport.iata === iata

                );


            if (!exists) {

                missingAirports.push(iata);

            }

        });

    });



    for (const iata of missingAirports) {

        try {

            await expandAirport(iata);

        }
        catch(error) {

            console.log(
                "Unable to expand airport:",
                iata,
                error
            );

        }

    }

}



    function selectRoute(route) {

        setSelectedRoute(
            route
        );

    }







    function clearSelection() {


        setOriginAirport(null);


        setDestinationAirport(null);


        setRoutes([]);


        setSelectedRoute(null);


        setFocusedAirport(null);

    }








    const lookup =
        useMemo(() => {


            const result = {};



            airports.forEach(

                airport => {

                    result[
                        airport.iata
                    ] =
                        airport;

                }

            );



            return result;



        },[
            airports
        ]);



        const activeAirports =
            useMemo(() => {

                if (routes.length === 0) {
                    return null;
                }

                const set = new Set();

                routes.forEach(route => {

                    route.airports?.forEach(iata => {

                        set.add(iata);

                    });

                });

                return set;

            }, [routes]);






    return (

        <div

            style={{

                width:"100vw",

                height:"100vh"

            }}

        >



            <MapContainer


                center={[
                    20,
                    0
                ]}


                zoom={2}


                minZoom={2}


                maxZoom={6}


                zoomControl={false}


                maxBounds={[
                    [-85,-180],
                    [85,180]
                ]}


                maxBoundsViscosity={1}


                eventHandlers={{

                    click:
                        clearSelection

                }}


                style={{

                    width:"100%",

                    height:"100%"

                }}

            >





                <MapController

                    airport={
                        focusedAirport ??
                        originAirport
                    }

                />





                <TileLayer

                    url=
                    "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"

                />







        {
            airports.map(

                airport => (

                <AirportNode

                    key={
                        airport.iata
                    }

                    airport={airport}

                    origin={
                        originAirport?.iata === airport.iata
                    }

                    destination={
                        destinationAirport?.iata === airport.iata
                    }

                    faded={
                        activeAirports &&
                        !activeAirports.has(
                            airport.iata
                        )
                    }

                    onSelect={selectAirport}

                    onExpand={expandAirport}

                />

                )

            )
        }








                {
                    routes.map(

                        route => {


                            if (

                                !route.airports ||
                                route.airports.length < 2

                            ) {

                                return null;

                            }





                            return route.airports

                                .slice(0, -1)

                                .map(

                                    (iata,index) => {


                                        const nextIata =
                                            route.airports[index + 1];



                                        const from =
                                            lookup[iata];



                                        const to =
                                            lookup[nextIata];



                                        if (
                                            !from ||
                                            !to
                                        ) {

                                            return null;

                                        }



                                        const segment = {

                                            ...route,

                                            from:iata,

                                            to:nextIata,

                                            id:
                                                `${route.id}-${iata}-${nextIata}`

                                        };



                                        return (

                                    <FlightArc

                                        key={segment.id}

                                        edge={segment}

                                        from={from}

                                        to={to}

                                        selected={
                                            selectedRoute?.id === route.id
                                        }

                                        colour={route.colour}

                                        index={index}

                                        onSelect={
                                            () => selectRoute(route)
                                        }

                                    />

                                        );

                                    }

                                );

                        }

                    )
                }





            </MapContainer>








            <DetailsPanel

                airport={
                    focusedAirport ??
                    originAirport
                }

                routes={routes}

                route={selectedRoute}

                onSelectRoute={selectRoute}

                onClose={clearSelection}

            />


        </div>

    );

}