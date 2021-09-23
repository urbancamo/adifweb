package uk.m0nom.adifweb.validation;

public class BooleanValidator implements Validator {

    @Override
    public ValidationResult isValid(String value) {
        return ValidationResult.SUCCESS;
    }
}
