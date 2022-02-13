package uk.m0nom.adifweb.validation;

import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.activity.ActivityType;
import uk.m0nom.adifweb.domain.HtmlParameterType;

import java.util.HashMap;
import java.util.Map;

public class Validators {

    private final Map<HtmlParameterType, Validator> validators;

    public Validators() {
        validators = new HashMap<>();
    }

    public void setupValidators(ActivityDatabases databases) {
        addValidator(HtmlParameterType.ENCODING, new EncodingValidator());
        addValidator(HtmlParameterType.FILENAME, new FilenameValidator());
        addValidator(HtmlParameterType.PRINTER_CONFIG, new PrinterConfigValidator());
        addValidator(HtmlParameterType.LOCATION, new LocationValidator());
        addValidator(HtmlParameterType.SOTA_REF, new ActivityValidator(databases, ActivityType.SOTA));
        addValidator(HtmlParameterType.GMA_REF, new ActivityValidator(databases, ActivityType.GMA));
        addValidator(HtmlParameterType.HEMA_REF, new ActivityValidator(databases, ActivityType.HEMA));
        addValidator(HtmlParameterType.WOTA_REF, new ActivityValidator(databases, ActivityType.WOTA));
        addValidator(HtmlParameterType.POTA_REF, new ActivityValidator(databases, ActivityType.POTA));
        addValidator(HtmlParameterType.WWFF_REF, new ActivityValidator(databases, ActivityType.WWFF));
        addValidator(HtmlParameterType.COTA_REF, new ActivityValidator(databases, ActivityType.COTA));
        addValidator(HtmlParameterType.LOTA_REF, new ActivityValidator(databases, ActivityType.LOTA));
        addValidator(HtmlParameterType.ROTA_REF, new ActivityValidator(databases, ActivityType.ROTA));
        addValidator(HtmlParameterType.IOTA_REF, new ActivityValidator(databases, ActivityType.IOTA));
        addValidator(HtmlParameterType.STATION_SUBLABEL, new BooleanValidator());
        addValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES, new BooleanValidator());
        addValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS, new DistanceValidator());
        addValidator(HtmlParameterType.ANTENNA, new AntennaValidator());
        addValidator(HtmlParameterType.CONTEST_RESULTS, new BooleanValidator());
        addValidator(HtmlParameterType.SATELLITE_NAME, new SatelliteNameValidator());
        addValidator(HtmlParameterType.SATELLITE_MODE, new SatelliteModeValidator());
        addValidator(HtmlParameterType.SATELLITE_BAND, new SatelliteBandValidator());
        addValidator(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT, new BooleanValidator());
        addValidator(HtmlParameterType.STRIP_COMMENT, new BooleanValidator());
        addValidator(HtmlParameterType.OPTIONS_VISIBLE, new BooleanValidator());
    }

    public void addValidator(HtmlParameterType type, Validator validator) {
        validators.put(type, validator);
    }

    public Validator getValidator(HtmlParameterType type) {
        return validators.get(type);
    }

}
