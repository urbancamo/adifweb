package uk.m0nom.adifweb;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import uk.m0nom.adif3.control.TransformControl;
import uk.m0nom.adif3.transform.TransformResults;
import uk.m0nom.adifweb.domain.*;
import uk.m0nom.adifweb.transformer.TransformerService;
import uk.m0nom.adifweb.util.TransformControlUtils;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Displays and accepts the input from the main ADIF Processor HTML form
 */
@Controller
public class UploadController {

	private static final String HTML_PARAMETERS = "HTML_PARAMETERS";

	private static final Logger logger = Logger.getLogger(UploadController.class.getName());

	@Value("${build.timestamp}")
	private String buildTimestamp;

	@Value("${build.version}")
	private String pomVersion;

	private final PrintJobConfigs printJobConfigs;

	private final Adif3SchemaElements adif3SchemaElements;

	private final ApplicationConfiguration configuration;
	private final ResourceLoader resourceLoader;

	private final TransformerService transformerService;

	public UploadController(ApplicationConfiguration configuration, ResourceLoader resourceLoader) {
		this.configuration = configuration;
		this.resourceLoader = resourceLoader;
		this.printJobConfigs = new PrintJobConfigs(resourceLoader);
		this.adif3SchemaElements = new Adif3SchemaElements(resourceLoader);

		String adifProcessingConfigFilename = "classpath:config/adif-processor.yaml";
		Resource adifProcessorConfig = resourceLoader.getResource(adifProcessingConfigFilename);
		logger.info(String.format("Configuring transformer using: %s", adifProcessingConfigFilename));


		this.transformerService = new TransformerService(configuration, adifProcessorConfig);
	}

	private HtmlParameters setParametersFromSession(HttpSession session) {
		HtmlParameters parameters = null;
		if (session != null) {
			parameters = (HtmlParameters) session.getAttribute(HTML_PARAMETERS);
		}
		if (parameters == null) {
			parameters = new HtmlParameters(configuration.getActivityDatabases());
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
		model.addAttribute("parameters", parameters.getParameters());
		model.addAttribute("build_timestamp", buildTimestamp);
		model.addAttribute("pom_version", pomVersion);
		model.addAttribute("satellites", configuration.getApSatellites().getSatelliteNames());
		model.addAttribute("antennas", configuration.getAntennas().getAntennaNames());
		model.addAttribute("printJobConfigs", printJobConfigs.getConfigs());

		return "upload";
	}

	@PostMapping("/upload")
	public ModelAndView handleUpload(StandardMultipartHttpServletRequest request, HttpSession session) throws Exception {
		HtmlParameters parameters = setParametersFromSession(session);

		var factory = new DiskFileItemFactory();
		String tmpPath = System.getProperty("java.io.tmpdir");
		if (!StringUtils.endsWith(tmpPath, File.separator)) {
			tmpPath = tmpPath + File.separator;
		}
		factory.setRepository(new File(tmpPath));
		factory.setSizeThreshold(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD);
		factory.setFileCleaningTracker(null);

		MultipartFile file = request.getFile(HtmlParameterType.FILENAME.getParameterName());

		parameters.addParametersFromRequest(request);

		assert file != null;
		HtmlParameter fileParam = new HtmlParameter(HtmlParameterType.FILENAME,
				file.getOriginalFilename(), parameters.getValidator(HtmlParameterType.FILENAME));

		parameters.put(fileParam.getType().getParameterName(), fileParam);

		parameters.validate();
		session.setAttribute(HTML_PARAMETERS, parameters);

		if (!parameters.isAllValid()) {
			ModelAndView backToUpload = new ModelAndView("upload");
			ModelMap map = backToUpload.getModelMap();
			map.put("validationErrors", "true");
			map.put("validationErrorMessages", getValidationErrorsString(parameters));
			map.put("parameters", parameters);
			map.put("satellites", configuration.getApSatellites().getSatelliteNames());
			map.put("antennas", configuration.getAntennas().getAntennaNames());
			map.put("printJobConfigs", printJobConfigs.getConfigs());
			return backToUpload;
		} else {
			TransformControl control = TransformControlUtils.createTransformControlFromParameters(configuration, parameters);
			control.setAdif3ElementSet(adif3SchemaElements.getElements());
			control.setDxccEntities(configuration.getDxccEntities());

			InputStream uploadedStream = file.getInputStream();
			long timestamp = new Date().getTime();

			String inputPath = String.format("%s%d-%s", tmpPath, timestamp, file.getOriginalFilename());
			OutputStream out = new FileOutputStream(inputPath);

			IOUtils.copy(uploadedStream, out);

			TransformResults transformResults = transformerService.runTransformer(control, resourceLoader,
					tmpPath, inputPath, FilenameUtils.getBaseName(file.getOriginalFilename()));

			if (transformResults.hasErrors()) {
				ModelAndView backToUpload = new ModelAndView("upload");
				ModelMap map = backToUpload.getModelMap();
				map.put("error", transformResults.getError());
				map.put("parameters", parameters);
				map.put("satellites", configuration.getApSatellites().getSatelliteNames());
				map.put("antennas", configuration.getAntennas().getAntennaNames());
				map.put("printJobConfigs", printJobConfigs.getConfigs());
				return backToUpload;
			}

			Map<String, Object> results = new HashMap<>();
			results.put("adiFile", transformResults.getAdiFile());
			results.put("kmlFile", transformResults.getKmlFile());
			results.put("formattedQsoFile", transformResults.getFormattedQsoFile());
			results.put("error", StringUtils.defaultIfEmpty(transformResults.getError(), "none"));
			results.put("validationErrors", getValidationErrorsString(parameters));

			results.put("callsignsWithoutLocation", buildCallsignList(transformResults.getContactsWithoutLocation()));
			results.put("callsignsWithDubiousLocation", buildCallsignList(transformResults.getContactsWithDubiousLocation()));

			return new ModelAndView("results", results);
		}
	}

	private String buildCallsignList(Collection<String> callsigns) {
		StringBuilder sb = new StringBuilder();
		for (String callsign : callsigns) {
			sb.append(String.format("%s, ", callsign));
		}
		String rtn = "none";
		if (sb.length() > 0) {
			rtn = sb.substring(0, sb.length()-2);
		}
		return rtn;
	}

	private String getValidationErrorsString(HtmlParameters parametersToValidate) {
		StringBuilder sb = new StringBuilder();

		for (HtmlParameter parameter : parametersToValidate.values()) {
			if (!parameter.getValidationResult().isValid()) {
				sb.append(parameter.getValidationResult().getError());
				sb.append(" ");
			}
		}
		return sb.toString();
	}

}
