package uk.m0nom.adifweb.domain;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.m0nom.adif3.print.PrintJobConfig;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class PrintJobConfigs {
    private static final Logger logger = Logger.getLogger(PrintJobConfigs.class.getName());

    private List<PrintJobConfig> configs;
    private final ResourceLoader resourceLoader;

    public PrintJobConfigs(ResourceLoader resourceLoader) {
        configs = new ArrayList<>(10);
        // This is the order that the config files will appear in the options list
        this.resourceLoader = resourceLoader;
        addConfig("adif-printer-sota-md.yaml");
        addConfig("adif-printer-132-utf8.yaml");
        addConfig("adif-printer-132-md.yaml");
        addConfig("adif-printer-132-ukac.yaml");
        addConfig("adif-printer-158.yaml");
        addConfig("adif-printer-158-utf8.yaml");
        addConfig("adif-printer-196.yaml");
        addConfig("adif-printer-196-utf8.yaml");

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
