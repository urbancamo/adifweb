package uk.m0nom.adifweb.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.m0nom.adifweb.validation.BooleanValidator;
import uk.m0nom.adifweb.validation.ValidationResult;
import uk.m0nom.adifweb.validation.Validator;

@Getter
@Setter
public class HtmlParameter {
    private HtmlParameterType type;
    private String key;
    private String value = null;
    private Validator validator;
    private ValidationResult validationResult;

    public HtmlParameter(HtmlParameterType type, String value, Validator validator) {
        this.type = type;
        this.key = type.getParameterName();
        if (value != null) {
            this.value = value.trim();
        }
        this.validator = validator;
    }

    public boolean isValid() { return validationResult.isValid(); }

    public void validate() {
        validationResult = validator.isValid(value);
    }

    public boolean getValueAsBoolean() {
        return StringUtils.equalsIgnoreCase(BooleanValidator.TRUE, value);
    }
}
