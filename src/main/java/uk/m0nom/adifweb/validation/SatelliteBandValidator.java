package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import org.marsik.ham.adif.enums.Band;

public class SatelliteBandValidator implements Validator {
    private final static String INVALID_BAND = "Invalid Band";

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        } else {
            // Check this is a valid band
            if (Band.findByCode(value.toLowerCase()) == null) {
                return new ValidationResult(INVALID_BAND);
            }
        }


        return ValidationResult.SUCCESS;
    }}
