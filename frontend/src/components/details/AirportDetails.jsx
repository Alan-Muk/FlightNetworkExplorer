export default function AirportDetails({
    airport
}) {

    return (

        <div
            style={{
                position: "absolute",
                right: 20,
                top: 20,
                width: 300,
                padding: 20,
                background: "#111",
                color: "white",
                borderRadius: 12,
                border: "1px solid #333"
            }}
        >

            <h2>
                {airport.iata}
            </h2>

            <h3>
                {airport.name}
            </h3>

            <p>
                {airport.city}
            </p>

            <p>
                {airport.country}
            </p>

        </div>

    );
}