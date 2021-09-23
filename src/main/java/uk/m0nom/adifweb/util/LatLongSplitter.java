package uk.m0nom.adifweb.util;

import org.apache.commons.lang3.StringUtils;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.util.logging.Logger;

public class LatLongSplitter {
    private static final Logger logger = Logger.getLogger(LatLongSplitter.class.getName());

    public static GlobalCoordinates split(String value) {
        if (StringUtils.isNotEmpty(value)) {
            String[] latlong = StringUtils.split(value, ",");
            if (latlong.length == 2) {
                try {
                    double latitude = Double.parseDouble(latlong[0].trim());
                    double longitude = Double.parseDouble(latlong[1].trim());
                    return new GlobalCoordinates(latitude, longitude);
                } catch (NumberFormatException nfe) {
                    logger.severe(String.format("Invalid latitude/longitude comma separated string: %s", value));
                }
            }
        }
        return null;
    }
}
