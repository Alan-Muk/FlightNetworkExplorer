import {
    Polyline
} from "react-leaflet";

import {
    useMemo,
    useState
} from "react";



export default function FlightArc({

    edge,

    from,

    to,

    selected,

    colour,

    onSelect,

    index

}) {


    const [
        hovered,
        setHovered
    ] = useState(false);




    const positions =
        useMemo(() => {


            const midLat =
                (
                    from.latitude +
                    to.latitude
                ) / 2;



            const midLng =
                (
                    from.longitude +
                    to.longitude
                ) / 2;



            /*
                Curve long flights
                so routes do not overlap
            */
            const baseOffset =
                Math.min(

                    25,

                    Math.abs(
                        from.longitude -
                        to.longitude
                    ) / 6

                );


            const curveOffset =
                baseOffset +
                (
                    (index ?? 0) * 4
                );



            return [

                [
                    from.latitude,
                    from.longitude
                ],


                [
                    midLat + curveOffset,
                    midLng
                ],


                [
                    to.latitude,
                    to.longitude
                ]

            ];


        },[
            from,
            to
        ]);






    return (

        <Polyline


            positions={
                positions
            }



            pathOptions={{


                color:

                    selected
                        ? "#00ffff"
                        :
                    hovered
                        ? "#00ff88"
                        :
                    colour ??
                    "#ffffff",




                weight:

                    selected
                        ? 6
                        :
                    hovered
                        ? 4
                        :
                        2,




                opacity:

                    selected
                        ? 1

                        :

                    hovered
                        ? .95

                        :

                        .55,




                dashArray:

                    selected
                        ? "12 8"

                        :

                        null,




                className:

                    selected
                        ? "flight-route-active"

                        :

                        "flight-route"

            }}





            eventHandlers={{


                click: () => {


                    onSelect(
                        edge
                    );


                },



                mouseover: () => {


                    setHovered(
                        true
                    );


                },



                mouseout: () => {


                    setHovered(
                        false
                    );


                }


            }}

        />

    );

}