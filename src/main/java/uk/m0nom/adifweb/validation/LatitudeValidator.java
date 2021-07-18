package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class LatitudeValidator implements Validator {
    public final static String NAN = "LAT_NAN";
    public final static String UNDER_RANGE = "LAT_UNDER_RANGE";
    public final static String OVER_RANGE = "LA_OVER_RANGE";

    @Override
    public ValidationResult isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        }

        boolean valid = false;
        try {
            Double d = Double.parseDouble(value);
             if (d < -90.0) {
                 return new ValidationResult(UNDER_RANGE);
             } else if (d > 90.0){
                 return new ValidationResult(OVER_RANGE);
             }
            return ValidationResult.SUCCESS;
        } catch (NumberFormatException e) {
            return new ValidationResult(NAN);
        }
    }
}
