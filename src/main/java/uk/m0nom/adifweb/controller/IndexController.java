package uk.m0nom.adifweb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.logging.Logger;

@Controller
public class IndexController {

    @Value("${build.timestamp}")
    private String buildTimestamp;

    @Value("${build.version}")
    private String pomVersion;


    @GetMapping("/")
    public String displayCoordForm(Model model) {
        model.addAttribute("build_timestamp", buildTimestamp);
        model.addAttribute("pom_version", pomVersion);

        return "index";
    }
}