import {
    MapContainer,
    TileLayer,
    Marker,
    Popup,
    Polyline
} from "react-leaflet";


export default function FlightMap({
    nodes,
    edges
}) {

    const center = [
        nodes[0].latitude,
        nodes[0].longitude
    ];


    const lookup = {};

    nodes.forEach(node => {
        lookup[node.iata] = node;
    });


    return (
        <MapContainer

            center={[20,0]}

            zoom={2}

            minZoom={2}

            maxZoom={8}

            maxBounds={[
                [-60,-180],
                [85,180]
            ]}

            maxBoundsViscosity={1}

        >

        <TileLayer
            url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />


            {nodes.map(node => (

                <Marker
                    key={node.iata}
                    position={[
                        node.latitude,
                        node.longitude
                    ]}
                >

                    <Popup>
                        <b>
                            {node.iata}
                        </b>
                        <br />
                        {node.name}
                    </Popup>

                </Marker>

            ))}


            {edges.map(edge => {

                const from =
                    lookup[edge.from];

                const to =
                    lookup[edge.to];


                if (!from || !to) {
                    return null;
                }


                return (
                    <Polyline
                        key={
                            edge.from +
                            "-" +
                            edge.to
                        }
                        positions={[
                            [
                                from.latitude,
                                from.longitude
                            ],
                            [
                                to.latitude,
                                to.longitude
                            ]
                        ]}
                        color="blue"
                    />
                );

            })}

        </MapContainer>
    );
}