package uk.m0nom.adifweb.validation;

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
        validators.put(HtmlParameterType.LATITUDE, new LatitudeValidator());
        validators.put(HtmlParameterType.LONGITUDE, new LongitudeValidator());
        validators.put(HtmlParameterType.ENCODING, new EncodingValidator());
        validators.put(HtmlParameterType.FILENAME, new FilenameValidator());
        validators.put(HtmlParameterType.GRID, new GridValidator());
        validators.put(HtmlParameterType.SOTA_REF, new ActivityValidator(databases, ActivityType.SOTA));
        validators.put(HtmlParameterType.HEMA_REF, new ActivityValidator(databases, ActivityType.HEMA));
        validators.put(HtmlParameterType.WOTA_REF, new ActivityValidator(databases, ActivityType.WOTA));
        validators.put(HtmlParameterType.POTA_REF, new ActivityValidator(databases, ActivityType.POTA));
    }

    public Validator getValidator(HtmlParameterType type) {
        return validators.get(type);
    }

}
