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
import uk.m0nom.adifweb.activity.ActivityService;
import uk.m0nom.adifweb.domain.ActivitySearchResult;

import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/api/activity")
public class ActivityApiController {

    private static final Logger logger = Logger.getLogger(ActivityApiController.class.getName());

    private final ActivityService activityService;

    public ActivityApiController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping(path = "", produces = "application/json")
    @ApiOperation(value = "Find matching activities", nickname = "Find matching activities")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Could not find any matching activities"),
            @ApiResponse(code = 503, message = "Service unavailable, try again later")
    })
    public ActivitySearchResult getLocation(@RequestParam String substring)
    {
        logger.info(String.format("Servicing activity matching API call requesting %s", substring));
        ActivitySearchResult result = activityService.getMatchingActivities(substring);
        if (result.hasMatches()) {
            return result;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
