package uk.m0nom.adifweb.file;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.m0nom.adif3.control.TransformControl;
import uk.m0nom.adifweb.ApplicationConfiguration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

@Service
public class FileService {
    private static final Logger logger = Logger.getLogger(FileService.class.getName());

    private final ApplicationConfiguration configuration;

    public FileService(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    public void storeInputFile(TransformControl control, MultipartFile uploadedFile, String tmpPath) {
        String inputPath = null;
        String inputFilename = null;
        String content = null;
        InputStream uploadedStream = null;

        try {
            uploadedStream = uploadedFile.getInputStream();
            inputFilename = String.format("%d-in-%s", control.getRunTimestamp(), uploadedFile.getOriginalFilename());
            inputPath = String.format("%s%s", tmpPath, inputFilename);
            var out = new FileOutputStream(inputPath);

            content = IOUtils.toString(uploadedStream, control.getEncoding());
            // Store the ADIF input file into the server temp directory
            IOUtils.write(content, out, control.getEncoding());
            logger.info(String.format("Wrote ADIF input file to: %s", inputPath));
        } catch (IOException ioe1) {
            logger.severe(ioe1.getMessage());
        } finally {
            try {
                assert uploadedStream != null;
                uploadedStream.close();
            } catch (IOException ioe2) {
                logger.severe(ioe2.getMessage());
            }
        }

        if (configuration.isAws()) {
            // Archive the content into S3 storage
            assert content != null;
            logger.info(String.format("Archiving %d characters into AWS S3 file %s", content.length(), inputFilename));
            configuration.getAwsS3Utils().archiveFile(inputFilename, content);
        }
    }

    public void archiveFile(TransformControl control, String filename, String tmpPath, String encoding) {
        String content;
        FileInputStream out = null;
        if (configuration.isAws()) {
            // Read content of file
            var filePath = String.format("%s%s", tmpPath, filename);
            try {
                out = new FileInputStream(filePath);
                content = IOUtils.toString(out, encoding);

                // Archive the content into S3 storage
                logger.info(String.format("Archiving output file %s", filename));
                configuration.getAwsS3Utils().archiveFile(filename, content);
            } catch (Exception e) {
                logger.severe(e.getMessage());
            } finally {
                try {
                    assert out != null;
                    out.close();
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }

        }
    }
}
