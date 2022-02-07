package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import uk.m0nom.antenna.Antennas;

public class AntennaValidator implements Validator {
    public final static String UNKNOWN_ANTENNA_TYPE = "Unknown Antenna Type";

    @Override
    public ValidationResult isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        }

        Antennas antennas = new Antennas();
        if (antennas.getAntenna(value) == null) {
            return new ValidationResult(UNKNOWN_ANTENNA_TYPE);
        }
        return ValidationResult.SUCCESS;
    }
}
