package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class EncodingValidator implements Validator {
    public final static String BAD_CHARSET = "Unknown character set name";

    @Override
    public ValidationResult isValid(String value)
    {
        if (!StringUtils.isEmpty(value)) {
            try {
                Charset.forName(value);
            } catch (UnsupportedCharsetException e) {
                return new ValidationResult(BAD_CHARSET);
            }
        }
        return ValidationResult.SUCCESS;
    }
}
