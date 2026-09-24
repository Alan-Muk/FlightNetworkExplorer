export default function StatusBar({
    originAirport,
    destinationAirport,
    routeCount,
    airportCount,
}) {
    let message;

    if (!originAirport) {
        message = "Select an airport to explore";
    } else if (!destinationAirport) {
        message = (
            <>
                Selected:{" "}
                <b style={{ color: "#00ffff", marginLeft: "5px" }}>
                    {originAirport.iata}
                </b>
                <span style={{ marginLeft: "20px", color: "#888" }}>
                    Click another airport to compare routes
                </span>
            </>
        );
    } else if (routeCount === 0) {
        message = (
            <>
                <b style={{ color: "#00ffff" }}>{originAirport.iata}</b>
                <span style={{ color: "#888", margin: "0 8px" }}>→</span>
                <b style={{ color: "#ff4d6d" }}>{destinationAirport.iata}</b>
                <span style={{ marginLeft: "20px", color: "#888" }}>
                    No routes found
                </span>
            </>
        );
    } else {
        message = (
            <>
                <b style={{ color: "#00ffff" }}>{originAirport.iata}</b>
                <span style={{ color: "#888", margin: "0 8px" }}>→</span>
                <b style={{ color: "#ff4d6d" }}>{destinationAirport.iata}</b>
                <span style={{ marginLeft: "20px" }}>
                    {routeCount} route{routeCount === 1 ? "" : "s"}
                </span>
            </>
        );
    }

    return (
        <footer
            style={{
                position: "absolute",
                bottom: 0,
                left: 0,
                width: "100%",
                height: "45px",
                zIndex: 3000,
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                padding: "0 20px",
                boxSizing: "border-box",
                background: "rgba(0,0,0,0.85)",
                borderTop: "1px solid #00ffff",
                color: "white",
                fontFamily: "monospace",
                fontSize: "13px",
            }}
        >
            <span
                style={{
                    display: "flex",
                    alignItems: "center",
                }}
            >
                {message}
            </span>
            <span style={{ color: "#666", fontSize: "11px" }}>
                {airportCount} airports visible
            </span>
        </footer>
    );
}