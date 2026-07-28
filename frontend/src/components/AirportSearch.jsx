import { useState } from "react";
import client from "../api/client";
import ConnectionList from "./ConnectionList";
import NetworkMap from "./NetworkMap";

export default function AirportSearch() {

    const [iata, setIata] = useState("");
    const [airport, setAirport] = useState(null);

    async function search() {

        const response = await client.get(
            `/airports/${iata}`
        );

        setAirport(response.data);
    }


    return (
        <div>
            <input
                value={iata}
                onChange={
                    e => setIata(e.target.value)
                }
                placeholder="IATA code e.g. FRA"
            />

            <button onClick={search}>
                Search
            </button>


        {airport && (
            <div>
                <h3>
                    {airport.name}
                </h3>

                <p>
                    {airport.city}, {airport.country}
                </p>

                <ConnectionList
                    airport={airport.iata}
                />

                <NetworkMap
                    airport={airport.iata}
                />
            </div>
        )}
        </div>
    );
}