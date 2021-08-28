package uk.m0nom.adifweb;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import uk.m0nom.adif3.args.TransformControl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class WriteYamlTest {

    @Test
    public void testSerialization() {
        TransformControl primary = new TransformControl();

        Yaml yamlProcessor = new Yaml();
        String yaml = yamlProcessor.dump(primary);

        try {
            FileUtils.write(new File("./target/TransformControl.yml"), yaml, StandardCharsets.UTF_8);
            yaml = FileUtils.readFileToString(new File("./target/TransformControl.yml"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            assertTrue(false);
        }

        TransformControl primaryCopy = yamlProcessor.loadAs(yaml, TransformControl.class);

        assertFalse(primary == primaryCopy);
        assertEquals(primary, primaryCopy);
    }
}
