import RouteDetails from "./RouteDetails";
import AirportPanel from "../AirportPanel";

import "./details.css";

export default function DetailsPanel({

    airport,

    routes,

    route,

    onSelectRoute,

    onClose

}) {

    if (
        !airport &&
        (
            !routes ||
            routes.length === 0
        )
    ) {

        return null;

    }

    return (

        <div className="details-panel">

            <button

                className="details-close"

                onClick={
                    onClose
                }

            >
                ×
            </button>

            {
                airport &&
                routes.length === 0 &&
                (
                    <AirportPanel
                        airport={airport}
                    />
                )
            }

            {
            routes &&
            routes.length > 0 &&
            (
                <RouteDetails

                    routes={routes}

                    route={route}

                    onSelectRoute={onSelectRoute}

                />
            )
            }

        </div>

    );

}