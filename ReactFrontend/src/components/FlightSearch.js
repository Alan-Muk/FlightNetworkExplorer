import { useState } from "react";
import axios from "axios";

export default function FlightSearch() {
  const [flights, setFlights] = useState([]);

  const search = async () => {
    const res = await axios.get(
      "http://localhost:8080/api/flights?from=AMS&to=LHR&date=2026-05-01"
    );
    setFlights(res.data);
  };

  return (
    <div>
      <button onClick={search}>Search Flights</button>

      {flights.map((f, i) => (
        <div key={i}>
          {f.airline} | {f.departure} → {f.arrival}
        </div>
      ))}
    </div>
  );
}

/**
 * FlightSearch Component
 *
 * This React component fetches a list of flights from a backend API
 * based on a predefined route and date, and displays them in a simple list.
 *
 * Key features:
 *  - Fetch flights from an API endpoint
 *  - Display airline name, departure, and arrival times for each flight
 *  - Handle state management using React's useState hook
 *
 * Usage:
 *  - Click the "Search Flights" button to retrieve and display flights
 *  - The flights list updates automatically after a successful API call
 *
 * Dependencies:
 *  - axios: for making HTTP GET requests
 *  - React useState hook: for managing the flights state
 *
 * Notes:
 *  - The API endpoint is currently hardcoded for demonstration purposes
 *  - Error handling is not included in this example
 */
