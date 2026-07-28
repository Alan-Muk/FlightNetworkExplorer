import { useState } from "react";
import client from "../api/client";

export default function ConnectionList({ airport }) {

    const [connections, setConnections] = useState([]);

    async function loadConnections() {

        const response = await client.get(
            `/graph/connections/${airport}`
        );

        setConnections(
            response.data.connections || []
        );
    }


    return (
        <div>
            <button onClick={loadConnections}>
                Load Connections
            </button>

            {connections.length > 0 && (
                <div>
                    <h3>
                        Flights from {airport}
                    </h3>

                    <ul>
                        {connections.map(code => (
                            <li key={code}>
                                {code}
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
}