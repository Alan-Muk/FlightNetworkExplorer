import { useEffect, useState } from "react";
import client from "../api/client";


export default function AirportPanel({
    airport,
    edges
}) {


    const [stats, setStats] = useState(null);

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState(null);



    useEffect(() => {

        if (!airport)
            return;


        let cancelled = false;


        async function loadStats() {

            try {

                setLoading(true);
                setError(null);


                const response =
                    await client.get(
                        `/airport/${airport.iata}/stats`
                    );


                if (!cancelled) {

                    setStats(
                        response.data
                    );

                }

            }
            catch (err) {

                console.error(
                    "Failed to load airport stats",
                    err
                );


                if (!cancelled) {

                    setStats(null);

                    setError(
                        "Unable to load statistics"
                    );

                }

            }
            finally {

                if (!cancelled) {

                    setLoading(false);

                }

            }

        }


        loadStats();


        return () => {

            cancelled = true;

        };


    }, [airport]);



    if (!airport)
        return null;



    return (

        <div
            style={{
                width:300,
                background:
                    "rgba(0,0,0,0.9)",
                color:"white",
                padding:20,
                borderRadius:12,
                border:
                    "1px solid #00ffff",
                boxShadow:
                    "0 0 20px rgba(0,255,255,.4)"
            }}
        >

            <h2
                style={{
                    color:"#00ffff"
                }}
            >
                {airport.iata}
            </h2>


            <h3>
                {airport.name}
            </h3>

            <p>
                {airport.city}
                {
                    airport.country &&
                    `, ${airport.country}`
                }
            </p>


            {
            loading && (
                <p>
                    Loading statistics...
                </p>
            )
            }
            {
            stats && (

                <>

                    <hr/>


                    <p>
                        Routes:
                        {" "}
                        <b>
                            {stats.connections}
                        </b>
                    </p>


                    <p>
                        Departures:
                        {" "}
                        <b>
                            {stats.outgoingRoutes}
                        </b>
                    </p>


                    <p>
                        Arrivals:
                        {" "}
                        <b>
                            {stats.incomingRoutes}
                        </b>
                    </p>



                    <h4>
                        Top destinations
                    </h4>


                    <ul>

                    {
                    stats.topDestinations?.map(
                        destination => (

                            <li
                                key={destination}
                            >
                                {destination}
                            </li>

                        )
                    )
                    }

                    </ul>



                    <h4>
                        Airlines
                    </h4>


                    <ul>

                    {
                   stats.airlines?.map(
                        airline => (

                            <li
                                key={airline}
                            >
                                {airline}
                            </li>

                        )
                    )
                    }

                    </ul>


                </>

            )
            }



        </div>

    );

}