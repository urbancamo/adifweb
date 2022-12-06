package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class DistanceValidator implements Validator {
    private final static Double MAX_DISTANCE = 1000.0;
    public final static String NAN = "Distance not a number";
    public final static String UNDER_RANGE = "Distance must be > 0.0";
    public final static String OVER_RANGE = String.format("Distance must be <= %.0f",  MAX_DISTANCE);

    @Override
    public ValidationResult isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        }

        try {
            double d = Double.parseDouble(value);
             if (d <= 0.0) {
                 return new ValidationResult(UNDER_RANGE);
             } else if (d > MAX_DISTANCE){
                 return new ValidationResult(OVER_RANGE);
             }
            return ValidationResult.SUCCESS;
        } catch (NumberFormatException e) {
            return new ValidationResult(NAN);
        }
    }
}
