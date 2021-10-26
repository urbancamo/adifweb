package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

public class PrinterConfigValidator implements Validator {
    public final static String NO_PRINTER_CONFIG_SPECIFIED = "NO_PRINTER_CONFIG_SPECIFIED";

    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return new ValidationResult(NO_PRINTER_CONFIG_SPECIFIED);
        }
        return ValidationResult.SUCCESS;
    }
}
