package uk.m0nom.adifweb.validation;

import org.springframework.beans.factory.annotation.Autowired;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.activity.ActivityType;
import uk.m0nom.adifweb.domain.HtmlParameterType;

import java.util.HashMap;
import java.util.Map;

public class Validators {

    private Map<HtmlParameterType, Validator> validators;

    public Validators() {
        validators = new HashMap<>();
    }

    public void setupValidators(ActivityDatabases databases) {
        addValidator(HtmlParameterType.LATLONG, new LatLongValidator());
        addValidator(HtmlParameterType.LATITUDE, new LatitudeValidator());
        addValidator(HtmlParameterType.LONGITUDE, new LongitudeValidator());
        addValidator(HtmlParameterType.ENCODING, new EncodingValidator());
        addValidator(HtmlParameterType.FILENAME, new FilenameValidator());
        addValidator(HtmlParameterType.GRID, new GridValidator());
        addValidator(HtmlParameterType.SOTA_REF, new ActivityValidator(databases, ActivityType.SOTA));
        addValidator(HtmlParameterType.HEMA_REF, new ActivityValidator(databases, ActivityType.HEMA));
        addValidator(HtmlParameterType.WOTA_REF, new ActivityValidator(databases, ActivityType.WOTA));
        addValidator(HtmlParameterType.POTA_REF, new ActivityValidator(databases, ActivityType.POTA));
    }

    public void addValidator(HtmlParameterType type, Validator validator) {
        validators.put(type, validator);
    }
    public Validator getValidator(HtmlParameterType type) {
        return validators.get(type);
    }

}
