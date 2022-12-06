package uk.m0nom.adifweb.transformer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.marsik.ham.adif.Adif3;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.m0nom.adifproc.activity.ActivityDatabaseService;
import uk.m0nom.adifproc.adif3.Adif3Transformer;
import uk.m0nom.adifproc.adif3.UnsupportedHeaderException;
import uk.m0nom.adifproc.adif3.contacts.Qsos;
import uk.m0nom.adifproc.adif3.control.TransformControl;
import uk.m0nom.adifproc.adif3.label.Adif3LabelFormatter;
import uk.m0nom.adifproc.adif3.label.Adif3LabelFormatterResult;
import uk.m0nom.adifproc.adif3.print.Adif3PrintFormatter;
import uk.m0nom.adifproc.adif3.transform.TransformResults;
import uk.m0nom.adifproc.contest.ContestResultsCalculator;
import uk.m0nom.adifproc.kml.KmlWriter;
import uk.m0nom.adifproc.qrz.CachingQrzXmlService;
import uk.m0nom.adifproc.qsofile.QsoFileReader;
import uk.m0nom.adifproc.qsofile.QsoFileWriter;
import uk.m0nom.adifweb.ApplicationConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

/**
 * Runs the ADIF transformer to process the ADIF input file
 */
@Service
public class TransformerService {
    private static final Logger logger = Logger.getLogger(TransformerService.class.getName());
    private final ApplicationConfiguration configuration;
    private final ResourceLoader resourceLoader;
    private final CachingQrzXmlService qrzService;
    private final KmlWriter kmlWriter;

    public TransformerService(ApplicationConfiguration configuration,
                              ResourceLoader resourceLoader,
                              KmlWriter kmlWriter,
                              CachingQrzXmlService qrzService) {
        this.configuration = configuration;
        this.resourceLoader = resourceLoader;
        this.qrzService = qrzService;
        this.kmlWriter = kmlWriter;
    }

    public TransformResults runTransformer(TransformControl control,
                                           String tmpPath, String originalFilename) {
        TransformResults results = new TransformResults();
        qrzService.setCredentials(control.getQrzUsername(), control.getQrzPassword());

        String adifPrinterConfigFilename = String.format("classpath:config/%s", control.getPrintConfigFile());
        Resource adifPrinterConfig = resourceLoader.getResource(adifPrinterConfigFilename);
        logger.info(String.format("Configuring print job using: %s", adifPrinterConfigFilename));

        var adifProcessingConfigFilename = "classpath:config/adif-processor.yaml";
        var adifProcessorConfig = resourceLoader.getResource(adifProcessingConfigFilename);
        logger.info(String.format("Configuring transformer using: %s", adifProcessingConfigFilename));

        Adif3Transformer transformer = configuration.getTransformer();
        ActivityDatabaseService summits = configuration.getActivityDatabases();
        String inBasename = FilenameUtils.getBaseName(originalFilename);
        String inExtension = FilenameUtils.getExtension(originalFilename);
        String in = String.format("%s%d-in-%s.%s", tmpPath, control.getRunTimestamp(), inBasename, inExtension);
        QsoFileReader reader = configuration.getReader(in);
        QsoFileWriter writer = configuration.getWriter();

        Adif3PrintFormatter printFormatter = configuration.getPrintFormatter();
        Adif3LabelFormatter labelFormatter = configuration.getLabelFormatter();

        String out = String.format("%s%d-out-%s.%s", tmpPath, control.getRunTimestamp(), inBasename, "adi");
        String kml = String.format("%s%d-out-%s.%s", tmpPath, control.getRunTimestamp(), inBasename, "kml");

        logger.info(String.format("Running from: %s", new File(".").getAbsolutePath()));
        try {
            if (control.hasQrzCredentials()) {
                if (!qrzService.refreshSessionKey()) {
                    logger.warning("Could not connect to QRZ.COM, disabling lookups and continuing...");
                    results.setError("Could not obtain QRZ.COM session key, some stations may not have a location");
                }
            }

            logger.info(String.format("Reading input file %s with encoding %s", in, control.getEncoding()));
            Adif3 log;
            try {
                log = reader.read(in, control.getEncoding(), false);
            } catch (Exception e) {
                String error = String.format("Error processing %s file, caught exception:\n\t'%s'", inExtension.toUpperCase(), e.getMessage());
                logger.severe(error);
                return new TransformResults(error);
            }
            Qsos qsos;

            try {
                qsos = transformer.transform(log, control, results);
            } catch (UnsupportedOperationException e) {
                return new TransformResults(e.getMessage());
            }
            if (control.getGenerateKml()) {
                kml = kmlWriter.write(control, kml, originalFilename, summits, qsos, results);
            }
            if (control.isContestResults()) {
                // Contest Calculations
                log.getHeader().setPreamble(new ContestResultsCalculator(summits).calculateResults(log));
            }
            if (control.isFormattedOutput()) {
                printFormatter.getPrintJobConfig().configure(adifPrinterConfigFilename, adifPrinterConfig.getInputStream());
                String markdown = String.format("%s%d-log-%s.%s", tmpPath, control.getRunTimestamp(), inBasename, printFormatter.getPrintJobConfig().getFilenameExtension());
                BufferedWriter markdownWriter = null;

                try {
                    File formattedQsoFile = new File(markdown);
                    if (formattedQsoFile.exists()) {
                        if (!formattedQsoFile.delete()) {
                            logger.severe(String.format("Error deleting QSO log file %s, check permissions?", markdown));
                        }
                    }
                    if (formattedQsoFile.createNewFile()) {
                        logger.info(String.format("Writing QSO log to: %s", markdown));
                        StringBuilder sb = printFormatter.format(qsos);
                        markdownWriter = Files.newBufferedWriter(formattedQsoFile.toPath(), Charset.forName(printFormatter.getPrintJobConfig().getOutEncoding()), StandardOpenOption.WRITE);
                        markdownWriter.write(sb.toString());
                        results.setFormattedQsoFile(FilenameUtils.getName(markdown));
                    } else {
                        logger.severe(String.format("Error creating QSO log %s, check permissions?", markdown));
                    }
                } catch (UnmappableCharacterException uce) {
                    logger.severe("Unmappable character in input file, consider a UTF-8 format log file instead");
                } catch (IOException ioe) {
                    logger.severe(String.format("Error writing QSO log %s: %s", markdown, ioe.getMessage()));
                } finally {
                    if (markdownWriter != null) {
                        markdownWriter.close();
                    }
                }
            }

            if (control.isQslLabels()) {
                String labels = String.format("%s%d-lab-%s.%s", tmpPath, control.getRunTimestamp(), inBasename, "txt");
                BufferedWriter qslLabelsWriter = null;

                try {
                    File qslLabelsFile = new File(labels);
                    if (qslLabelsFile.exists()) {
                        if (!qslLabelsFile.delete()) {
                            logger.severe(String.format("Error deleting QSL Labels File %s, check permissions?", labels));
                        }
                    }
                    if (qslLabelsFile.createNewFile()) {
                        logger.info(String.format("Writing QSL labels to: %s", labels));
                        Adif3LabelFormatterResult qslResult = labelFormatter.format(qsos, control.getDontQslCallsigns());
                        qslLabelsWriter = Files.newBufferedWriter(qslLabelsFile.toPath(), Charset.forName(printFormatter.getPrintJobConfig().getOutEncoding()), StandardOpenOption.WRITE);
                        qslLabelsWriter.write(qslResult.getLabels());

                        results.setQslLabelsFile(FilenameUtils.getName(labels));
                        results.setQslContacts(qslResult.getCallsigns());
                    } else {
                        logger.severe(String.format("Error creating QSL Labels file %s, check permissions?", labels));
                    }
                } catch (IOException ioe) {
                    logger.severe(String.format("Error writing QSL Labels %s: %s", labels, ioe.getMessage()));
                } finally {
                    if (qslLabelsWriter != null) {
                        qslLabelsWriter.close();
                    }
                }
            }

            logger.info(String.format("Writing QSO log file %s with encoding %s", out, control.getEncoding()));
            writer.write(out, control.getEncoding(), log);

            results.setAdiFile(FilenameUtils.getName(out));
            results.setKmlFile(FilenameUtils.getName(kml));

        } catch (NoSuchFileException nfe) {
            logger.severe(String.format("Could not open input file: %s", control.getPathname()));
        } catch (UnsupportedHeaderException ushe) {
            logger.severe(String.format("Unknown header for file: %s", in));
            logger.severe(ExceptionUtils.getStackTrace(ushe));
        } catch (IOException e) {
            logger.severe(String.format("Caught exception %s processing file: %s", e.getMessage(), in));
            logger.severe(ExceptionUtils.getStackTrace(e));
        }
        logger.info("Processing complete...");
        return results;
    }

}
