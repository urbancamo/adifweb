package uk.m0nom.adifweb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.m0nom.activity.Activity;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.coords.GlobalCoordinatesWithSourceAccuracy;
import uk.m0nom.coords.LocationParsers;
import uk.m0nom.coords.LocationSource;
import uk.m0nom.geocoding.GeocodingProvider;
import uk.m0nom.geocoding.GeocodingResult;
import uk.m0nom.geocoding.NominatimGeocodingProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class CoordinateConverterController {

	private static final Logger logger = Logger.getLogger(CoordinateConverterController.class.getName());

	@Value("${build.timestamp}")
	private String buildTimestamp;

	@Value("${build.version}")
	private String pomVersion;

	private final LocationParsers parsers;
	private final ApplicationConfiguration configuration;
	private final GeocodingProvider geocodingProvider;

	public CoordinateConverterController(ApplicationConfiguration configuration) {
		this.configuration = configuration;
		this.parsers = new LocationParsers(configuration.getActivityDatabases());
		this.geocodingProvider = new NominatimGeocodingProvider();
	}

	@GetMapping("/coord")
	public String displayCoordForm(Model model) {
		model.addAttribute("location", "");
		model.addAttribute("errors", "");
		model.addAttribute("results", "");
		model.addAttribute("build_timestamp", buildTimestamp);
		model.addAttribute("pom_version", pomVersion);

		return "coord";
	}

	@PostMapping("/coord")
	public ModelAndView handleCoord(@RequestParam String location) {
		String locationToCheck = location.trim();
		logger.info(String.format("Processing location: %s", locationToCheck));
		String resultCoords = "";
		String errors = "";
		String info = "";

		GlobalCoordinatesWithSourceAccuracy coordinates = parsers.parseStringForCoordinates(LocationSource.UNDEFINED, locationToCheck);
		logger.info(String.format("Location parsed successfully: %s", coordinates));

		if (coordinates == null) {
			// OK try and find an activity reference
			Activity activity = configuration.getActivityDatabases().findActivity(locationToCheck);
			if (activity != null) {
				coordinates = activity.getCoords();
				if (coordinates == null) {
					info = String.format("Activity for %s found but it doesn't have a location", locationToCheck);
				} else {
					info = String.format("Activity reference match for %s location %s", activity.getType().getActivityDescription(), activity.getName());
				}
			} else {
				try {
					// OK try and find an address
					GeocodingResult result = geocodingProvider.getLocationFromAddress(locationToCheck);
					coordinates = result.getCoordinates();
					if (coordinates == null) {
						info = result.getError();
					} else {
						info = String.format("Geocoding result based on match of '%s'", result.getMatchedOn());
					}
				} catch (Exception e) {
					errors = "Problem using the geocoding provider";
				}
			}
		}

		if (coordinates != null) {
			StringBuilder sb = new StringBuilder();
			List<String> formatted = parsers.format(coordinates);
			for (String format: formatted) {
				sb.append(format);
				sb.append("\n");
			}
			resultCoords = sb.toString();
		}

		Map<String, Object> results = new HashMap<>();
		results.put("location", locationToCheck);
		results.put("results", resultCoords);
		results.put("info", info);
		results.put("errors", errors);
		results.put("build_timestamp", buildTimestamp);
		results.put("pom_version", pomVersion);

		return new ModelAndView("coord", results);
	}
}
