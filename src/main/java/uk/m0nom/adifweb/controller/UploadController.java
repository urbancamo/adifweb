package uk.m0nom.adifweb.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import uk.m0nom.adifproc.adif3.transform.TransformResults;
import uk.m0nom.adifproc.icons.PortableIcons;
import uk.m0nom.adifproc.progress.ProgressFeedbackHandlerCallback;
import uk.m0nom.adifweb.ApplicationConfiguration;
import uk.m0nom.adifweb.domain.*;
import uk.m0nom.adifweb.file.FileService;
import uk.m0nom.adifweb.transformer.TransformerService;
import uk.m0nom.adifweb.util.CustomFileLogHandler;
import uk.m0nom.adifweb.util.LoggerSetup;
import uk.m0nom.adifweb.util.TransformControlUtils;
import uk.m0nom.adifweb.validation.ValidatorService;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Displays and accepts the input from the main ADIF Processor HTML form
 */
@Controller
public class UploadController implements ProgressFeedbackHandlerCallback {
	private static final Logger logger = Logger.getLogger(UploadController.class.getName());

	private static final String HTML_PARAMETERS = "HTML_PARAMETERS";

	@Value("${build.timestamp}")
	private String buildTimestamp;

	@Value("${build.version}")
	private String pomVersion;

	private final PrintJobConfigs printJobConfigs;
	private final PortableIcons portableIcons;
	private final Adif3SchemaService adif3SchemaService;

	private final ApplicationConfiguration configuration;

	private final TransformerService transformerService;
	private final FileService fileService;
	private final ValidatorService validatorService;

	private String tmpPath;

	private final WebSocketConfig webSocketConfig;

	private final String webSocketUri;

	public UploadController(ApplicationConfiguration configuration, TransformerService transformerService,
							PrintJobConfigs printJobConfigs, Adif3SchemaService adif3SchemaService,
							FileService fileService, ValidatorService validatorService,
							WebSocketConfig webSocketConfig, ConfigurableEnvironment environment) {
		this.configuration = configuration;
		this.printJobConfigs = printJobConfigs;
		this.adif3SchemaService = adif3SchemaService;
		this.fileService = fileService;
		this.transformerService = transformerService;
		this.validatorService = validatorService;
		this.webSocketConfig = webSocketConfig;

		this.portableIcons = new PortableIcons();
		tmpPath = System.getProperty("java.io.tmpdir");
		if (!StringUtils.endsWith(tmpPath, File.separator)) {
			tmpPath = tmpPath + File.separator;
		}

		if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
			webSocketUri = "ws://localhost:9090/progress";
		} else {
			webSocketUri = "ws://adifweb-env.eba-saseumwd.eu-west-2.elasticbeanstalk.com/progress";
		}
		logger.info(String.format("webSocketUri set to %s", webSocketUri));
	}

	private HtmlParameters setParametersFromSession(HttpSession session) {
		HtmlParameters parameters = null;
		if (session != null) {
			parameters = (HtmlParameters) session.getAttribute(HTML_PARAMETERS);
		}
		if (parameters == null) {
			parameters = new HtmlParameters(validatorService);
			parameters.reset();
		}
		return parameters;
	}

	@GetMapping("/upload")
	public String displayUploadForm(Model model, HttpSession session, @RequestParam(required=false) Boolean clear) {
		HtmlParameters parameters;
		if (clear != null) {
			parameters = setParametersFromSession(null);
		} else {
			parameters = setParametersFromSession(session);
		}

		model.addAttribute("error", "");
		model.addAttribute("upload", new ControlInfo());
		model.addAttribute("build_timestamp", buildTimestamp);
		model.addAttribute("pom_version", pomVersion);

		model.addAttribute("parameters", parameters.getParameters());
		model.addAttribute("satellites", configuration.getApSatelliteService().getSatelliteNames());
		model.addAttribute("antennas", configuration.getAntennaService().getAntennas());
		model.addAttribute("printJobConfigs", printJobConfigs.getConfigs());
		model.addAttribute("portableIcons", portableIcons.getIcons());
		model.addAttribute("maxQsosToProcess", Integer.toString(configuration.getMaxQsosToProcess()));
		model.addAttribute("webSocketUri", webSocketUri);
		return "upload";
	}

	@PostMapping("/upload")
	public ModelAndView handleUpload(
			StandardMultipartHttpServletRequest request, HttpSession session) {
		CustomFileLogHandler customFileLogHandler = null;
		ModelAndView rtn;
		try {
			long runTimestamp = java.time.Instant.now().toEpochMilli();
			customFileLogHandler = LoggerSetup.setupNewLogFile(runTimestamp);

			var parameters = setParametersFromSession(session);

			MultipartFile uploadedFile = request.getFile(HtmlParameterType.FILENAME.getParameterName());
			parameters.addParametersFromRequest(request);

			assert uploadedFile != null;
			var fileParam = new HtmlParameter(HtmlParameterType.FILENAME, uploadedFile.getOriginalFilename(), parameters.getValidator(HtmlParameterType.FILENAME));

			parameters.put(fileParam.getType().getParameterName(), fileParam);

			parameters.validate();
			session.setAttribute(HTML_PARAMETERS, parameters);

			if (!parameters.isAllValid()) {
				var backToUpload = new ModelAndView("upload");
				addBasicErrorElementsIntoMap(backToUpload.getModelMap(), parameters);
				rtn = backToUpload;
			} else {
				var control = TransformControlUtils.createTransformControlFromParameters(configuration, parameters);
				control.setRunTimestamp(runTimestamp);

				control.setAdif3Schema(adif3SchemaService.getSchema());
				control.setDxccEntities(configuration.getDxccEntities());
				control.setCountries(configuration.getCountries());
				
				fileService.archiveParameters(control, parameters);
				fileService.storeInputFile(control, uploadedFile, tmpPath);
				var transformResults = transformerService.runTransformer(control, tmpPath, uploadedFile.getOriginalFilename(), this, session.getId());
				if (transformResults.getAdiFile() != null) {
					fileService.archiveFile(transformResults.getAdiFile(), tmpPath);
				}
				if (transformResults.getKmlFile() != null) {
					fileService.archiveFile(transformResults.getKmlFile(), tmpPath);
				}
				if (transformResults.getFormattedQsoFile() != null) {
					fileService.archiveFile(transformResults.getFormattedQsoFile(), tmpPath);
				}
				if (transformResults.getQslLabelsFile() != null) {
					fileService.archiveFile(transformResults.getQslLabelsFile(), tmpPath);
				}

				if (customFileLogHandler != null) {
					String logFile = customFileLogHandler.getLogFile();
					customFileLogHandler.closeAndDetach();
					fileService.archiveFile(logFile, tmpPath);
				}

				if (transformResults.hasErrors()) {
					var backToUpload = new ModelAndView("upload");
					addBasicErrorElementsIntoMap(backToUpload.getModelMap(), parameters).addAttribute("error", transformResults.getError());
					rtn = backToUpload;
				} else {
					rtn = new ModelAndView("results", createTransformResults(transformResults));
				}
			}
		} finally {
			if (customFileLogHandler != null) {
				customFileLogHandler.closeAndDetach();
			}
		}
		return rtn;
	}

	private Map<String, Object> createTransformResults(TransformResults transformResults) {
		Map<String, Object> results = new HashMap<>();
		results.put("adiFile", transformResults.getAdiFile());
		results.put("kmlFile", transformResults.getKmlFile());
		results.put("formattedQsoFile", transformResults.getFormattedQsoFile());
		results.put("qslLabelsFile", transformResults.getQslLabelsFile());
		results.put("error", StringUtils.defaultIfEmpty(transformResults.getError(), "none"));
		results.put("warnings", String.join("\n", transformResults.getWarnings()));
		results.put("warningsRows", transformResults.getWarnings().size());
		results.put("callsignsWithoutLocation", String.join(", ", transformResults.getContactsWithoutLocation()));
		results.put("callsignsWithDubiousLocation", String.join("\n", transformResults.getContactsWithDubiousLocation()));
		results.put("callsignsWithDubiousLocationRows", transformResults.getContactsWithDubiousLocation().size());
		results.put("qslCallsigns", String.join(", ", transformResults.getQslContacts()));
		results.put("unknownSatellites", String.join(", ", transformResults.getUnknownSatellites()));
		results.put("unknownSatellitePasses", String.join(", ", transformResults.getUnknownSatellitePasses()));
		results.put("webSocketUri", webSocketUri);
		return results;
	}

	private ModelMap addBasicErrorElementsIntoMap(ModelMap map, HtmlParameters parameters) {
		map.put("parameters", parameters);
		map.put("satellites", configuration.getApSatelliteService().getSatelliteNames());
		map.put("antennas", configuration.getAntennaService().getAntennas());
		map.put("printJobConfigs", printJobConfigs.getConfigs());
		map.put("iconNames", portableIcons.getIconNames());
		map.put("maxQsosToProcess", configuration.getMaxQsosToProcess());
		map.put("webSocketUri", webSocketUri);
		return map;
	}

	@Override
	public void sendProgressUpdate(String sessionId, String message) {
		webSocketConfig.getProgressFeedbackHandler().sendProgressUpdate(sessionId, message);
	}
}

