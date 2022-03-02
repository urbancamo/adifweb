package uk.m0nom.adifweb;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import uk.m0nom.adif3.transform.TransformResults;
import uk.m0nom.adifweb.domain.*;
import uk.m0nom.adifweb.file.FileService;
import uk.m0nom.adifweb.transformer.TransformerService;
import uk.m0nom.adifweb.util.TransformControlUtils;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays and accepts the input from the main ADIF Processor HTML form
 */
@Controller
public class UploadController {

	private static final String HTML_PARAMETERS = "HTML_PARAMETERS";

	@Value("${build.timestamp}")
	private String buildTimestamp;

	@Value("${build.version}")
	private String pomVersion;

	private final PrintJobConfigs printJobConfigs;

	private final Adif3SchemaElementsService adif3SchemaElementsService;

	private final ApplicationConfiguration configuration;

	private final TransformerService transformerService;
	private final FileService fileService;

	public UploadController(ApplicationConfiguration configuration, TransformerService transformerService,
							PrintJobConfigs printJobConfigs, Adif3SchemaElementsService adif3SchemaElementsService,
							FileService fileService) {
		this.configuration = configuration;
		this.printJobConfigs = printJobConfigs;
		this.adif3SchemaElementsService = adif3SchemaElementsService;
		this.fileService = fileService;
		this.transformerService = transformerService;
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
		model.addAttribute("build_timestamp", buildTimestamp);
		model.addAttribute("pom_version", pomVersion);

		model.addAttribute("parameters", parameters.getParameters());
		model.addAttribute("satellites", configuration.getApSatellites().getSatelliteNames());
		model.addAttribute("antennas", configuration.getAntennas().getAntennaNames());
		model.addAttribute("printJobConfigs", printJobConfigs.getConfigs());

		return "upload";
	}

	@PostMapping("/upload")
	public ModelAndView handleUpload(StandardMultipartHttpServletRequest request, HttpSession session) {
		var parameters = setParametersFromSession(session);

		var factory = new DiskFileItemFactory();
		var tmpPath = System.getProperty("java.io.tmpdir");
		if (!StringUtils.endsWith(tmpPath, File.separator)) {
			tmpPath = tmpPath + File.separator;
		}
		factory.setRepository(new File(tmpPath));
		factory.setSizeThreshold(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD);
		factory.setFileCleaningTracker(null);

		MultipartFile uploadedFile = request.getFile(HtmlParameterType.FILENAME.getParameterName());

		parameters.addParametersFromRequest(request);

		assert uploadedFile != null;
		var fileParam = new HtmlParameter(HtmlParameterType.FILENAME,
				uploadedFile.getOriginalFilename(), parameters.getValidator(HtmlParameterType.FILENAME));

		parameters.put(fileParam.getType().getParameterName(), fileParam);

		parameters.validate();
		session.setAttribute(HTML_PARAMETERS, parameters);

		if (!parameters.isAllValid()) {
			var backToUpload = new ModelAndView("upload");
			addBasicErrorElementsIntoMap(backToUpload.getModelMap(), parameters)
					.addAttribute("validationErrors", "true")
					.addAttribute("validationErrorMessages", getValidationErrorsString(parameters));
			return backToUpload;
		} else {
			var control = TransformControlUtils.createTransformControlFromParameters(configuration, parameters);
			control.setAdif3ElementSet(adif3SchemaElementsService.getElements());
			control.setDxccEntities(configuration.getDxccEntities());

			fileService.archiveParameters(control, parameters);
			fileService.storeInputFile(control, uploadedFile, tmpPath);
			var transformResults = transformerService.runTransformer(control,
					tmpPath, uploadedFile.getOriginalFilename());
			fileService.archiveFile(transformResults.getAdiFile(), tmpPath, control.getEncoding());
			fileService.archiveFile(transformResults.getKmlFile(), tmpPath, control.getEncoding());
			fileService.archiveFile(transformResults.getFormattedQsoFile(), tmpPath, configuration.getFormatter().getPrintJobConfig().getOutEncoding());

			if (transformResults.hasErrors()) {
				var backToUpload = new ModelAndView("upload");
				addBasicErrorElementsIntoMap(backToUpload.getModelMap(), parameters)
						.addAttribute("error", transformResults.getError());
				return backToUpload;
			}

			return new ModelAndView("results", createTransformResults(transformResults, parameters));
		}
	}

	private Map<String, Object> createTransformResults(TransformResults transformResults, HtmlParameters parameters) {
		Map<String, Object> results = new HashMap<>();
		results.put("adiFile", transformResults.getAdiFile());
		results.put("kmlFile", transformResults.getKmlFile());
		results.put("formattedQsoFile", transformResults.getFormattedQsoFile());
		results.put("error", StringUtils.defaultIfEmpty(transformResults.getError(), "none"));
		results.put("validationErrors", getValidationErrorsString(parameters));

		results.put("callsignsWithoutLocation", buildCallsignList(transformResults.getContactsWithoutLocation()));
		results.put("callsignsWithDubiousLocation", buildCallsignList(transformResults.getContactsWithDubiousLocation()));
		return results;
	}

	private ModelMap addBasicErrorElementsIntoMap(ModelMap map, HtmlParameters parameters) {
		map.put("parameters", parameters);
		map.put("satellites", configuration.getApSatellites().getSatelliteNames());
		map.put("antennas", configuration.getAntennas().getAntennaNames());
		map.put("printJobConfigs", printJobConfigs.getConfigs());
		return map;
	}

	private String buildCallsignList(Collection<String> callsigns) {
		var sb = new StringBuilder();
		for (var callsign : callsigns) {
			sb.append(String.format("%s, ", callsign));
		}
		var rtn = "none";
		if (sb.length() > 0) {
			rtn = sb.substring(0, sb.length()-2);
		}
		return rtn;
	}

	private String getValidationErrorsString(HtmlParameters parametersToValidate) {
		var sb = new StringBuilder();

		for (var parameter : parametersToValidate.values()) {
			if (!parameter.getValidationResult().isValid()) {
				sb.append(parameter.getValidationResult().getError());
				sb.append(" ");
			}
		}
		return sb.toString();
	}

}

