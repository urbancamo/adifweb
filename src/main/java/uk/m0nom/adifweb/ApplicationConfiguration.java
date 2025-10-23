package uk.m0nom.adifweb;

import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import uk.m0nom.adifproc.activity.ActivityDatabaseService;
import uk.m0nom.adifproc.adif3.Adif3Transformer;
import uk.m0nom.adifproc.adif3.io.Adif3FileReader;
import uk.m0nom.adifproc.adif3.io.Adif3FileWriter;
import uk.m0nom.adifproc.adif3.label.Adif3LabelFormatter;
import uk.m0nom.adifproc.adif3.print.Adif3PrintFormatter;
import uk.m0nom.adifproc.antenna.AntennaService;
import uk.m0nom.adifproc.dxcc.*;
import uk.m0nom.adifproc.kml.KmlWriter;
import uk.m0nom.adifproc.qsofile.QsoFileReader;
import uk.m0nom.adifproc.qsofile.QsoFileWriter;
import uk.m0nom.adifproc.satellite.ApSatelliteService;
import uk.m0nom.adifproc.sotacsv.SotaCsvFileReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.logging.Logger;

@Configuration
@ComponentScan({"uk.m0nom.adifproc","uk.m0nom.adifweb"})
@Order(0)
@Data
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
    private Countries countries;
    private Adif3PrintFormatter printFormatter;
    private Adif3LabelFormatter labelFormatter;
    private SotaCsvFileReader sotaCsvFileReader;
    private Adif3FileReader adif3FileReader;

    private String qrzUsername;
    private String qrzPassword;

    private String awsAccessKey;
    private String awsSecretKey;

    @Value("${server.port}")
    private int serverPort;

    @Value("500")
    private int maxQsosToProcess;

    public ApplicationConfiguration(Adif3Transformer transformer,
                                    Adif3FileWriter writer,
                                    ApSatelliteService apSatelliteService,
                                    AntennaService antennaService,
                                    ActivityDatabaseService activityDatabases,
                                    Adif3PrintFormatter printFormatter,
                                    Adif3LabelFormatter labelFormatter,
                                    SotaCsvFileReader sotaCsvFileReader,
                                    Adif3FileReader adif3FileReader) {
        this.transformer = transformer;
        this.writer = writer;
        this.apSatelliteService = apSatelliteService;
        this.antennaService = antennaService;
        this.activityDatabases = activityDatabases;
        this.printFormatter = printFormatter;
        this.labelFormatter = labelFormatter;
        this.sotaCsvFileReader = sotaCsvFileReader;
        this.adif3FileReader = adif3FileReader;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        logger.info("ApplicationStartupListener#onApplicationEvent()");

        activityDatabases.loadData();
        JsonDxccEntities jsonDxccEntities = new DxccJsonReader().read();
        dxccEntities = new DxccEntities();
        try {
            dxccEntities.setup(jsonDxccEntities);
        } catch (ParseException e) {
            logger.severe(e.getMessage());
        }
        countries = new CountriesJsonReader().read();
        countries.setup();

        qrzUsername = setFromEnv("QRZ_USERNAME");
        qrzPassword = setFromEnv("QRZ_PASSWORD");

        awsAccessKey = setFromEnv("AWS_ACCESS_KEY");
        awsSecretKey = setFromEnv("AWS_SECRET_KEY");

        if (StringUtils.isEmpty(awsAccessKey)) {
            String localIpAddress = getLocalIpAddressOfThisMachine();
            if (localIpAddress != null) {
                logger.info(String.format("Access locally with: http://localhost:%s or http://%s:%s", serverPort, localIpAddress, serverPort));
            } else {
                logger.info(String.format("Access locally with: http://localhost:%s", serverPort));
            }
        }
        logger.info("Initialising complete, ready to process requests...");
    }

    private String getLocalIpAddressOfThisMachine() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            return null;
        }
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
        if (Strings.CI.equals(extension, "csv")) {
            return sotaCsvFileReader;
        }
        return adif3FileReader;
    }
}
