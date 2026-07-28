import { useEffect, useState } from "react";
import client from "../api/client";
import FlightMap from "./FlightMap";

export default function NetworkMap({ airport }) {

    const [network, setNetwork] = useState(null);


    useEffect(() => {

        async function loadNetwork() {

            const response =
                await client.get(
                    `/network/${airport}`
                );

            setNetwork(response.data);
        }


        if (airport) {
            loadNetwork();
        }

    }, [airport]);


    if (!network) {
        return null;
    }


    return (
        <FlightMap
            nodes={network.nodes}
            edges={network.edges}
        />
    );
}