package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class FilenameValidator implements Validator {
    public final static String NO_FILE_SPECIFIED = "NO_FILE_SPECIFIED";

    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return new ValidationResult(NO_FILE_SPECIFIED);
        }
        return ValidationResult.SUCCESS;
    }
}
