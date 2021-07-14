package uk.m0nom.adifweb.validation;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;

public class EncodingValidator implements Validator {
    @Override
    public boolean isValid(String value)
    {
        return StringUtils.isEmpty(value) || Charset.forName(value) != null;
    }
}
