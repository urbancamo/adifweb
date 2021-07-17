package uk.m0nom.adifweb;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tomcat.jni.Global;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.marsik.ham.adif.Adif3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	private final static String LATITUDE_PARAMETER = "latitude";
	private final static String LONGITUDE_PARAMTER = "longitude";
	private final static String GRID_PARAMETER = "grid";
	private final static String FILE_INPUT_PARAMETER = "fileInput";
	private final static String HEMA_PARAMETER = "hemaRef";
	private final static String WOTA_PARAMETER = "wotaRef";
	private final static String SOTA_PARAMETER = "sotaRef";
	private final static String POTA_PARAMETER = "potaRef";

	private static final Logger logger = Logger.getLogger(UploadController.class.getName());

	@Autowired
	private ApplicationConfiguration configuration;

	private Map<String, HtmlParameter> parametersToValidate = new HashMap<>();
	private Validators validators = new Validators();

	@Autowired
	private ResourceLoader resourceLoader;

	@GetMapping("/")
	public RedirectView redirectWithUsingRedirectView(
			RedirectAttributes attributes) {
		return new RedirectView("/upload");
	}

	@GetMapping("/upload")
	public String displayUploadForm(Model model) {
		model.addAttribute("upload", new ControlInfo());
		return "upload";
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
		addParameterFromRequest(HtmlParameterType.LONGITUDE, LONGITUDE_PARAMTER, request);
		addParameterFromRequest(HtmlParameterType.GRID, GRID_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.SOTA_REF, SOTA_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.WOTA_REF, WOTA_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.HEMA_REF, HEMA_PARAMETER, request);
		addParameterFromRequest(HtmlParameterType.POTA_REF, POTA_PARAMETER, request);
		parametersToValidate.put("filename", new HtmlParameter(HtmlParameterType.FILENAME, "filename", file.getOriginalFilename(), validators.getValidator(HtmlParameterType.FILENAME)));

		if (!validateParameters(parametersToValidate)) {
			return new ModelAndView("upload", parametersToValidate);
		} else {
			TransformControl control = createTransformControlFromParameters();

			InputStream uploadedStream = file.getInputStream();
			long timestamp = new Date().getTime();

			String adifPath = String.format("%s%d-%s", tmpPath, timestamp, file.getOriginalFilename());
			OutputStream out = new FileOutputStream(adifPath);

			IOUtils.copy(uploadedStream, out);
			TransformResults transformResults = runTransformer(control, tmpPath, adifPath, FilenameUtils.getBaseName(file.getOriginalFilename()));
			Map<String, Object> results = new HashMap<>();
			results.put("adiFile", transformResults.getAdiFile());
			results.put("kmlFile", transformResults.getKmlFile());
			results.put("markdownFile", transformResults.getMarkdownFile());
			results.put("error", StringUtils.defaultIfEmpty(transformResults.getError(), "none"));
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

	private void addParameterFromRequest(HtmlParameterType type, String key, StandardMultipartHttpServletRequest request) {
		HtmlParameter parameter = new HtmlParameter(type, key, request.getParameter(key), validators.getValidator(type));
		parametersToValidate.put(key, parameter);
	}

	private boolean validateParameters(Map<String, HtmlParameter> parameters) {
		boolean valid = true;
		Collection<HtmlParameter> toValidate = parameters.values();

		for (HtmlParameter parameter : toValidate) {
			valid &= parameter.isValid();
		}
		return valid;
	}

	private TransformControl createTransformControlFromParameters() {
		TransformControl control = new TransformControl();
		control.setMarkdown(true);
		control.setGenerateKml(true);

		control.setKmlS2s(true);
		control.setHema(parametersToValidate.get(HEMA_PARAMETER).getValue());
		control.setSota(parametersToValidate.get(SOTA_PARAMETER).getValue());
		control.setWota(parametersToValidate.get(WOTA_PARAMETER).getValue());
		control.setPota(parametersToValidate.get(POTA_PARAMETER).getValue());
		control.setMyGrid(parametersToValidate.get(GRID_PARAMETER).getValue());
		//control.setMyLatitude(parametersToValidate.get(LATITUDE_PARAMETER).getValue());
		//control.setMyLongitude(parametersToValidate.get(LONGITUDE_PARAMTER).getValue());

		GlobalCoordinates coordinates = LatLongSplitter.split(parametersToValidate.get(LATLONG_PARAMETER).getValue());
		if (coordinates != null) {
			control.setMyLatitude(String.format("%f", coordinates.getLatitude()));
			control.setMyLongitude(String.format("%f", coordinates.getLongitude()));
		}
		control.setEncoding(parametersToValidate.get(ENCODING_PARAMETER).getValue());

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

		String qrzUsername = "M0NOM";
		String qrzPassword = "WindermereIsMyQTH";
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
			Adif3 log = readerWriter.read(inPath, control.getEncoding(), false);
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
