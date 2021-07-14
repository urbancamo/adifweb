package uk.m0nom.adifweb.domain;

import lombok.Getter;
import lombok.Setter;
import uk.m0nom.adifweb.validation.Validator;

@Getter
@Setter
public class HtmlParameter {
    private HtmlParameterType type;
    private String key;
    private String value;
    private Validator validator;

    public HtmlParameter(HtmlParameterType type, String key, String value, Validator validator) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.validator = validator;
    }

    public boolean isValid() {
        return validator.isValid(value);
    }
}
