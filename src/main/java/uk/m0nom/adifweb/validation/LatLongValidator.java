package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class LatLongValidator implements Validator {
    public final static String INCORRECT_FORMAT = "INCORRECT_FORMAT";

    @Override
    public ValidationResult isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        }

        boolean valid = false;
        String[] latlong = StringUtils.split(value, ",");
        if (latlong.length == 2) {
            ValidationResult latResult = new LatitudeValidator().isValid(latlong[0].trim());
            ValidationResult longResult = new LongitudeValidator().isValid(latlong[1].trim());
            if (!latResult.isValid()) {
                return latResult;
            }
            if (!longResult.isValid()) {
                return longResult;
            }
        } else {
            return new ValidationResult(INCORRECT_FORMAT);
        }
        return ValidationResult.SUCCESS;
    }
}
