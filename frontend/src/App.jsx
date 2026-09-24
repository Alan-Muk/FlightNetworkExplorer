import WorldMap from "./components/map/WorldMap";
import DetailsPanel from "./components/details/DetailsPanel";

import { useFlightNetwork } from "./hooks/useFlightNetwork";

export default function App() {
    const network = useFlightNetwork();

    return (
        <div
            style={{
                height: "100vh",
                width: "100vw",
                overflow: "hidden",
                display: "flex",
                position: "relative",
            }}
        >
            <WorldMap network={network} />
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