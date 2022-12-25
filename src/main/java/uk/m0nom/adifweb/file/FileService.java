package uk.m0nom.adifweb.file;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.m0nom.adifproc.adif3.control.TransformControl;
import uk.m0nom.adifweb.domain.HtmlParameter;
import uk.m0nom.adifweb.domain.HtmlParameters;

import java.io.*;
import java.util.logging.Logger;

@Service
public class FileService {
    private static final Logger logger = Logger.getLogger(FileService.class.getName());

    private final AwsS3Utils awsS3Utils;

    public FileService(AwsS3Utils awsS3Utils) {
        this.awsS3Utils = awsS3Utils;
    }

    public void storeInputFile(TransformControl control, MultipartFile uploadedFile, String tmpPath) {
        String inputFilename = String.format("%d-in-%s", control.getRunTimestamp(), uploadedFile.getOriginalFilename());
        String inputPath = String.format("%s%s", tmpPath, inputFilename);

        try (var out = new FileOutputStream(inputPath);
                var uploadedStream = uploadedFile.getInputStream()) {
            uploadedStream.transferTo(out);
            logger.info(String.format("Wrote ADIF input file to: %s", inputPath));
        } catch (IOException ioe1) {
            logger.severe(ioe1.getMessage());
        }

        if (awsS3Utils.isConfigured()) {
            // Archive the content into S3 storage
            logger.info(String.format("Archiving input file to AWS S3 file %s", inputFilename));
            awsS3Utils.archiveFile(inputFilename, new File(inputPath));
        }
    }

    public void archiveParameters(TransformControl control,  HtmlParameters parameters) {
        var sb = new StringBuilder();
        for (HtmlParameter parameter : parameters.values()) {
            sb.append(String.format("%s: %s\n", parameter.getKey(), parameter.getValue()));
        }
        String file = String.format("%d-in-%s.%s", control.getRunTimestamp(), "parameters", "txt");
        archiveData(file, sb.toString());
    }

    public void archiveData(String filename, String content) {
        // Read content of file
        awsS3Utils.archiveData(filename, content);
    }

    public void archiveFile(String filename, String tmpPath) {
        if (awsS3Utils.isConfigured()) {
            // Read content of file
            var filePath = String.format("%s%s", tmpPath, filename);
            // Archive the content into S3 storage
            logger.info(String.format("Archiving output file into S3: %s", filename));
            awsS3Utils.archiveFile(filename, new File(filePath));
        }
    }
}
