package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class SatelliteModeValidator implements Validator {

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        }
        return ValidationResult.SUCCESS;
    }}
