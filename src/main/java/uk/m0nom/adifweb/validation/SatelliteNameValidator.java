package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import uk.m0nom.adifproc.satellite.ApSatellite;
import uk.m0nom.adifproc.satellite.ApSatelliteService;

import java.time.LocalDate;

public class SatelliteNameValidator implements Validator {
    public final static String UNSUPPORTED_SATELLITE = "Unsupported Satellite";

    private final ApSatelliteService apSatelliteService;

    public SatelliteNameValidator(ApSatelliteService apSatelliteService) {
        this.apSatelliteService = apSatelliteService;
    }

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        } else {
            ApSatellite satellite = apSatelliteService.getSatellite(value.toUpperCase(), LocalDate.now());
            if (satellite == null) {
                return new ValidationResult(UNSUPPORTED_SATELLITE);
            }
        }
        return ValidationResult.SUCCESS;
    }}
