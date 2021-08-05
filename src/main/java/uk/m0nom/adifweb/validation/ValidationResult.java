package uk.m0nom.adifweb.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.PropertySource;

@Getter
@Setter
@AllArgsConstructor
public class ValidationResult {
    public static final ValidationResult SUCCESS = new ValidationResult(true, "");
    public static final ValidationResult EMPTY = new ValidationResult(true, null);

    public ValidationResult(String error) {
        this(false, error);
    }

    private boolean valid;
    private String error;

    public boolean hasError() {
        return StringUtils.isNotEmpty(error);
    }
}
