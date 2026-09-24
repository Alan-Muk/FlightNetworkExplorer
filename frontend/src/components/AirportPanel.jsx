import { useEffect, useState } from "react";
import client from "../api/client";

export default function AirportPanel({ airport }) {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!airport) return;

        let cancelled = false;

        async function loadStats() {
            try {
                setLoading(true);
                setError(null);

                const response = await client.get(
                    `/airport/${airport.iata}/stats`,
                );

                if (!cancelled) {
                    setStats(response.data);
                }
            } catch (err) {
                console.error("Failed to load airport stats", err);

                if (!cancelled) {
                    setStats(null);
                    setError("Unable to load statistics");
                }
            } finally {
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

    if (!airport) return null;

    return (
        <div
            style={{
                background: "rgba(0,0,0,0.9)",
                color: "white",
                padding: 20,
                borderRadius: 12,
                border: "1px solid #00ffff",
                boxShadow: "0 0 20px rgba(0,255,255,.4)",
            }}
        >
            <h2 style={{ color: "#00ffff", margin: "0 0 8px" }}>
                {airport.iata}
            </h2>
            <h3 style={{ margin: "0 0 8px" }}>{airport.name}</h3>
            <p style={{ margin: "0 0 12px", color: "#aaa" }}>
                {airport.city}
                {airport.country && `, ${airport.country}`}
            </p>

            {loading && <p style={{ color: "#888" }}>Loading statistics...</p>}

            {error && <p style={{ color: "#ff4d6d" }}>{error}</p>}

            {stats && (
                <>
                    <hr
                        style={{
                            border: "none",
                            borderTop: "1px solid #333",
                            margin: "12px 0",
                        }}
                    />

                    <div
                        style={{
                            display: "grid",
                            gridTemplateColumns: "1fr 1fr 1fr",
                            gap: 8,
                            marginBottom: 16,
                        }}
                    >
                        <Stat label="Routes" value={stats.connections} />
                        <Stat label="Out" value={stats.outgoingRoutes} />
                        <Stat label="In" value={stats.incomingRoutes} />
                    </div>

                    {stats.topDestinations?.length > 0 && (
                        <>
                            <h4 style={{ margin: "16px 0 6px" }}>
                                Top destinations
                            </h4>
                            <ul style={{ margin: 0, padding: "0 0 0 20px" }}>
                                {stats.topDestinations.map((dest) => (
                                    <li key={dest}>{dest}</li>
                                ))}
                            </ul>
                        </>
                    )}

                    {stats.airlines?.length > 0 && (
                        <>
                            <h4 style={{ margin: "16px 0 6px" }}>Airlines</h4>
                            <div
                                style={{
                                    display: "flex",
                                    flexWrap: "wrap",
                                    gap: 6,
                                }}
                            >
                                {stats.airlines.map((airline) => (
                                    <span
                                        key={airline}
                                        style={{
                                            background: "#222",
                                            border: "1px solid #333",
                                            borderRadius: 999,
                                            padding: "4px 10px",
                                            fontSize: 12,
                                        }}
                                    >
                                        {airline}
                                    </span>
                                ))}
                            </div>
                        </>
                    )}
                </>
            )}
        </div>
    );
}

function Stat({ label, value }) {
    return (
        <div
            style={{
                background: "#1b1b22",
                borderRadius: 8,
                padding: "8px 10px",
                textAlign: "center",
            }}
        >
            <div style={{ color: "#888", fontSize: 11 }}>{label}</div>
            <div style={{ fontSize: 15, fontWeight: 600 }}>{value}</div>
        </div>
    );
}