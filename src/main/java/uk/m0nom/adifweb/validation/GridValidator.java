package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import uk.m0nom.maidenheadlocator.MaidenheadLocatorConversion;

public class GridValidator implements Validator {
    public final static String INVALID_GRID = "INVALID_GRID";

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        } else {
            try {
                MaidenheadLocatorConversion.isAValidGridSquare(value);
            } catch (UnsupportedOperationException e) {
                return new ValidationResult(INVALID_GRID);
            }
        }
        return ValidationResult.SUCCESS;
    }
}
