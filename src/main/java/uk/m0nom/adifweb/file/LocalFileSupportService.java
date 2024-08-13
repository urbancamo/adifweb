package uk.m0nom.adifweb.file;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Logger;

@Service("localFileSupportService")
public class LocalFileSupportService implements FileSupportService {
    private static final Logger logger = Logger.getLogger(LocalFileSupportService.class.getName());

    @Value("${adifproc.files.path}")
    private String adifFilesPath;

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public void archiveFile(String filename, File fileToUpload) {
        Path path = Paths.get(adifFilesPath, ARCHIVE_FILE_PATH, filename);
        try {
            Files.copy(fileToUpload.toPath(), path);
        } catch (IOException e) {
            logger.severe(String.format("Error archiving file %s into path %s: %s", filename, path, e.getMessage()));
        }
    }

    @Override
    public void archiveData(String infile, String content) {
        Path path = Paths.get(adifFilesPath, ARCHIVE_FILE_PATH, infile);
        try {
            Files.writeString(path, content);
        } catch (IOException e) {
            logger.severe(String.format("Error archiving file %s into path %s: %s", infile, path, e.getMessage()));
        }
    }

    @Override
    public Set<String> getFiles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
