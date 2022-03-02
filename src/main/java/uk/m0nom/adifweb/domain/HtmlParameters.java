package uk.m0nom.adifweb.domain;

import lombok.Getter;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.adifweb.validation.BooleanValidator;
import uk.m0nom.adifweb.validation.ValidationResult;
import uk.m0nom.adifweb.validation.Validator;
import uk.m0nom.adifweb.validation.Validators;
import uk.m0nom.kml.activity.KmlLocalActivities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class HtmlParameters {

    private final Map<String, HtmlParameter> parameters;
    private final Validators validators;

    public HtmlParameters(ActivityDatabases databases) {
        parameters = new HashMap<>();
        validators = new Validators();
        validators.setupValidators(databases);
    }

    public void reset() {
        addParameter(new HtmlParameter(HtmlParameterType.FILENAME, "", validators.getValidator(HtmlParameterType.FILENAME)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.ENCODING, "windows-1251", validators.getValidator(HtmlParameterType.ENCODING)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOCATION, "", validators.getValidator(HtmlParameterType.LOCATION)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SOTA_REF, "", validators.getValidator(HtmlParameterType.SOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.GMA_REF, "", validators.getValidator(HtmlParameterType.GMA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.WOTA_REF, "", validators.getValidator(HtmlParameterType.WOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.HEMA_REF, "", validators.getValidator(HtmlParameterType.HEMA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.POTA_REF, "", validators.getValidator(HtmlParameterType.POTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.WWFF_REF, "", validators.getValidator(HtmlParameterType.WWFF_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.COTA_REF, "", validators.getValidator(HtmlParameterType.COTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOTA_REF, "", validators.getValidator(HtmlParameterType.LOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.ROTA_REF, "", validators.getValidator(HtmlParameterType.ROTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.IOTA_REF, "", validators.getValidator(HtmlParameterType.IOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.STATION_SUBLABEL, BooleanValidator.TRUE, validators.getValidator(HtmlParameterType.STATION_SUBLABEL)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOCAL_ACTIVATION_SITES, "", validators.getValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS, KmlLocalActivities.DEFAULT_RADIUS, validators.getValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.ANTENNA, "Vertical", validators.getValidator(HtmlParameterType.ANTENNA)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.CONTEST_RESULTS, "", validators.getValidator(HtmlParameterType.CONTEST_RESULTS)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_NAME, "", validators.getValidator(HtmlParameterType.SATELLITE_NAME)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_MODE, "", validators.getValidator(HtmlParameterType.SATELLITE_MODE)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_BAND, "", validators.getValidator(HtmlParameterType.SATELLITE_BAND)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT, "", validators.getValidator(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.STRIP_COMMENT, "", validators.getValidator(HtmlParameterType.STRIP_COMMENT)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.PRINTER_CONFIG, "", validators.getValidator(HtmlParameterType.PRINTER_CONFIG)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.OPTIONS_VISIBLE, "", validators.getValidator(HtmlParameterType.OPTIONS_VISIBLE)), parameters);
    }

    public void addParameter(HtmlParameter parameter, Map<String, HtmlParameter> parameters) {
        parameters.put(parameter.getKey(), parameter);
        parameter.setValidationResult(ValidationResult.EMPTY);
    }

    public void addParametersFromRequest(StandardMultipartHttpServletRequest request) {
        for (HtmlParameter parameter : parameters.values()) {
            addParameterFromRequest(parameter.getType(), request);
        }
    }

    private void addParameterFromRequest(HtmlParameterType type, StandardMultipartHttpServletRequest request) {
        String key = type.getParameterName();
        HtmlParameter parameter = new HtmlParameter(type, request.getParameter(key), validators.getValidator(type));
        parameters.put(key, parameter);
    }

    public void validate() {
        Collection<HtmlParameter> toValidate = parameters.values();
        for (HtmlParameter parameter : toValidate) {
            parameter.validate();
        }
    }

    public void put(String parameterName, HtmlParameter param) {
        parameters.put(parameterName, param);
    }

    public boolean isAllValid() {
        boolean allValid = true;
        for (HtmlParameter parameter : parameters.values()) {
            allValid &= parameter.getValidationResult().isValid();
        }
        return allValid;
    }

    public Collection<HtmlParameter> values() {
        return parameters.values();
    }

    public HtmlParameter get(String ref) {
        return parameters.get(ref);
    }

    public Validator getValidator(HtmlParameterType filename) {
        return validators.getValidator(filename);
    }
}
