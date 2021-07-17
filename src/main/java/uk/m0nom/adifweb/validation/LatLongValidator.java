package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class LatLongValidator implements Validator {
    @Override
    public boolean isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        boolean valid = false;
        String[] latlong = StringUtils.split(value, ",");
        if (latlong.length == 2) {
            boolean latValid = new LatitudeValidator().isValid(latlong[0].trim());
            boolean longValid = new LongitudeValidator().isValid(latlong[1].trim());
            valid = latValid && longValid;
        }
        return valid;
    }
}
