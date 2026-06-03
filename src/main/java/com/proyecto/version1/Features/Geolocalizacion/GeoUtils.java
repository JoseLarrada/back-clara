package com.proyecto.version1.Features.Geolocalizacion;

import java.math.BigDecimal;

/**
 * Utilidades geográficas para cálculos de distancia entre coordenadas GPS.
 * Usa la fórmula de Haversine para calcular la distancia sobre la superficie terrestre.
 */
public final class GeoUtils {

    private static final double RADIO_TIERRA_METROS = 6_371_000.0; // Radio medio de la Tierra en metros

    private GeoUtils() {
        // Clase utilitaria, no instanciable
    }

    /**
     * Calcula la distancia en metros entre dos puntos geográficos usando la fórmula de Haversine.
     *
     * @param lat1 Latitud del punto 1
     * @param lon1 Longitud del punto 1
     * @param lat2 Latitud del punto 2
     * @param lon2 Longitud del punto 2
     * @return Distancia en metros entre los dos puntos
     */
    public static double calcularDistanciaMetros(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_METROS * c;
    }
}
