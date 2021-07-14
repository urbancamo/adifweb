package uk.m0nom.adifweb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
@RequestMapping("/api")
public class DownloadController {

	private final Logger logger = LoggerFactory.getLogger(DownloadController.class);

	@GetMapping (value = "/download", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<StreamingResponseBody> download(@RequestParam String filename, final HttpServletResponse response) {

		response.setContentType("text/plain");
		response.setHeader(
				"Content-Disposition",
				String.format("attachment;filename=%s", filename));

		StreamingResponseBody stream = out -> {

			final String home = System.getProperty("java.io.tmpdir");
			final File fileToStream = new File(home + File.separator + filename);
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
				} catch (final IOException e) {
					logger.error("Exception while reading and streaming data {} ", e);
				}
			}
		};
		logger.info("steaming response {} ", stream);
		return new ResponseEntity(stream, HttpStatus.OK);
	}
}
