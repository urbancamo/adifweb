package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class BooleanValidator implements Validator {
    public final static String TRUE = "on";
    public final static String NOT_A_BOOLEAN = "Value is not a boolean!";

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value) || StringUtils.equals(TRUE, value)) {
            return ValidationResult.SUCCESS;
        }
        return new ValidationResult(NOT_A_BOOLEAN);
    }
}
