package uk.m0nom.adifweb.file;

import java.io.File;
import java.util.Set;

public interface FileSupportService {
    String ARCHIVE_FILE_PATH = "archive";

    boolean isConfigured();

    void archiveFile(String filename, File fileToUpload);

    void archiveData(String infile, String content);

    Set<String> getFiles();
}
