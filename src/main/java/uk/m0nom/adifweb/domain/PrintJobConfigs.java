package uk.m0nom.adifweb.domain;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.m0nom.adifproc.adif3.print.PrintJobConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PrintJobConfigs {
    private static final Logger logger = Logger.getLogger(PrintJobConfigs.class.getName());

    private final List<PrintJobConfig> configs;
    private final ResourceLoader resourceLoader;

    public PrintJobConfigs(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        configs = new ArrayList<>(10);
        // This is the order that the config files will appear in the options list
        addConfig("adif-printer-sota-md.yaml");
        addConfig("adif-printer-132-utf8.yaml");
        addConfig("adif-printer-132-md.yaml");
        addConfig("adif-printer-132-ukac.yaml");
        addConfig("adif-printer-158.yaml");
        addConfig("adif-printer-158-utf8.yaml");
        addConfig("adif-printer-medium-md.yaml");
        addConfig("adif-printer-196.yaml");
        addConfig("adif-printer-196-utf8.yaml");
        addConfig("adif-printer-wide-md.yaml");
        addConfig("adif-printer-a4-24-63.5mmx33.9mm.yaml");
    }

    private void addConfig(String filename) {
        String adifPrinterConfigFilename = String.format("classpath:config/%s", filename);
        Resource adifPrinterConfig = resourceLoader.getResource(adifPrinterConfigFilename);
        logger.info(String.format("Configuring print job using: %s", adifPrinterConfigFilename));

        PrintJobConfig config = new PrintJobConfig();
        try {
            config.configure(filename, adifPrinterConfig.getInputStream());
            configs.add(config);
        } catch (IOException e) {
            logger.severe(String.format("Print Job Configuration file: %s could not be accessed as a resource", filename));
        }

    }

    public List<PrintJobConfig> getConfigs() {
        return configs;
    }
}
