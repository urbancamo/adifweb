package uk.m0nom.adifweb;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.logging.Logger;

@RestController
public class DownloadController {
	private static final Logger logger = Logger.getLogger(DownloadController.class.getName());

	@GetMapping (value = "/download", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<StreamingResponseBody> download(@RequestParam String filename, final HttpServletResponse response) {

		logger.info(String.format("Download request for: %s", filename));
		response.setContentType("text/plain");
		response.setHeader(
				"Content-Disposition",
				String.format("attachment;filename=%s", filename));

		StreamingResponseBody stream = out -> {

			final String home = System.getProperty("java.io.tmpdir");
			final String fileToStreamLocation = home + File.separator + filename;
			final File fileToStream = new File(fileToStreamLocation);

			logger.info(String.format("Streaming file from: %s", fileToStreamLocation));
			final BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());

			if (fileToStream.exists() && fileToStream.isFile()) {
				try {
					final InputStream inputStream = new FileInputStream(fileToStream);
					byte[] bytes = new byte[1024];
					int length;
					while ((length = inputStream.read(bytes)) >= 0) {
						outputStream.write(bytes, 0, length);
					}

					inputStream.close();
					outputStream.close();
					logger.info(String.format("Completed streaming of %s to browser", fileToStreamLocation));
				} catch (final IOException e) {
					logger.severe(String.format("Exception while reading and streaming data %s ", e.getMessage()));
				}
			}
		};
		logger.info(String.format("steaming response %s", stream));
		return new ResponseEntity(stream, HttpStatus.OK);
	}
}
