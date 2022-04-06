package uk.m0nom.adifweb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.m0nom.adifweb.domain.LocationSearchResult;
import uk.m0nom.adifweb.location.LocationService;
import uk.m0nom.adifproc.coords.GlobalCoords3D;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class CoordinateConverterController {

	private static final Logger logger = Logger.getLogger(CoordinateConverterController.class.getName());

	@Value("${build.timestamp}")
	private String buildTimestamp;

	@Value("${build.version}")
	private String pomVersion;

	private final LocationService locationService;

	public CoordinateConverterController(LocationService locationService) {
		this.locationService = locationService;
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
		logger.info(String.format("Servicing location request %s", location));

		Map<String, Object> results = new HashMap<>();
		results.put("location", location);

		LocationSearchResult locationSearchResult = locationService.getLocation(location);
		GlobalCoords3D coordinates = locationSearchResult.getCoordinates();
		if (coordinates != null) {
			StringBuilder sb = new StringBuilder();
			for (String match : locationSearchResult.getMatches()) {
				sb.append(match);
				sb.append("\n");
			}
			results.put("results", sb.toString());
			results.put("info", locationSearchResult.getInfo());
			results.put("errors", locationSearchResult.getError());
			results.put("latitude", String.format("%.6f", coordinates.getLatitude()));
			results.put("longitude", String.format("%.6f", coordinates.getLongitude()));
			results.put("haveLocation", "true");
		} else {
			results.put("haveLocation", "false");
		}

		results.put("build_timestamp", buildTimestamp);
		results.put("pom_version", pomVersion);

		return new ModelAndView("coord", results);
	}
}
