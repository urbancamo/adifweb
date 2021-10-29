package uk.m0nom.adifweb;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.marsik.ham.adif.Adif3;
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
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.activity.ActivityType;
import uk.m0nom.adif3.Adif3Transformer;
import uk.m0nom.adif3.UnsupportedHeaderException;
import uk.m0nom.adif3.contacts.Qsos;
import uk.m0nom.adif3.control.TransformControl;
import uk.m0nom.adif3.print.Adif3PrintFormatter;
import uk.m0nom.adif3.transform.TransformResults;
import uk.m0nom.adifweb.domain.*;
import uk.m0nom.contest.ContestResultsCalculator;
import uk.m0nom.icons.IconResource;
import uk.m0nom.kml.KmlWriter;
import uk.m0nom.qrz.CachingQrzXmlService;
import uk.m0nom.qrz.QrzService;
import uk.m0nom.qsofile.QsoFileReader;
import uk.m0nom.qsofile.QsoFileWriter;

import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class UploadController {

	private static final String HTML_PARAMETERS = "HTML_PARAMETERS";

	@Value("${qrz.username}")
	private String qrzUsername;

	@Value("${qrz.password}")
	private String qrzPassword;

	@Value("${build.timestamp}")
	private String buildTimestamp;

	@Value("${build.version}")
	private String pomVersion;

	private PrintJobConfigs printJobConfigs;

	private static final Logger logger = Logger.getLogger(UploadController.class.getName());

	private final ApplicationConfiguration configuration;
	private final ResourceLoader resourceLoader;

	public UploadController(ApplicationConfiguration configuration, ResourceLoader resourceLoader) {
		this.configuration = configuration;
		this.resourceLoader = resourceLoader;
		this.printJobConfigs = new PrintJobConfigs(resourceLoader);
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
		HtmlParameters parameters = null;
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
		model.addAttribute("satellites", configuration.getSatellites().getSatelliteNames());
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
			map.put("satellites", configuration.getSatellites().getSatelliteNames());
			map.put("printJobConfigs", printJobConfigs.getConfigs());
			return backToUpload;
		} else {
			TransformControl control = createTransformControlFromParameters(parameters);

			InputStream uploadedStream = file.getInputStream();
			long timestamp = new Date().getTime();

			String inputPath = String.format("%s%d-%s", tmpPath, timestamp, file.getOriginalFilename());
			OutputStream out = new FileOutputStream(inputPath);

			IOUtils.copy(uploadedStream, out);
			TransformResults transformResults = runTransformer(control, tmpPath, inputPath, FilenameUtils.getBaseName(file.getOriginalFilename()));

			if (transformResults.isErrors()) {
				ModelAndView backToUpload = new ModelAndView("upload");
				ModelMap map = backToUpload.getModelMap();
				map.put("error", transformResults.getError());
				map.put("parameters", parameters);
				map.put("satellites", configuration.getSatellites().getSatelliteNames());
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

	private TransformControl createTransformControlFromParameters(HtmlParameters parameters) {
		TransformControl control = new TransformControl();
		control.setMarkdown(true);
		control.setGenerateKml(true);

		control.setKmlS2s(true);
		for (ActivityType activity : ActivityType.values()) {
			String ref = String.format("%sRef", activity.getActivityName().toLowerCase());
			if (parameters.get(ref) != null) {
				control.setActivityRef(activity, parameters.get(ref).getValue());
			}
		}

		control.setSatelliteName(parameters.get(HtmlParameterType.SATELLITE_NAME.getParameterName()).getValue());
		control.setSatelliteMode(parameters.get(HtmlParameterType.SATELLITE_MODE.getParameterName()).getValue());
		control.setSatelliteBand(parameters.get(HtmlParameterType.SATELLITE_BAND.getParameterName()).getValue());
		control.setSotaMicrowaveAwardComment(parameters.get(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT.getParameterName()).getValue() != null);
		control.setStripComment(parameters.get(HtmlParameterType.STRIP_COMMENT.getParameterName()).getValue() != null);

		control.setContestResults(parameters.get(HtmlParameterType.CONTEST_RESULTS.getParameterName()).getValue() != null);

		control.setLocation(parameters.get(HtmlParameterType.LOCATION.getParameterName()).getValue());
		control.setEncoding(parameters.get(HtmlParameterType.ENCODING.getParameterName()).getValue());

		control.setKmlContactWidth(3);
		control.setKmlContactTransparency(20);
		control.setKmlContactColourByBand(false);
		control.setKmlContactShadow(true);
		control.setKmlS2sContactLineStyle("brick_red:50:2");
		control.setKmlContactLineStyle("baby_blue:50:2");
		control.setIcon(IconResource.FIXED_ICON_NAME, IconResource.FIXED_DEFAULT_ICON_URL);
		control.setIcon(IconResource.PORTABLE_ICON_NAME, IconResource.PORTABLE_DEFAULT_ICON_URL);
		control.setIcon(IconResource.MOBILE_ICON_NAME, IconResource.MOBILE_DEFAULT_ICON_URL);
		control.setIcon(IconResource.MARITIME_MOBILE_ICON_NAME, IconResource.MARITIME_DEFAULT_ICON_URL);

		control.setIcon(ActivityType.POTA.getActivityName(), IconResource.POTA_DEFAULT_ICON_URL);
		control.setIcon(ActivityType.SOTA.getActivityName(), IconResource.SOTA_DEFAULT_ICON_URL);
		control.setIcon(ActivityType.HEMA.getActivityName(), IconResource.HEMA_DEFAULT_ICON_URL);
		control.setIcon(ActivityType.WOTA.getActivityName(), IconResource.HEMA_DEFAULT_ICON_URL);
		control.setIcon(ActivityType.WWFF.getActivityName(), IconResource.WWFF_DEFAULT_ICON_URL);
		control.setIcon(ActivityType.COTA.getActivityName(), IconResource.COTA_DEFAULT_ICON_URL);
		control.setIcon(ActivityType.LOTA.getActivityName(), IconResource.LOTA_DEFAULT_ICON_URL);
		control.setIcon(ActivityType.ROTA.getActivityName(), IconResource.ROTA_DEFAULT_ICON_URL);

		control.setIcon(IconResource.CW_ICON_NAME, IconResource.CW_DEFAULT_ICON_URL);
		control.setKmlShowStationSubLabel(null != parameters.get(HtmlParameterType.STATION_SUBLABEL.getParameterName()).getValue());
		control.setKmlShowLocalActivationSites(parameters.get(HtmlParameterType.LOCAL_ACTIVATION_SITES.getParameterName()).getValue() != null);
		control.setKmlLocalActivationSitesRadius(Double.valueOf(parameters.get(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS.getParameterName()).getValue()));
		control.setHfAntennaTakeoffAngle(Double.valueOf(parameters.get(HtmlParameterType.ANTENNA_TAKEOFF_ANGLE.getParameterName()).getValue()));

		control.setQrzUsername(qrzUsername);
		control.setQrzPassword(qrzPassword);
		control.setUseQrzDotCom(StringUtils.isNotEmpty(qrzUsername) && StringUtils.isNotEmpty(qrzPassword));
		control.setPrintConfigFile(parameters.get(HtmlParameterType.PRINTER_CONFIG.getParameterName()).getValue());

		return control;
	}

	private TransformResults runTransformer(TransformControl control, String tmpPath, String inPath, String originalFilename) {
		TransformResults results = new TransformResults();
		QrzService qrzService = new CachingQrzXmlService(control.getQrzUsername(), control.getQrzPassword());
		KmlWriter kmlWriter = new KmlWriter(control);

		Adif3Transformer transformer = configuration.getTransformer();
		ActivityDatabases summits = configuration.getActivityDatabases();
		QsoFileReader reader = configuration.getReader(inPath);
		QsoFileWriter writer = configuration.getWriter();

		Adif3PrintFormatter formatter = configuration.getFormatter();

		String inBasename = FilenameUtils.getBaseName(inPath);
		String out = String.format("%s%s.%s", tmpPath, inBasename, "adi");
		String kml = String.format("%s%s.%s", tmpPath, inBasename, "kml");

		logger.info(String.format("Running from: %s", new File(".").getAbsolutePath()));
		try {
			if (control.getUseQrzDotCom()) {
				qrzService.enable();
				if (!qrzService.getSessionKey()) {
					logger.warning("Could not connect to QRZ.COM, disabling lookups and continuing...");
					qrzService.disable();
				}
			}
			String adifProcessingConfigFilename = "classpath:config/adif-processor.yaml";
			Resource adifProcessorConfig = resourceLoader.getResource(adifProcessingConfigFilename);
			logger.info(String.format("Configuring transformer using: %s", adifProcessingConfigFilename));

			transformer.configure(adifProcessorConfig.getInputStream(), summits, qrzService);

			logger.info(String.format("Reading input file %s with encoding %s", inPath, control.getEncoding()));
			Adif3 log;
			try {
				log = reader.read(inPath, control.getEncoding(), false);
			} catch (Exception e) {
				String error = String.format("Error processing ADI file, caught exception:\n\t'%s'", e.getMessage());
				logger.severe(error);
				return new TransformResults(error);
			}
			Qsos qsos;

			try {
				qsos = transformer.transform(log, control);
			} catch (UnsupportedOperationException e) {
				return new TransformResults(e.getMessage());
			}
			if (control.getGenerateKml()) {

				kmlWriter.write(kml, originalFilename, summits, qsos, results);
				if (StringUtils.isNotEmpty(results.getError())) {
					kml = "";
				}
			}
			if (control.isContestResults()) {
				// Contest Calculations
				log.getHeader().setPreamble(new ContestResultsCalculator(summits).calculateResults(log));
			}
			logger.info(String.format("Writing output file %s with encoding %s", out, control.getEncoding()));
			writer.write(out, control.getEncoding(), log);

			if (control.isMarkdown()) {
				String adifPrinterConfigFilename = String.format("classpath:config/%s", control.getPrintConfigFile());
				Resource adifPrinterConfig = resourceLoader.getResource(adifPrinterConfigFilename);
				logger.info(String.format("Configuring print job using: %s", adifPrinterConfigFilename));

				formatter.getPrintJobConfig().configure(adifPrinterConfigFilename, adifPrinterConfig.getInputStream());
				String markdown = String.format("%s%s.%s", tmpPath, inBasename, formatter.getPrintJobConfig().getFilenameExtension());
				BufferedWriter markdownWriter = null;

				try {
					File formattedQsoFile = new File(markdown);
					if (formattedQsoFile.exists()) {
						if (!formattedQsoFile.delete()) {
							logger.severe(String.format("Error deleting Markdown file %s, check permissions?", markdown));
						}
					}
					if (formattedQsoFile.createNewFile()) {
						logger.info(String.format("Writing printer job to: %s", markdown));
						StringBuilder sb = formatter.format(log);
						markdownWriter = Files.newBufferedWriter(formattedQsoFile.toPath(), Charset.forName(formatter.getPrintJobConfig().getOutEncoding()), StandardOpenOption.WRITE);
						markdownWriter.write(sb.toString());

						results.setAdiFile(FilenameUtils.getName(out));
						results.setKmlFile(FilenameUtils.getName(kml));
						results.setFormattedQsoFile(FilenameUtils.getName(markdown));
					} else {
						logger.severe(String.format("Error creating Markdown file %s, check permissions?", markdown));
					}
				} catch (IOException ioe) {
					logger.severe(String.format("Error writing Markdown file %s: %s", markdown, ioe.getMessage()));
				} finally {
					if (markdownWriter != null) {
						markdownWriter.close();
					}
				}
			}
		} catch (NoSuchFileException nfe) {
			logger.severe(String.format("Could not open input file: %s", control.getPathname()));
		} catch (UnsupportedHeaderException ushe) {
			logger.severe(String.format("Unknown header for file: %s", inPath));
			logger.severe(ExceptionUtils.getStackTrace(ushe));
		} catch (IOException e) {
			logger.severe(String.format("Caught exception %s processing file: %s", e.getMessage(), inPath));
			logger.severe(ExceptionUtils.getStackTrace(e));
		}
		logger.info("Processing complete...");
		return results;
	}
}
