package uk.m0nom.adifweb.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.m0nom.adifweb.location.LocationService;
import uk.m0nom.adifweb.domain.LocationSearchResult;

import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/api/location")
public class LocationApiController {

    private static final Logger logger = Logger.getLogger(LocationApiController.class.getName());

    private final LocationService locationService;

    public LocationApiController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping(path = "", produces = "application/json")
    @ApiOperation(value = "Find location", nickname = "Find Location")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Location found"),
            @ApiResponse(code = 404, message = "Could not find a location match"),
            @ApiResponse(code = 503, message = "Service unavailable, try again later")
    })
    public LocationSearchResult getLocation(@RequestParam String code)
    {
        logger.info(String.format("Servicing location API call requesting %s", code));
        LocationSearchResult result = locationService.getLocation(code);
        if (result.hasMatches()) {
            return result;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
