import {
    useMap
} from "react-leaflet";

import {
    useEffect,
    useRef
} from "react";



export default function MapController({

    airport

}) {


    const map =
        useMap();



    const lastAirport =
        useRef(null);




    useEffect(() => {


        if (!airport)
            return;



        if (
            lastAirport.current === airport.iata
        ) {

            return;

        }



        lastAirport.current =
            airport.iata;




        map.flyTo(

            [

                airport.latitude,

                airport.longitude

            ],


            5,


            {

                duration:1.5,

                easeLinearity:0.25

            }

        );



    }, [

        airport,

        map

    ]);



    return null;

}