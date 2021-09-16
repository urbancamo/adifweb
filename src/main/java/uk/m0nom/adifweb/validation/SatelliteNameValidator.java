package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import uk.m0nom.maidenheadlocator.MaidenheadLocatorConversion;
import uk.m0nom.satellite.Satellites;

public class SatelliteNameValidator implements Validator {
    public final static String UNSUPPORTED_SATELLITE = "Unsupported Satellite";

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        } else {
            Satellites satellites = new Satellites();
            if (satellites.getSatellite(value.toUpperCase()) == null) {
                return new ValidationResult(UNSUPPORTED_SATELLITE);
            }
        }
        return ValidationResult.SUCCESS;
    }}
