package com.backend.backend.util;

/** Geographic helpers for computing distances between coordinates. */
public final class GeoUtils {

  private static final double EARTH_RADIUS_KM = 6371.0;

  private GeoUtils() {
    // utility class
  }

  /**
   * Great-circle distance between two points in kilometres using the Haversine formula.
   *
   * @param lat1 latitude of the first point, in degrees
   * @param lon1 longitude of the first point, in degrees
   * @param lat2 latitude of the second point, in degrees
   * @param lon2 longitude of the second point, in degrees
   * @return distance in kilometres
   */
  public static double haversine(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

    return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}
