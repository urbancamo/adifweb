package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class BooleanValidator implements Validator {
    public final static String TRUE = "TRUE";

    @Override
    public ValidationResult isValid(String value) {
        return ValidationResult.SUCCESS;
    }
}
