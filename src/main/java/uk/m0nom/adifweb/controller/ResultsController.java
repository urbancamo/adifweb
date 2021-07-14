package uk.m0nom.adifweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ResultsController {

	@GetMapping("/results")
	public String resultsForm(Model model) {
		return "results";
	}
}
