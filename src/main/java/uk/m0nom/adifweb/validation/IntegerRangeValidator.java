package uk.m0nom.adifweb.validation;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class IntegerRangeValidator implements Validator {
    public final static String NAN = "Value not a number";
    public final static String UNDER_RANGE = "Value must be >= 1";
    public final static String OVER_RANGE = "Value must be <= 24";

    private int min = 0;
    private int max = 0;

    public IntegerRangeValidator(int min, int max) {
        setMin(min);
        setMax(max);
    }

    @Override
    public ValidationResult isValid(String value) {
        try {
            if (StringUtils.isEmpty(value)) {
                return ValidationResult.EMPTY;
            }
            int val = Integer.valueOf(value);
            if (val < min)
                return new ValidationResult(UNDER_RANGE);
            else if (val > max)
                return new ValidationResult(OVER_RANGE);
            else
                return ValidationResult.SUCCESS;

        }
        catch (NumberFormatException nfe) {
            return new ValidationResult(NAN);
        }
    }
}
