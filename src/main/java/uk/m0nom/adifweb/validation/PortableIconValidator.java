package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class PortableIconValidator implements Validator {
    public final static String NO_PORTABLE_ICON_SPECIFIED = "NO_ICON_SPECIFIED";

    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return new ValidationResult(NO_PORTABLE_ICON_SPECIFIED);
        }
        return ValidationResult.SUCCESS;
    }
}
