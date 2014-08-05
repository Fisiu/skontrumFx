package storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Storage {

	private Path path;
	private OpenOption[] options = { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND };

	public Storage(String fileName) {
		path = Paths.get(System.getProperty("user.home"), fileName + ".txt");
		System.out.println("Data file is: " + path.toAbsolutePath().toString());
	}

	/**
	 * Create regular file in filesystem
	 * 
	 * @return <code>true</code> if file was created, <code>false</code> otherwise
	 */
	public boolean createFile() {
		try {
			Files.createFile(path);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Append barcode string to file, each one in a new line.
	 * 
	 * @param barcode
	 *            String witch should be appended to the fle
	 * @return <code>true</code> if barcode was written into file, <code>false</code> otherwise
	 */
	public boolean appendBarcode(String barcode) {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options)) {
			writer.write(barcode);
			writer.newLine();
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
			return false;
		}
		return true;
	}
}
