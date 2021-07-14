package uk.m0nom.adifweb.startup;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.adif3.Adif3FileReaderWriter;
import uk.m0nom.adif3.Adif3Transformer;
import uk.m0nom.adif3.print.Adif3PrintFormatter;
import uk.m0nom.kml.KmlWriter;

import java.util.logging.Logger;

@Component
@Order(0)
@Getter
@Setter
public class ApplicationConfiguration implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = Logger.getLogger(ApplicationConfiguration.class.getName());

    private Adif3Transformer transformer;
    private Adif3FileReaderWriter readerWriter;
    private KmlWriter kmlWriter;

    private ActivityDatabases summits;

    private Adif3PrintFormatter formatter;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("ApplicationStartupListener#onApplicationEvent()");
        transformer = new Adif3Transformer();
        readerWriter = new Adif3FileReaderWriter();
        summits = new ActivityDatabases();
        formatter = new Adif3PrintFormatter();
        summits.loadData();
    }
}
