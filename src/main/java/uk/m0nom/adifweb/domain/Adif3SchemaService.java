package uk.m0nom.adifweb.domain;

import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.m0nom.adifproc.adif3.xsdquery.Adif3Schema;
import uk.m0nom.adifproc.adif3.xsdquery.Adif3SchemaLoader;

import java.io.IOException;
import java.util.logging.Logger;

@Getter
@Service
public class Adif3SchemaService {
    private static final Logger logger = Logger.getLogger(Adif3SchemaService.class.getName());
    private static final String ADIF3_SCHEMA_NAME = "adx316generic.xsd";

    private Adif3Schema schema;
    private final Adif3SchemaLoader loader;

    public Adif3SchemaService(ResourceLoader resourceLoader, Adif3SchemaLoader loader) {
        this.loader = loader;
        String adifSchemaFilename = String.format("classpath:adif/%s", ADIF3_SCHEMA_NAME);
        Resource adifSchema = resourceLoader.getResource(adifSchemaFilename);

        try {
            schema = loader.loadAdif3Schema(adifSchema.getInputStream());
            logger.info(String.format("Loaded %d ADIF3 Schema Elements from %s", schema.getFields().size(), ADIF3_SCHEMA_NAME));
        } catch (IOException e) {
            logger.severe(String.format("Could not load ADIF3 Schema File: %s", ADIF3_SCHEMA_NAME));
        }
    }

}
