package uk.m0nom.adifweb.file;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

@Service("awsS3FileSupportService")
@Getter
public class AwsS3FileSupportService implements FileSupportService {
    private AmazonS3 s3client = null;
    private final static String ADIF_PROC_BUCKET = "adif-processor";
    private final boolean configured;
    private static final Logger logger = Logger.getLogger(AwsS3FileSupportService.class.getName());

    public AwsS3FileSupportService(Environment env) {
        String accessKey = env.getProperty("AWS_ACCESS_KEY");
        String secretKey = env.getProperty("AWS_SECRET_KEY");

        configured = StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey);
        if (configured) {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.EU_WEST_2).build();
        }
    }

    @Override
    public boolean isConfigured() { return configured; }

    @Override
    public void archiveFile(String filename, File fileToUpload) {
        if (isConfigured()) {
            String path = String.format("%s/%s", ARCHIVE_FILE_PATH, filename);
            try {
                s3client.putObject(ADIF_PROC_BUCKET, path, fileToUpload);
            } catch (Exception e) {
                logger.severe(String.format("Exception archiving file %s into bucket %s: %s", path, ADIF_PROC_BUCKET, e.getMessage()));}
        }
    }

    @Override
    public void archiveData(String infile, String content) {
        if (isConfigured()) {
            String path = String.format("%s/%s", ARCHIVE_FILE_PATH, infile);
            try {
                s3client.putObject(ADIF_PROC_BUCKET, path, content);
            } catch (Exception e) {
                logger.severe(String.format("Exception archiving file %s into bucket %s: %s", path, ADIF_PROC_BUCKET, e.getMessage()));
            }
        }
    }

    @Override
    public Set<String> getFiles() {
        if (isConfigured()) {
            Set<String> inputFiles = new TreeSet<>();
            ObjectListing objectListing = s3client.listObjects(ADIF_PROC_BUCKET, ARCHIVE_FILE_PATH);
            for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
                inputFiles.add(os.getKey());
            }
            return inputFiles;
        }
        return null;
    }

}
