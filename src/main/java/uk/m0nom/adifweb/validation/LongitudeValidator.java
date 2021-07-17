package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class LongitudeValidator implements Validator {
    @Override
    public boolean isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        boolean valid = false;
        try {
            Double d = Double.parseDouble(value);
            valid = (d >= -180.0 || d <= 180.0);
        } catch (NumberFormatException e) {
            // Let this fall out
        }
        return valid;
    }
}
