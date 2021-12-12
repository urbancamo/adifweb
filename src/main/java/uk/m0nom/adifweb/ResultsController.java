package uk.m0nom.adifweb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResultsController {

	@GetMapping("/results")
	public String resultsForm() {
		return "results";
	}
}
