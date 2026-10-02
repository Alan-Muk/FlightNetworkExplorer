import { Polyline } from "react-leaflet";
import { useMemo, useState } from "react";

/**
 * Number of intermediate points along each arc. Higher = smoother but more DOM nodes.
 * 24 gives a smooth curve without being excessive.
 */
const ARC_SEGMENTS = 24;

/**
 * Convert lat/lon (degrees) to a 3D unit vector.
 */
function toVector(lat, lon) {
    const latRad = (lat * Math.PI) / 180;
    const lonRad = (lon * Math.PI) / 180;
    return [
        Math.cos(latRad) * Math.cos(lonRad),
        Math.cos(latRad) * Math.sin(lonRad),
        Math.sin(latRad),
    ];
}

/**
 * Convert a 3D unit vector back to [lat, lon] in degrees.
 */
function toLatLon(v) {
    const [x, y, z] = v;
    const lat = Math.atan2(z, Math.sqrt(x * x + y * y)) * (180 / Math.PI);
    const lon = Math.atan2(y, x) * (180 / Math.PI);
    return [lat, lon];
}

/**
 * Generate points along the great-circle path between two coordinates.
 * Uses spherical linear interpolation (slerp).
 */
function greatCirclePoints(from, to, segments) {
    const v1 = toVector(from[0], from[1]);
    const v2 = toVector(to[0], to[1]);

    // Angle between the two vectors
    const dot = Math.min(1, Math.max(-1, v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2]));
    const omega = Math.acos(dot);
    const sinOmega = Math.sin(omega);

    // If the two points are (nearly) identical, return a flat line
    if (sinOmega < 1e-9) {
        return [from, to];
    }

    const points = [];
    for (let i = 0; i <= segments; i++) {
        const t = i / segments;
        const a = Math.sin((1 - t) * omega) / sinOmega;
        const b = Math.sin(t * omega) / sinOmega;
        points.push(
            toLatLon([
                a * v1[0] + b * v2[0],
                a * v1[1] + b * v2[1],
                a * v1[2] + b * v2[2],
            ]),
        );
    }
    return points;
}

export default function FlightArc({
    edge,
    from,
    to,
    selected,
    colour,
    onSelect,
    index,
}) {
    const [hovered, setHovered] = useState(false);

    const positions = useMemo(() => {
        const fromLatLng = [from.latitude, from.longitude];
        const toLatLng = [to.latitude, to.longitude];


        // Great-circle path
        const path = greatCirclePoints(fromLatLng, toLatLng, ARC_SEGMENTS);

        // Offset overlapping arcs slightly so multiple routes between the
        // same two points are distinguishable.
       const offsetSign = (index ?? 0) % 2 === 0 ? 1 : -1;
       const offset = Math.floor((index ?? 0) / 2 + 1) * 0.35 * offsetSign;

        if (offset === 0) return path;


        // Push the midpoint of the path perpendicular to the route direction.
        // For simplicity, use a small latitude offset — works well for most
        // routes and keeps the visual separation subtle.
        return path.map((point, i) => {
            // Weight the offset so it peaks at the midpoint
            const weight = Math.sin((i / (path.length - 1)) * Math.PI);
            return [point[0] + weight * offset, point[1]];
        });
    }, [from, to, index]);


    return (
        <Polyline
            positions={positions}
            pathOptions={{
                color: selected
                    ? "#00ffff"
                    : hovered
                      ? "#00ff88"
                      : (colour ?? "#ffffff"),
                weight: selected ? 6 : hovered ? 4 : 2,
                opacity: selected ? 1 : hovered ? 0.95 : 0.55,
                dashArray: selected ? "12 8" : null,
                className: selected ? "flight-route-active" : "flight-route",
                lineCap: "round",
                lineJoin: "round",
            }}
            eventHandlers={{
                click: () => {
                    onSelect(edge);
                },
                mouseover: () => {
                    setHovered(true);
                },
                mouseout: () => {
                    setHovered(false);
                },
            }}
        />
    );
}