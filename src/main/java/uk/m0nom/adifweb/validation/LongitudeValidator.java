package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class LongitudeValidator implements Validator {
    public final static String NAN = "Longitude not a number";
    public final static String UNDER_RANGE = "Latitude must be >= -180.0";
    public final static String OVER_RANGE = "Latitude must be <= 180.0";

    @Override
    public ValidationResult isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.SUCCESS;
        }

        boolean valid = false;
        try {
            Double d = Double.parseDouble(value);
            if (d < -180.0) {
                return new ValidationResult(UNDER_RANGE);
            } else if (d > 180.0) {
                return new ValidationResult(OVER_RANGE);
            }
            return ValidationResult.SUCCESS;
        } catch (NumberFormatException e) {
            return new ValidationResult(NAN);
        }
    }
}
