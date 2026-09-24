import { MapContainer, TileLayer } from "react-leaflet";

import AirportNode from "./AirportNode";
import FlightArc from "./FlightArc";
import MapController from "./MapController";

import "./map.css";

export default function WorldMap({ network }) {
    const {
        airports,
        focusedAirport,
        originAirport,
        destinationAirport,
        routes,
        selectedRoute,
        lookup,
        activeAirports,
        expandAirport,
        selectAirport,
        selectRoute,
        clearSelection,
    } = network;

    return (
        <div
            style={{
                width: "100%",
                height: "100%",
                position: "relative",
            }}
        >
            <MapContainer
                center={[20, 0]}
                zoom={2}
                minZoom={2}
                maxZoom={6}
                zoomControl={false}
                maxBounds={[
                    [-85, -180],
                    [85, 180],
                ]}
                maxBoundsViscosity={1}
                eventHandlers={{
                    click: clearSelection,
                }}
                style={{
                    width: "100%",
                    height: "100%",
                }}
            >
                <MapController airport={focusedAirport ?? originAirport} />

                <TileLayer url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png" />

                {airports.map((airport) => (
                    <AirportNode
                        key={airport.iata}
                        airport={airport}
                        origin={originAirport?.iata === airport.iata}
                        destination={destinationAirport?.iata === airport.iata}
                        faded={
                            activeAirports && !activeAirports.has(airport.iata)
                        }
                        onSelect={selectAirport}
                        onExpand={expandAirport}
                    />
                ))}

                {routes.map((route) => {
                    if (!route.airports || route.airports.length < 2) {
                        return null;
                    }

                    return route.airports.slice(0, -1).map((iata, index) => {
                        const nextIata = route.airports[index + 1];
                        const from = lookup[iata];
                        const to = lookup[nextIata];

                        if (!from || !to) {
                            return null;
                        }

                        const segment = {
                            ...route,
                            from: iata,
                            to: nextIata,
                            id: `${route.id}-${iata}-${nextIata}`,
                        };

                        return (
                            <FlightArc
                                key={segment.id}
                                edge={segment}
                                from={from}
                                to={to}
                                selected={selectedRoute?.id === route.id}
                                colour={route.colour}
                                index={index}
                                onSelect={() => selectRoute(route)}
                            />
                        );
                    });
                })}
            </MapContainer>
        </div>
    );
}