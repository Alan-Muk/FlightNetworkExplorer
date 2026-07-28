import {
    Marker,
    Popup
} from "react-leaflet";

import L from "leaflet";



function createIcon(
    type,
    size,
    faded
) {

    const colors = {

        normal:"#ffffff",

        origin:"#00ffff",

        destination:"#ff4d6d"

    };


    const color =
        colors[type];


    return L.divIcon({

        className:"airport-node",

        html:
        `
        <div
            style="
                width:${size}px;
                height:${size}px;
                border-radius:50%;
                background:${color};
                box-shadow:
                    0 0 ${size * 1.8}px ${color};
                border:
                    2px solid #111;
                opacity:${faded ? 0.15 : 1};
                filter:${faded ? "grayscale(100%)" : "none"};
                transition:
                    opacity .3s ease,
                    filter .3s ease;
            "
        >
        </div>
        `,

        iconSize:[
            size,
            size
        ],

        iconAnchor:[
            size / 2,
            size / 2
        ]

    });

}





export default function AirportNode({

    airport,

    origin,

    destination,

    faded,

    onSelect,

    onExpand

}) {



    if (

        airport.latitude == null ||

        airport.longitude == null

    ) {

        return null;

    }





    const connections =
        airport.connections ?? 0;



    const size =
        Math.min(
            34,
            8 + connections / 35
        );





    const type =

        origin

        ?

        "origin"

        :

        destination

        ?

        "destination"

        :

        "normal";





    return (

        <Marker

            position={[
                airport.latitude,
                airport.longitude
            ]}

            icon={
                createIcon(
                    type,
                    size,
                    faded
                )
            }

            eventHandlers={{

                click: () => {

                    if (onExpand) {

                        onExpand(
                            airport.iata
                        );

                    }

                },

                dblclick: () => {

                    onSelect(
                        airport.iata
                    );

                }

            }}

        >

            <Popup>

                <strong>
                    {airport.iata}
                </strong>

                <br />

                {airport.name}

                {
                    airport.city &&

                    <>

                        <br />

                        {airport.city}

                    </>

                }

                {
                    connections > 0 &&

                    <>

                        <br />
                        <br />

                        Connections:
                        {" "}
                        {connections}

                    </>

                }

                {
                    origin &&

                    <>

                        <br />
                        <br />

                        ✈ Starting point

                    </>

                }

                {
                    destination &&

                    <>

                        <br />
                        <br />

                        🛬 Destination

                    </>

                }

            </Popup>

        </Marker>

    );

}