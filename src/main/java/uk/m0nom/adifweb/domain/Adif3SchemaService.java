package uk.m0nom.adifweb.domain;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.m0nom.adifproc.adif3.xsdquery.Adif3Field;
import uk.m0nom.adifproc.adif3.xsdquery.Adif3Schema;
import uk.m0nom.adifproc.adif3.xsdquery.Adif3SchemaLoader;
import uk.m0nom.adifproc.adif3.xsdquery.Adif3Type;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class Adif3SchemaService {
    private static final Logger logger = Logger.getLogger(Adif3SchemaService.class.getName());
    private static final String ADIF3_SCHEMA_NAME = "adx312generic.xsd";

    private Adif3Schema schema;

    public Adif3SchemaService(ResourceLoader resourceLoader) {
        String adifSchemaFilename = String.format("classpath:adif/%s", ADIF3_SCHEMA_NAME);
        Resource adifSchema = resourceLoader.getResource(adifSchemaFilename);

        Adif3SchemaLoader loader = new Adif3SchemaLoader();
        try {
            schema = loader.loadAdif3Schema(adifSchema.getInputStream());
        } catch (IOException e) {
            logger.severe(String.format("Could not load ADIF3 Schema File: %s", ADIF3_SCHEMA_NAME));
        }
        logger.info(String.format("Loaded %d ADIF3 Schema Elements from %s", schema.getFields().size(), ADIF3_SCHEMA_NAME));
    }

    public Adif3Schema getSchema() { return schema; }
}
