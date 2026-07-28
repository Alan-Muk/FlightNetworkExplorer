import { Marker, Popup } from "react-leaflet";
import L from "leaflet";

const defaultIcon =
    new L.Icon({
        iconUrl:
            "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",

        iconRetinaUrl:
            "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",

        shadowUrl:
            "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",

        iconSize: [
            25,
            41
        ],

        iconAnchor: [
            12,
            41
        ]
    });


export default function AirportMarker({

    airport,

    selected,

    highlighted,

    onSelect,

    onDestinationSelect

}) {


    function clickHandler() {

        if (highlighted) {

            onDestinationSelect(
                airport
            );

            return;

        }


        onSelect(
            airport.iata
        );

    }

    if (
        airport.latitude == null ||
        airport.longitude == null
    ) {
        return null;
    }

    return (

        <Marker

            position={[
                Number(airport.latitude),
                Number(airport.longitude)
            ]}

            eventHandlers={{
                click: clickHandler
            }}


            opacity={
                selected
                    ? 1
                    : highlighted
                        ? 0.9
                        : 0.5
            }


            icon={
                defaultIcon
            }

        >

            <Popup>

                <strong>
                    {airport.iata}
                </strong>

                <br />

                {airport.name}

                <br />

                {
                    highlighted
                    &&
                    <small>
                        Click to fly here
                    </small>
                }

            </Popup>


        </Marker>

    );

}