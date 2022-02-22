package uk.m0nom.adifweb.transformer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.marsik.ham.adif.Adif3;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.adif3.Adif3Transformer;
import uk.m0nom.adif3.UnsupportedHeaderException;
import uk.m0nom.adif3.contacts.Qsos;
import uk.m0nom.adif3.control.TransformControl;
import uk.m0nom.adif3.print.Adif3PrintFormatter;
import uk.m0nom.adif3.transform.TransformResults;
import uk.m0nom.adifweb.ApplicationConfiguration;
import uk.m0nom.contest.ContestResultsCalculator;
import uk.m0nom.kml.KmlWriter;
import uk.m0nom.qrz.CachingQrzXmlService;
import uk.m0nom.qrz.QrzService;
import uk.m0nom.qsofile.QsoFileReader;
import uk.m0nom.qsofile.QsoFileWriter;

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
public class TransformerService {
    private static final Logger logger = Logger.getLogger(TransformerService.class.getName());
    private final ApplicationConfiguration configuration;
    private final Resource adifProcessorConfig;

    public TransformerService(ApplicationConfiguration configuration, Resource adifProcessorConfig) {
        this.configuration = configuration;
        this.adifProcessorConfig = adifProcessorConfig;
    }

    public TransformResults runTransformer(TransformControl control, ResourceLoader resourceLoader,
                                           String tmpPath, String inPath, String originalFilename) {
        TransformResults results = new TransformResults();
        QrzService qrzService = new CachingQrzXmlService(control.getQrzUsername(), control.getQrzPassword());
        KmlWriter kmlWriter = new KmlWriter(control);

        String adifPrinterConfigFilename = String.format("classpath:config/%s", control.getPrintConfigFile());
        Resource adifPrinterConfig = resourceLoader.getResource(adifPrinterConfigFilename);
        logger.info(String.format("Configuring print job using: %s", adifPrinterConfigFilename));


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
            if (control.isQrzDotComEnabled()) {
                qrzService.enable();
                if (!qrzService.getSessionKey()) {
                    logger.warning("Could not connect to QRZ.COM, disabling lookups and continuing...");
                    qrzService.disable();
                }
            }

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
                qsos = transformer.transform(log, control, results);
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
            logger.info(String.format("Writing QSO log file %s with encoding %s", out, control.getEncoding()));
            writer.write(out, control.getEncoding(), log);

            if (control.isMarkdown()) {
                formatter.getPrintJobConfig().configure(adifPrinterConfigFilename, adifPrinterConfig.getInputStream());
                String markdown = String.format("%s%s.%s", tmpPath, inBasename, formatter.getPrintJobConfig().getFilenameExtension());
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
                        StringBuilder sb = formatter.format(log);
                        markdownWriter = Files.newBufferedWriter(formattedQsoFile.toPath(), Charset.forName(formatter.getPrintJobConfig().getOutEncoding()), StandardOpenOption.WRITE);
                        markdownWriter.write(sb.toString());

                        results.setAdiFile(FilenameUtils.getName(out));
                        results.setKmlFile(FilenameUtils.getName(kml));
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
