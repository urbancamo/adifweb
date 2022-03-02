package uk.m0nom.adifweb;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.adif3.Adif3FileReader;
import uk.m0nom.adif3.Adif3FileWriter;
import uk.m0nom.adif3.Adif3Transformer;
import uk.m0nom.adif3.print.Adif3PrintFormatter;
import uk.m0nom.antenna.Antennas;
import uk.m0nom.dxcc.DxccEntities;
import uk.m0nom.dxcc.DxccJsonReader;
import uk.m0nom.kml.KmlWriter;
import uk.m0nom.qsofile.QsoFileReader;
import uk.m0nom.qsofile.QsoFileWriter;
import uk.m0nom.satellite.ApSatellites;
import uk.m0nom.sotacsv.SotaCsvFileReader;

import java.util.logging.Logger;

@Component
@Order(0)
@Getter
@Setter
public class ApplicationConfiguration implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = Logger.getLogger(ApplicationConfiguration.class.getName());

    private Adif3Transformer transformer = new Adif3Transformer();
    private QsoFileReader reader;
    private QsoFileWriter writer = new Adif3FileWriter();
    private KmlWriter kmlWriter;
    private ApSatellites apSatellites = new ApSatellites();
    private Antennas antennas = new Antennas();
    private ActivityDatabases activityDatabases = new ActivityDatabases();
    private DxccEntities dxccEntities = null;
    private Adif3PrintFormatter formatter = new Adif3PrintFormatter();

    private String qrzUsername;
    private String qrzPassword;

    private String awsAccessKey;
    private String awsSecretKey;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
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
            return new SotaCsvFileReader();
        }
        return new Adif3FileReader();
    }
}
