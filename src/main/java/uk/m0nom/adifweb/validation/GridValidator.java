package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import uk.m0nom.adifproc.coords.LocationSource;
import uk.m0nom.adifproc.maidenheadlocator.MaidenheadLocatorConversion;

public class GridValidator implements Validator {
    public final static String INVALID_GRID = "Invalid Grid, accepts 4/6/8/10 char refs";

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        } else {
            try {
                MaidenheadLocatorConversion.locatorToCoords(LocationSource.UNDEFINED, value);
            } catch (UnsupportedOperationException e) {
                return new ValidationResult(INVALID_GRID);
            }
        }
        return ValidationResult.SUCCESS;
    }
}
