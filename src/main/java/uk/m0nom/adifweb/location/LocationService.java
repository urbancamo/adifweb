package uk.m0nom.adifweb.location;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.m0nom.activity.Activity;
import uk.m0nom.adifweb.ApplicationConfiguration;
import uk.m0nom.adifweb.domain.LocationSearchResult;
import uk.m0nom.coords.GlobalCoords3D;
import uk.m0nom.coords.LocationParserResult;
import uk.m0nom.coords.LocationParsers;
import uk.m0nom.coords.LocationSource;
import uk.m0nom.geocoding.GeocodingProvider;
import uk.m0nom.geocoding.GeocodingResult;
import uk.m0nom.geocoding.NominatimGeocodingProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Service
public class LocationService {
    private static final Logger logger = Logger.getLogger(LocationService.class.getName());

    private final LocationParsers parsers;
    private final ApplicationConfiguration configuration;
    private final GeocodingProvider geocodingProvider;


    public LocationService(ApplicationConfiguration configuration) {
        this.configuration = configuration;
        this.parsers = new LocationParsers();
        this.geocodingProvider = new NominatimGeocodingProvider();
    }


    public LocationSearchResult getLocation(String location) {
        String locationToCheck = location.trim();
        logger.info(String.format("Processing location: %s", locationToCheck));
        String errors = "";
        String info = "";

        GlobalCoords3D coordinates = null;
        LocationParserResult result = parsers.parseStringForCoordinates(LocationSource.UNDEFINED, locationToCheck);
        if (result != null) {
            coordinates = result.getCoords();
            logger.info(String.format("Location parsed successfully as %s: %s", result.getParser().getName(), coordinates));
            info = String.format("Location parsed successfully as %s", result.getParser().getName());
        } else {
            // OK try and find an activity reference
            Activity activity = configuration.getActivityDatabases().findActivity(locationToCheck.toUpperCase());
            if (activity != null) {
                coordinates = activity.getCoords();
                if (coordinates == null) {
                    info = String.format("Activity reference match for %s location '%s' has no coordinates defined", activity.getType().getActivityDescription(), activity.getName());
                } else {
                    info = String.format("Activity reference match for %s location '%s'", activity.getType().getActivityDescription(), activity.getName());
                }
            } else {
                try {
                    // OK try and find an address
                    GeocodingResult geocodingResult = geocodingProvider.getLocationFromAddress(locationToCheck);
                    coordinates = geocodingResult.getCoordinates();
                    if (coordinates == null) {
                        info = geocodingResult.getError();
                    } else {
                        info = String.format("Geocoding result based on match of '%s'", geocodingResult.getMatchedOn());
                    }
                } catch (Exception e) {
                    errors = "Problem using the geocoding provider";
                }
            }
        }

        LocationSearchResult locationSearchResult = new LocationSearchResult();
        locationSearchResult.setCoordinates(coordinates);
        Collection<String> resultCoords = new ArrayList<>();
        if (coordinates != null) {
            List<String> formatted = parsers.format(coordinates);
            resultCoords.addAll(formatted);
            locationSearchResult.setMatches(resultCoords);
        }

        if (StringUtils.isNotBlank(info)) {
            locationSearchResult.setInfo(info);
        }
        if (StringUtils.isNotBlank(errors)) {
            locationSearchResult.setError(errors);
        }
        return locationSearchResult;
    }
}
