package uk.m0nom.adifweb.util;

import lombok.Getter;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

@Getter
public final class CustomFileLogHandler extends FileHandler {

    private final String logFile;

    public CustomFileLogHandler(long runTimestamp) throws IOException, SecurityException {
        super(String.format("%s/%d.log", System.getProperty("java.io.tmpdir"), runTimestamp));
        logFile = String.format("%d.log", runTimestamp);
        setFormatter(new SimpleFormatter());
        setLevel(Level.ALL);
    }

    public void closeAndDetach() {
        LoggerSetup.getRootLogger().removeHandler(this);
        super.close();
    }
}
