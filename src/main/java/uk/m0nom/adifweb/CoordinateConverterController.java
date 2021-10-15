package uk.m0nom.adifweb;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.m0nom.coords.GlobalCoordinatesWithSourceAccuracy;
import uk.m0nom.coords.LocationParsers;
import uk.m0nom.coords.LocationSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class CoordinateConverterController {

	private static final Logger logger = Logger.getLogger(CoordinateConverterController.class.getName());

	private final LocationParsers parsers;

	public CoordinateConverterController(ApplicationConfiguration configuration) {
		this.parsers = new LocationParsers(configuration.getSummits());
	}

	@GetMapping("/coord")
	public String displayCoordForm(Model model) {
		model.addAttribute("location", "");
		model.addAttribute("errors", "");
		model.addAttribute("results", "");
		return "coord";
	}

	@PostMapping("/coord")
	public ModelAndView handleCoord(@RequestParam String location) {
		logger.info(String.format("Processing location: %s", location));
		String resultCoords = "";
		String errors = "";

		GlobalCoordinatesWithSourceAccuracy coordinates = parsers.parseStringForCoordinates(LocationSource.UNDEFINED, location);
		logger.info(String.format("Location parsed successfully: %s", coordinates));

		if (coordinates == null) {
			errors = "Can't parse the input string";
		} else {
			StringBuilder sb = new StringBuilder();
			List<String> formatted = parsers.format(coordinates);
			for (String format: formatted) {
				sb.append(format);
				sb.append("\n");
			}
			resultCoords = sb.toString();
		}

		Map<String, Object> results = new HashMap<>();
		results.put("location", location);
		results.put("results", resultCoords);
		results.put("errors", errors);

		return new ModelAndView("coord", results);
	}
}
