package uk.m0nom.adifweb.domain;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.m0nom.adifproc.adif3.xsdquery.Adif3Element;
import uk.m0nom.adifproc.adif3.xsdquery.Adif3SchemaLoader;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class Adif3SchemaElementsService {
    private static final Logger logger = Logger.getLogger(Adif3SchemaElementsService.class.getName());
    private static final String ADIF3_SCHEMA_NAME = "adx312generic.xsd";

    private Set<Adif3Element> elements;

    public Adif3SchemaElementsService(ResourceLoader resourceLoader) {
        String adifSchemaFilename = String.format("classpath:adif/%s", ADIF3_SCHEMA_NAME);
        Resource adifSchema = resourceLoader.getResource(adifSchemaFilename);

        Adif3SchemaLoader loader = new Adif3SchemaLoader();
        try {
            elements = loader.loadAdif3Schema(adifSchema.getInputStream());
        } catch (IOException e) {
            logger.severe(String.format("Could not load ADIF3 Schema File: %s", ADIF3_SCHEMA_NAME));
        }
        logger.info(String.format("Loaded %d ADIF3 Schema Elements from %s", elements.size(), ADIF3_SCHEMA_NAME));
    }

    public Set<Adif3Element> getElements() {
        return elements;
    }
}
