package uk.m0nom.adifweb.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Controller
public class IndexController {

    @Value("${build.timestamp}")
    private String buildTimestamp;

    @Value("${build.version}")
    private String pomVersion;

    private final String releaseNotes;

    public static String getResource(String classpathLocation) {
        try {
            return IOUtils.toString(Objects.requireNonNull(IndexController.class.getResourceAsStream(classpathLocation)), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not read file [ " + classpathLocation + " ] from classpath", e);
        }
    }
    public IndexController() {
        // Convert Markdown release notes to HTML
        String releaseNotesMd = getResource("/release-notes.md");
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse(releaseNotesMd);
        releaseNotes = renderer.render(document);
    }
    @GetMapping("/")
    public String displayIndexPage(Model model) {
        model.addAttribute("build_timestamp", buildTimestamp);
        model.addAttribute("pom_version", pomVersion);

        //
        model.addAttribute("release_notes", releaseNotes);
        return "index";
    }
}