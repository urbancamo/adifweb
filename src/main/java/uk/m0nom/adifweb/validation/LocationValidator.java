package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;
import uk.m0nom.adifproc.coords.LocationParserResult;
import uk.m0nom.adifproc.coords.LocationParsingService;
import uk.m0nom.adifproc.coords.LocationSource;

public class LocationValidator implements Validator {
    public final static String INCORRECT_FORMAT = "Unrecognised location format";

    public LocationParsingService parsingService;

    public LocationValidator(LocationParsingService parsingService) {
        this.parsingService = parsingService;
    }

    @Override
    public ValidationResult isValid(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;
        }

        LocationParserResult result = parsingService.parseStringForCoordinates(LocationSource.UNDEFINED, value);
        if (result == null || result.getCoords() == null) {
            return new ValidationResult(INCORRECT_FORMAT);
        }
        return ValidationResult.SUCCESS;
    }
}
