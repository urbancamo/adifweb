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
import uk.m0nom.coords.LocationParserResult;
import uk.m0nom.coords.LocationParsers;
import uk.m0nom.coords.LocationSource;
import uk.m0nom.geocoding.GeocodingProvider;
import uk.m0nom.geocoding.GeocodingResult;
import uk.m0nom.geocoding.NominatimGeocodingProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
		this.parsers = new LocationParsers();
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

		GlobalCoordinatesWithSourceAccuracy coordinates = null;
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

		Map<String, Object> results = new HashMap<>();
		if (coordinates != null) {
			StringBuilder sb = new StringBuilder();
			List<String> formatted = parsers.format(coordinates);
			for (String format: formatted) {
				sb.append(format);
				sb.append("\n");
			}
			resultCoords = sb.toString();
			results.put("latitude", String.format("%.6f", coordinates.getLatitude()));
			results.put("longitude", String.format("%.6f", coordinates.getLongitude()));
			results.put("haveLocation", "true");
		} else {
			results.put("haveLocation", "false");
		}

		results.put("location", locationToCheck);
		results.put("results", resultCoords);
		results.put("info", info);
		results.put("errors", errors);
		results.put("build_timestamp", buildTimestamp);
		results.put("pom_version", pomVersion);

		return new ModelAndView("coord", results);
	}
}
