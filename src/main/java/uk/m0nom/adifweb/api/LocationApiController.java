package uk.m0nom.adifweb.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.m0nom.adifweb.ApplicationConfiguration;
import uk.m0nom.adifweb.domain.LocationSearchResult;
import uk.m0nom.location.LocationService;

import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/location-api")
public class LocationApiController {

    private static final Logger logger = Logger.getLogger(LocationApiController.class.getName());

    private LocationService locationService;

    public LocationApiController(ApplicationConfiguration configuration) {
        this.locationService = new LocationService(configuration);
    }


    @GetMapping(
            path = "/getLocation",
            produces = "application/json")
    public LocationSearchResult getLocation(@RequestParam String location)
    {
        logger.info(String.format("Servicing location API call requesting %s", location));

        return locationService.getLocation(location);
    }
}
