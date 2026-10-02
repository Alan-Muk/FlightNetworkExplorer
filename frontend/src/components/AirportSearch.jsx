import { useState } from "react";
import client from "../api/client";

/**
 * Small search box for jumping to an airport by IATA code.
 *
 * <p>On submit, verifies the airport exists via `/api/airports/{iata}`, then calls
 * `onSelect(iata)`. The parent is responsible for actually adding the airport to the map
 * and setting selection state.
 */
export default function AirportSearch({ onSelect }) {
    const [query, setQuery] = useState("");
    const [status, setStatus] = useState("idle"); // idle | loading | not-found | invalid | error

    async function submit(e) {
        e?.preventDefault();

        const iata = query.trim().toUpperCase();

        if (!/^[A-Z]{3}$/.test(iata)) {
            setStatus("invalid");
            return;
        }

        setStatus("loading");

        try {
            await client.get(`/airports/${iata}`);
            onSelect(iata);
            setQuery("");
            setStatus("idle");
        } catch (err) {
            if (err.response?.status === 404) {
                setStatus("not-found");
            } else {
                setStatus("error");
            }
        }
    }

    function handleChange(e) {
        setQuery(e.target.value.toUpperCase());
        if (status !== "idle") setStatus("idle");
    }

    const message = {
        idle: null,
        loading: "Searching…",
        "not-found": "Airport not found",
        invalid: "Enter a 3-letter IATA code",
        error: "Search failed",
    }[status];

    const isError =
        status === "not-found" || status === "invalid" || status === "error";

    return (
        <form
            onSubmit={submit}
            style={{
                display: "flex",
                alignItems: "center",
                gap: 8,
                position: "relative",
            }}
        >
            <input
                value={query}
                onChange={handleChange}
                placeholder="IATA"
                maxLength={3}
                autoComplete="off"
                spellCheck={false}
                style={{
                    background: "rgba(0,0,0,0.5)",
                    border: "1px solid #00ffff",
                    color: "#00ffff",
                    padding: "6px 12px",
                    borderRadius: 4,
                    fontFamily: "monospace",
                    fontSize: 14,
                    letterSpacing: "3px",
                    textTransform: "uppercase",
                    width: 90,
                    outline: "none",
                    textAlign: "center",
                }}
            />
            <button
                type="submit"
                style={{
                    background: "transparent",
                    border: "1px solid #00ffff",
                    color: "#00ffff",
                    padding: "6px 14px",
                    borderRadius: 4,
                    cursor: "pointer",
                    fontFamily: "monospace",
                    fontSize: 12,
                    letterSpacing: "1px",
                }}
            >
                SEARCH
            </button>
            {message && (
                <span
                    style={{
                        color: isError ? "#ff4d6d" : "#888",
                        fontSize: 12,
                        fontFamily: "monospace",
                        whiteSpace: "nowrap",
                    }}
                >
                    {message}
                </span>
            )}
        </form>
    );
}