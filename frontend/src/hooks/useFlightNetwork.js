import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import client from "../api/client";

/**
 * Owns all state for the flight network exploration feature:
 *  - the set of airports currently visible on the map
 *  - which airport is focused / origin / destination
 *  - the current route comparison results and selected route
 *
 * Actions returned by this hook are stable (wrapped in useCallback) so they can safely
 * be passed to child components without causing re-renders.
 */
export function useFlightNetwork() {
  const [airports, setAirports] = useState([]);
  const [expandedAirports, setExpandedAirports] = useState([]);
  const [focusedAirport, setFocusedAirport] = useState(null);
  const [originAirport, setOriginAirport] = useState(null);
  const [destinationAirport, setDestinationAirport] = useState(null);
  const [routes, setRoutes] = useState([]);
  const [selectedRoute, setSelectedRoute] = useState(null);

  const loadAirports = useCallback(async () => {
    const response = await client.get("/hubs");
    setAirports(response.data);
  }, []);

  useEffect(() => {
    loadAirports();
  }, [loadAirports]);

  const inflight = useRef(new Set());

  const expandAirport = useCallback(async (iata) => {
    setAirports((previous) => {
      const airport = previous.find((item) => item.iata === iata);
      if (airport) setFocusedAirport(airport);
      return previous;
    });

    if (inflight.current.has(iata)) return;
    inflight.current.add(iata);

    try {
      const response = await client.get(`/network/${iata}`);
      const nodes = response.data.nodes ?? [];
      setAirports((prev) => {
        const map = {};
        prev.forEach((a) => {
          map[a.iata] = a;
        });
        nodes.forEach((a) => {
          map[a.iata] = a;
        });
        return Object.values(map);
      });
      setExpandedAirports((prev) =>
        prev.includes(iata) ? prev : [...prev, iata],
      );
    } catch (err) {
      inflight.current.delete(iata); // allow retry on failure
      throw err;
    }
  }, []);

  const expandRouteAirports = useCallback(
    async (routeResults, airportsSnapshot) => {
      const missing = [];

      routeResults.forEach((route) => {
        if (!route.airports) return;

        route.airports.forEach((iata) => {
          const exists = airportsSnapshot.some(
            (airport) => airport.iata === iata,
          );
          if (!exists && !missing.includes(iata)) {
            missing.push(iata);
          }
        });
      });

      for (const iata of missing) {
        await expandAirport(iata);
      }
    },
    [expandAirport],
  );

  const selectAirport = useCallback(
    async (iata) => {
      const airport = airports.find((item) => item.iata === iata);
      if (!airport) return;

      await expandAirport(iata);

      // Resolve the origin against the latest airports list
      const currentOrigin = originAirport;

      if (!currentOrigin) {
        setOriginAirport(airport);
        setDestinationAirport(null);
        setRoutes([]);
        setSelectedRoute(null);
        return;
      }

      if (!destinationAirport && currentOrigin.iata !== airport.iata) {
        setDestinationAirport(airport);

        const response = await client.get(
          `/routes/compare/${currentOrigin.iata}/${airport.iata}`,
        );

        const routeResults = response.data.routes ?? [];
        setRoutes(routeResults);

        await expandRouteAirports(routeResults, airports);
      }
    },
    [
      airports,
      originAirport,
      destinationAirport,
      expandAirport,
      expandRouteAirports,
    ],
  );

  const selectRoute = useCallback((route) => {
    setSelectedRoute(route);
  }, []);

  const clearSelection = useCallback(() => {
    setOriginAirport(null);
    setDestinationAirport(null);
    setRoutes([]);
    setSelectedRoute(null);
    setFocusedAirport(null);
  }, []);

  const lookup = useMemo(() => {
    const result = {};
    airports.forEach((airport) => {
      result[airport.iata] = airport;
    });
    return result;
  }, [airports]);

  const activeAirports = useMemo(() => {
    if (routes.length === 0) return null;

    const set = new Set();
    routes.forEach((route) => {
      route.airports?.forEach((iata) => set.add(iata));
    });
    return set;
  }, [routes]);

  return {
    // state
    airports,
    expandedAirports,
    focusedAirport,
    originAirport,
    destinationAirport,
    routes,
    selectedRoute,

    // derived
    lookup,
    activeAirports,

    // actions
    loadAirports,
    expandAirport,
    selectAirport,
    selectRoute,
    clearSelection,
  };
}
