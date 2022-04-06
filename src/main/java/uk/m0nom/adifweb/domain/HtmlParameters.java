package uk.m0nom.adifweb.domain;

import lombok.Getter;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import uk.m0nom.adifweb.validation.BooleanValidator;
import uk.m0nom.adifweb.validation.ValidationResult;
import uk.m0nom.adifweb.validation.Validator;
import uk.m0nom.adifweb.validation.ValidatorService;
import uk.m0nom.adifproc.kml.activity.KmlLocalActivities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class HtmlParameters {

    private final Map<String, HtmlParameter> parameters;
    private final ValidatorService validatorService;

    public HtmlParameters(ValidatorService validatorService) {
        parameters = new HashMap<>();
        this.validatorService = validatorService;
    }

    public void reset() {
        addParameter(new HtmlParameter(HtmlParameterType.FILENAME, "", validatorService.getValidator(HtmlParameterType.FILENAME)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.ENCODING, "windows-1251", validatorService.getValidator(HtmlParameterType.ENCODING)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOCATION, "", validatorService.getValidator(HtmlParameterType.LOCATION)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SOTA_REF, "", validatorService.getValidator(HtmlParameterType.SOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.GMA_REF, "", validatorService.getValidator(HtmlParameterType.GMA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.WOTA_REF, "", validatorService.getValidator(HtmlParameterType.WOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.HEMA_REF, "", validatorService.getValidator(HtmlParameterType.HEMA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.POTA_REF, "", validatorService.getValidator(HtmlParameterType.POTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.WWFF_REF, "", validatorService.getValidator(HtmlParameterType.WWFF_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.COTA_REF, "", validatorService.getValidator(HtmlParameterType.COTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOTA_REF, "", validatorService.getValidator(HtmlParameterType.LOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.ROTA_REF, "", validatorService.getValidator(HtmlParameterType.ROTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.IOTA_REF, "", validatorService.getValidator(HtmlParameterType.IOTA_REF)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.STATION_SUBLABEL, BooleanValidator.TRUE, validatorService.getValidator(HtmlParameterType.STATION_SUBLABEL)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOCAL_ACTIVATION_SITES, "", validatorService.getValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS, KmlLocalActivities.DEFAULT_RADIUS, validatorService.getValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.ANTENNA, "Vertical", validatorService.getValidator(HtmlParameterType.ANTENNA)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.CONTEST_RESULTS, "", validatorService.getValidator(HtmlParameterType.CONTEST_RESULTS)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_NAME, "", validatorService.getValidator(HtmlParameterType.SATELLITE_NAME)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_MODE, "", validatorService.getValidator(HtmlParameterType.SATELLITE_MODE)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SATELLITE_BAND, "", validatorService.getValidator(HtmlParameterType.SATELLITE_BAND)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT, "", validatorService.getValidator(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.STRIP_COMMENT, "", validatorService.getValidator(HtmlParameterType.STRIP_COMMENT)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.PRINTER_CONFIG, "", validatorService.getValidator(HtmlParameterType.PRINTER_CONFIG)), parameters);
        addParameter(new HtmlParameter(HtmlParameterType.OPTIONS_VISIBLE, "", validatorService.getValidator(HtmlParameterType.OPTIONS_VISIBLE)), parameters);
    }

    public void addParameter(HtmlParameter parameter, Map<String, HtmlParameter> parameters) {
        parameters.put(parameter.getKey(), parameter);
        parameter.setValidationResult(ValidationResult.EMPTY);
    }

    public void addParametersFromRequest(StandardMultipartHttpServletRequest request) {
        parameters.values().forEach(p -> addParameterFromRequest(p.getType(), request));
    }

    private void addParameterFromRequest(HtmlParameterType type, StandardMultipartHttpServletRequest request) {
        String key = type.getParameterName();
        HtmlParameter parameter = new HtmlParameter(type, request.getParameter(key), validatorService.getValidator(type));
        parameters.put(key, parameter);
    }

    public void validate() {
        parameters.values().forEach(HtmlParameter::validate);
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
        return validatorService.getValidator(filename);
    }
}
