package uk.m0nom.adifweb.validation;

import org.springframework.stereotype.Service;
import uk.m0nom.adifproc.activity.ActivityDatabaseService;
import uk.m0nom.adifproc.activity.ActivityType;
import uk.m0nom.adifproc.coords.LocationParsingService;
import uk.m0nom.adifproc.satellite.ApSatelliteService;
import uk.m0nom.adifweb.domain.HtmlParameterType;

import java.util.HashMap;
import java.util.Map;

@Service
public class ValidatorService {

    private final Map<HtmlParameterType, Validator> validators;

    public ValidatorService(ActivityDatabaseService activityDatabaseService,
                            LocationParsingService locationParsingService,
                            ApSatelliteService apSatelliteService) {
        validators = new HashMap<>();

        addValidator(HtmlParameterType.ENCODING, new EncodingValidator());
        addValidator(HtmlParameterType.FILENAME, new FilenameValidator());
        addValidator(HtmlParameterType.PRINTER_CONFIG, new PrinterConfigValidator());
        addValidator(HtmlParameterType.CALLSIGN, new CallsignValidator());
        addValidator(HtmlParameterType.LOCATION, new LocationValidator(locationParsingService));
        addValidator(HtmlParameterType.SOTA_REF, new ActivityValidator(activityDatabaseService, ActivityType.SOTA));
        addValidator(HtmlParameterType.GMA_REF, new ActivityValidator(activityDatabaseService, ActivityType.GMA));
        addValidator(HtmlParameterType.HEMA_REF, new ActivityValidator(activityDatabaseService, ActivityType.HEMA));
        addValidator(HtmlParameterType.WOTA_REF, new ActivityValidator(activityDatabaseService, ActivityType.WOTA));
        addValidator(HtmlParameterType.POTA_REF, new PotaValidator(activityDatabaseService));
        addValidator(HtmlParameterType.WWFF_REF, new ActivityValidator(activityDatabaseService, ActivityType.WWFF));
        addValidator(HtmlParameterType.COTA_REF, new ActivityValidator(activityDatabaseService, ActivityType.COTA));
        addValidator(HtmlParameterType.LOTA_REF, new ActivityValidator(activityDatabaseService, ActivityType.LOTA));
        addValidator(HtmlParameterType.ROTA_REF, new ActivityValidator(activityDatabaseService, ActivityType.ROTA));
        addValidator(HtmlParameterType.IOTA_REF, new ActivityValidator(activityDatabaseService, ActivityType.IOTA));
        addValidator(HtmlParameterType.BOTA_REF, new ActivityValidator(activityDatabaseService, ActivityType.BOTA));
        addValidator(HtmlParameterType.STATION_SUBLABEL, new BooleanValidator());
        addValidator(HtmlParameterType.ACTIVITY_SUBLABEL, new BooleanValidator());
        addValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES, new BooleanValidator());
        addValidator(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS, new DistanceValidator());
        addValidator(HtmlParameterType.ANTENNA, new AntennaValidator());
        addValidator(HtmlParameterType.CONTEST_RESULTS, new BooleanValidator());
        addValidator(HtmlParameterType.SATELLITE_NAME, new SatelliteNameValidator(apSatelliteService));
        addValidator(HtmlParameterType.SATELLITE_MODE, new SatelliteModeValidator());
        addValidator(HtmlParameterType.SATELLITE_BAND, new SatelliteBandValidator());
        addValidator(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT, new BooleanValidator());
        addValidator(HtmlParameterType.STRIP_COMMENT, new BooleanValidator());
        addValidator(HtmlParameterType.OPTIONS_VISIBLE, new BooleanValidator());
        addValidator(HtmlParameterType.DONT_QSL_CALLSIGNS, new CallsignListValidator());
        addValidator(HtmlParameterType.QSL_LABELS_INITIAL_POSITION, new IntegerRangeValidator(1, 24));
    }

    public void addValidator(HtmlParameterType type, Validator validator) {
        validators.put(type, validator);
    }

    public Validator getValidator(HtmlParameterType type) {
        return validators.get(type);
    }

}
