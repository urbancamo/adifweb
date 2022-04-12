package uk.m0nom.adifweb;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import uk.m0nom.adifproc.activity.ActivityDatabaseService;
import uk.m0nom.adifproc.adif3.Adif3Transformer;
import uk.m0nom.adifproc.adif3.io.Adif3FileReader;
import uk.m0nom.adifproc.adif3.io.Adif3FileWriter;
import uk.m0nom.adifproc.adif3.print.Adif3PrintFormatter;
import uk.m0nom.adifproc.antenna.AntennaService;
import uk.m0nom.adifproc.dxcc.DxccEntities;
import uk.m0nom.adifproc.dxcc.DxccJsonReader;
import uk.m0nom.adifproc.kml.KmlWriter;
import uk.m0nom.adifproc.qsofile.QsoFileReader;
import uk.m0nom.adifproc.qsofile.QsoFileWriter;
import uk.m0nom.adifproc.satellite.ApSatelliteService;
import uk.m0nom.adifproc.sotacsv.SotaCsvFileReader;

import java.util.logging.Logger;

@Configuration
@ComponentScan({"uk.m0nom.adifproc","uk.m0nom.adifweb"})
@Order(0)
@Getter
@Setter
public class ApplicationConfiguration implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = Logger.getLogger(ApplicationConfiguration.class.getName());

    private Adif3Transformer transformer;
    private QsoFileReader reader;
    private QsoFileWriter writer;
    private KmlWriter kmlWriter;
    private ApSatelliteService apSatelliteService;
    private AntennaService antennaService;
    private ActivityDatabaseService activityDatabases;
    private DxccEntities dxccEntities = null;
    private Adif3PrintFormatter formatter;
    private SotaCsvFileReader sotaCsvFileReader;
    private Adif3FileReader adif3FileReader;

    private String qrzUsername;
    private String qrzPassword;

    private String awsAccessKey;
    private String awsSecretKey;

    public ApplicationConfiguration(Adif3Transformer transformer,
                                    Adif3FileWriter writer,
                                    ApSatelliteService apSatelliteService,
                                    AntennaService antennaService,
                                    ActivityDatabaseService activityDatabases,
                                    Adif3PrintFormatter formatter,
                                    SotaCsvFileReader sotaCsvFileReader,
                                    Adif3FileReader adif3FileReader) {
        this.transformer = transformer;
        this.writer = writer;
        this.apSatelliteService = apSatelliteService;
        this.antennaService = antennaService;
        this.activityDatabases = activityDatabases;
        this.formatter = formatter;
        this.sotaCsvFileReader = sotaCsvFileReader;
        this.adif3FileReader = adif3FileReader;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        logger.info("ApplicationStartupListener#onApplicationEvent()");

        activityDatabases.loadData();
        dxccEntities = new DxccJsonReader().read();

        qrzUsername = setFromEnv("QRZ_USERNAME");
        qrzPassword = setFromEnv("QRZ_PASSWORD");

        awsAccessKey = setFromEnv("AWS_ACCESS_KEY");
        awsSecretKey = setFromEnv("AWS_SECRET_KEY");

        logger.info("Initialising complete, ready to process requests...");
    }

    private String setFromEnv(String envVar) {
        String value = System.getenv(envVar);
        if (StringUtils.isNotEmpty(value)) {
            logger.info(String.format("%s set from environment", envVar));
        }
        return value;
    }

    public QsoFileReader getReader(String inputFile) {
        String extension = FilenameUtils.getExtension(inputFile).toLowerCase();
        if (StringUtils.equals(extension, "csv")) {
            return sotaCsvFileReader;
        }
        return adif3FileReader;
    }
}
