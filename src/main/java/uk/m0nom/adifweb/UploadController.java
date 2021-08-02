package uk.m0nom.adifweb;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.marsik.ham.adif.Adif3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.adif3.Adif3FileReaderWriter;
import uk.m0nom.adif3.Adif3Transformer;
import uk.m0nom.adif3.UnsupportedHeaderException;
import uk.m0nom.adif3.args.TransformControl;
import uk.m0nom.adif3.contacts.Qsos;
import uk.m0nom.adif3.print.Adif3PrintFormatter;
import uk.m0nom.adif3.transform.TransformResults;
import uk.m0nom.adifweb.domain.ControlInfo;
import uk.m0nom.adifweb.domain.HtmlParameter;
import uk.m0nom.adifweb.domain.HtmlParameterType;
import uk.m0nom.adifweb.util.LatLongSplitter;
import uk.m0nom.adifweb.validation.ValidationResult;
import uk.m0nom.adifweb.validation.Validators;
import uk.m0nom.kml.KmlWriter;
import uk.m0nom.qrz.QrzXmlService;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;

@Controller
public class UploadController {
	private final static String ENCODING_PARAMETER = "encoding";
	private final static String LATLONG_PARAMETER = "latlong";
	private final static String GRID_PARAMETER = "grid";
	private final static String FILE_INPUT_PARAMETER = "filename";
	private final static String HEMA_PARAMETER = "hemaRef";
	private final static String WOTA_PARAMETER = "wotaRef";
	private final static String SOTA_PARAMETER = "sotaRef";
	private final static String POTA_PARAMETER = "potaRef";
	private final static String WWFF_PARAMETER = "wwffRef";

	private static final Logger logger = Logger.getLogger(UploadController.class.getName());

	@Autowired
	private ApplicationConfiguration configuration;

	private Map<String, HtmlParameter> parameters = new HashMap<>();
	private Validators validators = new Validators();

	@Autowired
	private ResourceLoader resourceLoader;

	

	@GetMapping("/upload")
	public String displayUploadForm(Model model) {
		validators.setupValidators(configuration.getSummits());
		model.addAttribute("error", "");
		model.addAttribute("upload", new ControlInfo());
		model.addAttribute("parameters", getDefaultParameters());
		return "upload";
	}
	
	private Map<String, HtmlParameter> getDefaultParameters() {
		Map<String, HtmlParameter> parameters = new HashMap<>();
		addParameter(new HtmlParameter(HtmlParameterType.FILENAME, FILE_INPUT_PARAMETER, "", validators.getValidator(HtmlParameterType.FILENAME)), parameters);
		addParameter(new HtmlParameter(HtmlParameterType.ENCODING, ENCODING_PARAMETER, "windows-1251", validators.getValidator(HtmlParameterType.ENCODING)), parameters);
		addParameter(new HtmlParameter(HtmlParameterType.LATLONG, LATLONG_PARAMETER, "", validators.getValidator(HtmlParameterType.LATLONG)), parameters);
		addParameter(new HtmlParameter(HtmlParameterType.GRID, GRID_PARAMETER, "", validators.getValidator(HtmlParameterType.GRID)), parameters);
		addParameter(new HtmlParameter(HtmlParameterType.SOTA_REF, SOTA_PARAMETER, "", validators.getValidator(HtmlParameterType.SOTA_REF)), parameters);
		addParameter(new HtmlParameter(HtmlParameterType.WOTA_REF, WOTA_PARAMETER, "", validators.getValidator(HtmlParameterType.WOTA_REF)), parameters);
		addParameter(new HtmlParameter(HtmlParameterType.HEMA_REF, HEMA_PARAMETER, "", validators.getValidator(HtmlParameterType.HEMA_REF)), parameters);
		addParameter(new HtmlParameter(HtmlParameterType.POTA_REF, POTA_PARAMETER, "", validators.getValidator(HtmlParameterType.POTA_REF)), parameters);
		addParameter(new HtmlParameter(HtmlParameterType.WWFF_REF, WWFF_PARAMETER, "", validators.getValidator(HtmlParameterType.WWFF_REF)), parameters);

		return parameters;
	}

	private void addParameter(HtmlParameter parameter, Map<String, HtmlParameter> parameters) {
		parameters.put(parameter.getKey(), parameter);
		parameter.setValidationResult(ValidationResult.EMPTY);
	}

	@PostMapping("/upload")
	public ModelAndView handleUpload(StandardMultipartHttpServletRequest request, ModelMap model, HttpServletResponse response) throws Exception {
		validators.setupValidators(configuration.getSummits());

		var factory = new DiskFileItemFactory();
		String tmpPath = System.getProperty("java.io.tmpdir");
		if (!StringUtils.endsWith(tmpPath, File.separator)) {
			tmpPath = tmpPath + File.separator;
		}
		factory.setRepository(new File(tmpPath));
		factory.setSizeThreshold(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD);
		factory.setFileCleaningTracker(null);

		ServletFileUpload upload = new ServletFileUpload(factory);
		MultipartFile file = request.getFile(FILE_INPUT_PARAMETER);

		addParameterFromRequest(HtmlParameterType.ENCODING, ENCODING_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.LATLONG, LATLONG_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.GRID, GRID_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.SOTA_REF, SOTA_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.WOTA_REF, WOTA_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.HEMA_REF, HEMA_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.POTA_REF, POTA_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.WWFF_REF, WWFF_PARAMETER, request);
		parameters.put(FILE_INPUT_PARAMETER, new HtmlParameter(HtmlParameterType.FILENAME, FILE_INPUT_PARAMETER,
				file.getOriginalFilename(), validators.getValidator(HtmlParameterType.FILENAME)));

		validateParameters(parameters);

		if (!HtmlParameter.isAllValid(parameters)) {
			ModelAndView backToUpload = new ModelAndView("upload");
			ModelMap map = backToUpload.getModelMap();
			map.put("validationErrors", "true");
			map.put("validationErrorMessages", getValidationErrorsString(parameters));
			map.put("parameters", parameters);
			return backToUpload;
		} else {
			TransformControl control = createTransformControlFromParameters();

			InputStream uploadedStream = file.getInputStream();
			long timestamp = new Date().getTime();

			String adifPath = String.format("%s%d-%s", tmpPath, timestamp, file.getOriginalFilename());
			OutputStream out = new FileOutputStream(adifPath);

			IOUtils.copy(uploadedStream, out);
			TransformResults transformResults = runTransformer(control, tmpPath, adifPath, FilenameUtils.getBaseName(file.getOriginalFilename()));

			if (transformResults.isErrors()) {
				ModelAndView backToUpload = new ModelAndView("upload");
				ModelMap map = backToUpload.getModelMap();
				map.put("error", transformResults.getError());
				map.put("parameters", parameters);
				return backToUpload;
			}

			Map<String, Object> results = new HashMap<>();
			results.put("adiFile", transformResults.getAdiFile());
			results.put("kmlFile", transformResults.getKmlFile());
			results.put("markdownFile", transformResults.getMarkdownFile());
			results.put("error", StringUtils.defaultIfEmpty(transformResults.getError(), "none"));
			results.put("validationErrors", getValidationErrorsString(parameters));
			StringBuilder sb = new StringBuilder("");
			for (String callsign : transformResults.getContactsWithoutLocation()) {
				sb.append(String.format("%s, ", callsign));
			}
			String callsignsWithoutLocation = "none";
			if (sb.length() > 0) {
				callsignsWithoutLocation = sb.substring(0, sb.length()-2);
			}
			results.put("callsignsWithoutLocation", callsignsWithoutLocation);
			return new ModelAndView("results", results);
		}
	}

	private String getValidationErrorsString(Map<String, HtmlParameter> parametersToValidate) {
		StringBuilder sb = new StringBuilder();

		for (HtmlParameter parameter : parametersToValidate.values()) {
			if (!parameter.getValidationResult().isValid()) {
				sb.append(parameter.getValidationResult().getError());
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private void addParameterFromRequest(HtmlParameterType type, String key, StandardMultipartHttpServletRequest request) {
		HtmlParameter parameter = new HtmlParameter(type, key, request.getParameter(key), validators.getValidator(type));
		parameters.put(key, parameter);
	}

	private void validateParameters(Map<String, HtmlParameter> parameters) {
		boolean valid = true;
		Collection<HtmlParameter> toValidate = parameters.values();
		for (HtmlParameter parameter : toValidate) {
			parameter.validate();
		}
	}

	private TransformControl createTransformControlFromParameters() {
		TransformControl control = new TransformControl();
		control.setMarkdown(true);
		control.setGenerateKml(true);

		control.setKmlS2s(true);
		control.setHema(parameters.get(HEMA_PARAMETER).getValue());
		control.setSota(parameters.get(SOTA_PARAMETER).getValue());
		control.setWota(parameters.get(WOTA_PARAMETER).getValue());
		control.setPota(parameters.get(POTA_PARAMETER).getValue());
		control.setWwff(parameters.get(WWFF_PARAMETER).getValue());
		control.setMyGrid(parameters.get(GRID_PARAMETER).getValue());
		//control.setMyLatitude(parametersToValidate.get(LATITUDE_PARAMETER).getValue());
		//control.setMyLongitude(parametersToValidate.get(LONGITUDE_PARAMTER).getValue());

		GlobalCoordinates coordinates = LatLongSplitter.split(parameters.get(LATLONG_PARAMETER).getValue());
		if (coordinates != null) {
			control.setMyLatitude(String.format("%f", coordinates.getLatitude()));
			control.setMyLongitude(String.format("%f", coordinates.getLongitude()));
		}
		control.setEncoding(parameters.get(ENCODING_PARAMETER).getValue());

		control.setKmlContactWidth(3);
		control.setKmlContactTransparency(20);
		control.setKmlContactColourByBand(false);
		control.setKmlContactShadow(true);
		control.setKmlS2sContactLineStyle("brick_red:50:2");
		control.setKmlContactLineStyle("baby_blue:50:2");
		control.setKmlFixedIconUrl("http://maps.google.com/mapfiles/kml/shapes/ranger_station.png");
		control.setKmlPortableIconUrl("http://maps.google.com/mapfiles/kml/shapes/hiker.png");
		control.setKmlMobileIconUrl("http://maps.google.com/mapfiles/kml/shapes/ranger_station.png");
		control.setKmlMaritimeIconUrl("http://maps.google.com/mapfiles/kml/shapes/sailing.png");
		control.setKmlParkIconUrl("http://maps.google.com/mapfiles/kml/shapes/picnic.png");
		control.setKmlSotaIconUrl("http://maps.google.com/mapfiles/kml/shapes/mountains.png");
		control.setKmlHemaIconUrl("http://maps.google.com/mapfiles/kml/shapes/hospitals.png");
		control.setKmlWotaIconUrl("http://maps.google.com/mapfiles/kml/shapes/trail.png");
		control.setKmlWwffIconUrl("http://maps.google.com/mapfiles/kml/shapes/parks.png");

		String qrzUsername = "M0NOM";
		String qrzPassword = "mark4qrzasm0nom";
		control.setUseQrzDotCom(StringUtils.isNotEmpty(qrzUsername));
		control.setQrzUsername(qrzUsername);
		control.setQrzPassword(qrzPassword);

		return control;
	}

	private TransformResults runTransformer(TransformControl control, String tmpPath, String inPath, String originalFilename) {
		TransformResults results = new TransformResults();
		QrzXmlService qrzXmlService = new QrzXmlService(control.getQrzUsername(), control.getQrzPassword());
		KmlWriter kmlWriter = new KmlWriter(control);

		Adif3Transformer transformer = configuration.getTransformer();
		ActivityDatabases summits = configuration.getSummits();
		Adif3FileReaderWriter readerWriter = configuration.getReaderWriter();
		Adif3PrintFormatter formatter = configuration.getFormatter();

		String outPath = tmpPath;

		String inBasename = FilenameUtils.getBaseName(inPath);
		String out = String.format("%s%s-%s.%s", outPath, inBasename, "fta", "adi");
		String kml = String.format("%s%s-%s.%s", outPath, inBasename, "fta", "kml");
		String markdown = String.format("%s%s-%s.%s", outPath, inBasename, "fta", "md");
		logger.info(String.format("Running from: %s", new File(".").getAbsolutePath()));
		try {
			if (control.getUseQrzDotCom()) {
				qrzXmlService.enable();
				if (!qrzXmlService.getSessionKey()) {
					logger.warning("Could not connect to QRZ.COM, disabling lookups and continuing...");
					qrzXmlService.disable();
				}
			}
			String adifProcessingConfigFilename = "classpath:config/adif-processor.yaml";
			Resource adifProcessorConfig = resourceLoader.getResource(adifProcessingConfigFilename);
			logger.info(String.format("Configuring transformer using: %s", adifProcessingConfigFilename));

			transformer.configure(adifProcessorConfig.getInputStream(), summits, qrzXmlService);

			logger.info(String.format("Reading input file %s with encoding %s", inPath, control.getEncoding()));
			Adif3 log = null;
			try {
				log = readerWriter.read(inPath, control.getEncoding(), false);
			} catch (Exception e) {
				String error = String.format("Error processing ADI file, caught exception:\n\t'%s'", e.getMessage());
				logger.severe(error);
				return new TransformResults(error);
			}
			Qsos qsos = transformer.transform(log, control);
			logger.info(String.format("Writing output file %s with encoding %s", out, control.getEncoding()));
			readerWriter.write(out, control.getEncoding(), log);
			if (control.getGenerateKml()) {

				kmlWriter.write(kml, originalFilename, summits, qsos, results);
				if (StringUtils.isNotEmpty(results.getError())) {
					kml = "";
				}
			}
			if (control.getMarkdown()) {
				BufferedWriter markdownWriter = null;
				try {
					File markdownFile = new File(markdown);
					if (markdownFile.exists()) {
						if (!markdownFile.delete()) {
							logger.severe(String.format("Error deleting Markdown file %s, check permissions?", markdown));
						}
					}
					if (markdownFile.createNewFile()) {
						String adifPrinterConfigFilename = "classpath:config/adif-printer-132-markdown.yaml";
						Resource adifPrinterConfig = resourceLoader.getResource(adifPrinterConfigFilename);
						logger.info(String.format("Configuring print job using: %s", adifPrinterConfigFilename));

						formatter.getPrintJobConfig().configure(adifPrinterConfig.getInputStream());
						logger.info(String.format("Writing Markdown to: %s", markdown));
						StringBuilder sb = formatter.format(log);
						markdownWriter = Files.newBufferedWriter(markdownFile.toPath(), Charset.forName(formatter.getPrintJobConfig().getOutEncoding()), StandardOpenOption.WRITE);
						markdownWriter.write(sb.toString());

						results.setAdiFile(FilenameUtils.getName(out));
						results.setKmlFile(FilenameUtils.getName(kml));
						results.setMarkdownFile(FilenameUtils.getName(markdown));
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
		return results;
	}
}
