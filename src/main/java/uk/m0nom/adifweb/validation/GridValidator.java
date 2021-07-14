package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import uk.m0nom.maidenheadlocator.MaidenheadLocatorConversion;

public class GridValidator implements Validator {
    @Override
    public boolean isValid(String value) {
        return StringUtils.isEmpty(value) || MaidenheadLocatorConversion.isAValidGridSquare(value);
    }
}
