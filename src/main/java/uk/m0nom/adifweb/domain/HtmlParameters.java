package uk.m0nom.adifweb.domain;

import lombok.Getter;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.adifweb.validation.ValidationResult;
import uk.m0nom.adifweb.validation.Validator;
import uk.m0nom.adifweb.validation.Validators;
import uk.m0nom.comms.Ionosphere;
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
        addParameter(new HtmlParameter(HtmlParameterType.WOTA_REF, "", validators.getValidator(HtmlParameterType.WOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.HEMA_REF, "", validators.getValidator(HtmlParameterType.HEMA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.POTA_REF, "", validators.getValidator(HtmlParameterType.POTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.WWFF_REF, "", validators.getValidator(HtmlParameterType.WWFF_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.COTA_REF, "", validators.getValidator(HtmlParameterType.COTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOTA_REF, "", validators.getValidator(HtmlParameterType.LOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.ROTA_REF, "", validators.getValidator(HtmlParameterType.ROTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.STATION_SUBLABEL, "TRUE", validators.getValidator(HtmlParameterType.STATION_SUBLABEL)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOCAL_ACTIVATION_SITES, "", validators.getValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS, KmlLocalActivities.DEFAULT_RADIUS, validators.getValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.ANTENNA_TAKEOFF_ANGLE, String.format("%.2f", Ionosphere.HF_ANTENNA_DEFAULT_TAKEOFF_ANGLE), validators.getValidator(HtmlParameterType.ANTENNA_TAKEOFF_ANGLE)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.CONTEST_RESULTS, "", validators.getValidator(HtmlParameterType.CONTEST_RESULTS)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_NAME, "", validators.getValidator(HtmlParameterType.SATELLITE_NAME)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_MODE, "", validators.getValidator(HtmlParameterType.SATELLITE_MODE)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_BAND, "", validators.getValidator(HtmlParameterType.SATELLITE_BAND)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT, "", validators.getValidator(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.STRIP_COMMENT, "", validators.getValidator(HtmlParameterType.STRIP_COMMENT)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.PRINTER_CONFIG, "", validators.getValidator(HtmlParameterType.PRINTER_CONFIG)), parameters);
    }

    public void addParameter(HtmlParameter parameter, Map<String, HtmlParameter> parameters) {
        parameters.put(parameter.getKey(), parameter);
        parameter.setValidationResult(ValidationResult.EMPTY);
    }

    public void addParametersFromRequest(StandardMultipartHttpServletRequest request) {
        addParameterFromRequest(HtmlParameterType.ENCODING, request);
        addParameterFromRequest(HtmlParameterType.LOCATION, request);
        addParameterFromRequest(HtmlParameterType.SOTA_REF, request);
        addParameterFromRequest(HtmlParameterType.WOTA_REF, request);
        addParameterFromRequest(HtmlParameterType.HEMA_REF, request);
        addParameterFromRequest(HtmlParameterType.POTA_REF, request);
        addParameterFromRequest(HtmlParameterType.WWFF_REF, request);
        addParameterFromRequest(HtmlParameterType.COTA_REF, request);
        addParameterFromRequest(HtmlParameterType.LOTA_REF, request);
        addParameterFromRequest(HtmlParameterType.ROTA_REF, request);
        addParameterFromRequest(HtmlParameterType.SATELLITE_NAME, request);
        addParameterFromRequest(HtmlParameterType.SATELLITE_MODE, request);
        addParameterFromRequest(HtmlParameterType.SATELLITE_BAND, request);
        addParameterFromRequest(HtmlParameterType.STATION_SUBLABEL, request);
        addParameterFromRequest(HtmlParameterType.LOCAL_ACTIVATION_SITES, request);
        addParameterFromRequest(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS, request);
        addParameterFromRequest(HtmlParameterType.ANTENNA_TAKEOFF_ANGLE, request);
        addParameterFromRequest(HtmlParameterType.CONTEST_RESULTS, request);
        addParameterFromRequest(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT, request);
        addParameterFromRequest(HtmlParameterType.STRIP_COMMENT, request);
        addParameterFromRequest(HtmlParameterType.PRINTER_CONFIG, request);
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
