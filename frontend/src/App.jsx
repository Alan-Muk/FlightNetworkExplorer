import WorldMap from "./components/map/WorldMap";
import DetailsPanel from "./components/details/DetailsPanel";
import AirportSearch from "./components/AirportSearch";

import { useFlightNetwork } from "./hooks/useFlightNetwork";

export default function App() {
    const network = useFlightNetwork();

    return (
        <div
            style={{
                height: "100vh",
                width: "100vw",
                overflow: "hidden",
                position: "relative",
            }}
        >
            <WorldMap network={network} />

            <div
                style={{
                    position: "absolute",
                    top: 16,
                    left: 16,
                    zIndex: 3000,
                }}
            >
                <AirportSearch onSelect={network.selectAirport} />
            </div>

            <DetailsPanel
                airport={network.focusedAirport ?? network.originAirport}
                routes={network.routes}
                route={network.selectedRoute}
                onSelectRoute={network.selectRoute}
                onClose={network.clearSelection}
            />
        </div>
    );
}