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
import uk.m0nom.kml.KmlWriter;
import uk.m0nom.qsofile.QsoFileReader;
import uk.m0nom.qsofile.QsoFileWriter;
import uk.m0nom.sotacsv.SotaCsvFileReader;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;

@Component
@Order(0)
@Getter
@Setter
public class ApplicationConfiguration implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = Logger.getLogger(ApplicationConfiguration.class.getName());

    private Adif3Transformer transformer;
    private QsoFileReader reader;
    private QsoFileWriter writer;
    private KmlWriter kmlWriter;

    private ActivityDatabases summits;

    private Adif3PrintFormatter formatter;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("ApplicationStartupListener#onApplicationEvent()");
        transformer = new Adif3Transformer();
        writer = new Adif3FileWriter();
        summits = new ActivityDatabases();
        formatter = new Adif3PrintFormatter();
        summits.loadData();
        logger.info("Initialising complete, ready to process requests...");
    }

    public QsoFileReader getReader(String inputFile) {
        String extension = FilenameUtils.getExtension(inputFile).toLowerCase();
        if (StringUtils.equals(extension, "csv")) {
            return new SotaCsvFileReader();
        }
        return new Adif3FileReader();
    }
}
