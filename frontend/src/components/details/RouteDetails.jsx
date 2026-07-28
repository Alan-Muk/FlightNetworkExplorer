export default function RouteDetails({

    routes,

    route,

    onSelectRoute

}) {

    if (
        !routes ||
        routes.length === 0
    ) {

        return (

            <div className="route-card">

                <h2>
                    Route Comparison
                </h2>

                <p>
                    No routes found.
                </p>

            </div>

        );

    }



    const selected =
        route ??
        routes[0];



    function badges(route) {

        const result = [];

        if (route.shortest)
            result.push("Shortest");

        if (route.fastest)
            result.push("Fastest");

        if (route.leastConnected)
            result.push("Fewest Stops");

        if (route.mostConnected)
            result.push("Most Stops");

        if (result.length === 0)
            result.push("Alternative");

        return result;

    }



    return (

        <div className="route-card">

            <h2>
                Route Comparison
            </h2>

            <div className="route-list">

                <small>

                    {routes.length}

                    {" routes found"}

                </small>

                {

                    routes.map(

                        item => (

                            <button

                                key={
                                    item.id
                                }

                                className={
                                    item.id === selected.id
                                        ? "route-option active"
                                        : "route-option"
                                }

                                onClick={
                                    () =>
                                        onSelectRoute(
                                            item
                                        )
                                }

                            >

                                <div>

                                    {
                                        badges(item).map(
                                            badge => (
                                                <span
                                                    key={badge}
                                                    className="route-badge"
                                                >
                                                    {badge}
                                                </span>
                                            )
                                        )
                                    }

                                </div>

                                <small>

                                    {

                                        item.airports?.join(
                                            " → "
                                        )

                                    }

                                </small>

                            </button>

                        )

                    )

                }

            </div>

            <hr />

            <div className="route-header">

                <h3>

                    {

                        selected.from

                    }

                    {" → "}

                    {

                        selected.to

                    }

                </h3>

            </div>

            <div className="route-badges">

                {

                    badges(selected).map(

                        badge => (

                            <span

                                key={
                                    badge
                                }

                                className="route-badge"

                            >

                                {

                                    badge

                                }

                            </span>

                        )

                    )

                }

            </div>

            <div className="route-stats">

                <div>

                    <span>
                        Distance
                    </span>

                    <strong>

                        {

                            Math.round(
                                selected.distanceKm
                            )

                        }

                        {" km"}

                    </strong>

                </div>

                <div>

                    <span>
                        Flight Time
                    </span>

                    <strong>

                        {

                            selected.estimatedFlightTime

                        }

                    </strong>

                </div>

                <div>

                    <span>
                        Stops
                    </span>

                    <strong>

                        {

                            selected.stops === 0
                                ? "Direct"
                                : selected.stops

                        }

                    </strong>

                </div>

            </div>

            <div className="route-section">

                <span>
                    Route
                </span>

                {

                    selected.airports?.map(

                        airport => (

                            <div

                                key={
                                    airport
                                }

                                className="route-stop"

                            >

                                {airport}

                            </div>

                        )

                    )

                }

            </div>

            <div className="route-section">

                <span>
                    Airlines
                </span>

                <div className="airline-list">

                    {

                        selected.airlines?.map(

                            airline => (

                                <span

                                    key={
                                        airline
                                    }

                                    className="airline"

                                >

                                    {

                                        airline

                                    }

                                </span>

                            )

                        )

                    }

                </div>

            </div>

        </div>

    );

}