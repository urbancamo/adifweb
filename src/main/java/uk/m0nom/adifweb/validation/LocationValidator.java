package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import uk.m0nom.coords.LocationParserResult;
import uk.m0nom.coords.LocationParsers;
import uk.m0nom.coords.LocationSource;

public class LocationValidator implements Validator {
    public final static String INCORRECT_FORMAT = "Unrecognised location format";

    @Override
    public ValidationResult isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        }

        LocationParsers parsers = new LocationParsers();
        LocationParserResult result = parsers.parseStringForCoordinates(LocationSource.UNDEFINED, value);
        if (result == null || result.getCoords() == null) {
            return new ValidationResult(INCORRECT_FORMAT);
        }
        return ValidationResult.SUCCESS;
    }
}
