package utils;

public class Distancia {
    /**
     * Calcula la distancia en kilómetros entre dos puntos geográficos.
     *
     * @param lon1 Longitud del primer punto.
     * @param lat1 Latitud del primer punto.
     * @param lon2 Longitud del segundo punto.
     * @param lat2 Latitud del segundo punto.
     * @return Distancia en kilómetros.
     */
    public static float calcularDistancia(double lon1, double lat1, double lon2, double lat2) {
        double earthRadius = 6371; // Radio de la tierra en km

        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double dlon = (lon2 - lon1);
        double dlat = (lat2 - lat1);

        double sinlat = Math.sin(dlat / 2);
        double sinlon = Math.sin(dlon / 2);

        double a = (sinlat * sinlat) + Math.cos(lat1) * Math.cos(lat2) * (sinlon * sinlon);
        double c = 2 * Math.asin(Math.min(1.0, Math.sqrt(a)));

        return (float) (earthRadius * c);
    }
}
