package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Storage {

	private Path path;
	private OpenOption[] options = { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND };

	public Storage(String fileName) {
		path = Paths.get(System.getProperty("user.home"), fileName + ".txt");
		System.out.println("Data file is: " + path.toAbsolutePath().toString());
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
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
			System.err.format("IOException: %s%n", e);
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
		if (Files.notExists(path)) {
			createFile();
		}
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options)) {
			writer.write(barcode);
			writer.newLine();
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
			return false;
		}
		return true;
	}

	/**
	 * Deletes specific barcode from file.
	 * 
	 * @param barcodeLine
	 *            Barcode's line number which should be deleted from file.
	 * @return <code>true</code> when barcode was successfully deleted, <code>false</code> otherwise.
	 */
	public boolean deleteBarcode(int barcodeLine) {
		Path tmpPath = null;
		try {
			// create tmp file
			tmpPath = Files.createFile(Paths.get(path.toString() + ".tmp"));

			// read from path, write to tmpPath
			try (BufferedReader reader = Files.newBufferedReader(path);
					BufferedWriter writer = Files.newBufferedWriter(tmpPath, options);) {

				String line = null;
				int lineNo = 0;
				while ((line = reader.readLine()) != null) {
					if (lineNo != barcodeLine) {
						writer.write(line);
						writer.newLine();
					}
					lineNo++;
				}
			}
			// replace original file with a new one
			Files.move(tmpPath, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
			return false;
		}
		return true;
	}

	public List<String> openFile(File file) {
		List<String> list = new ArrayList<String>();
		if (!file.isDirectory() && file.canWrite()) {
			path = Paths.get(file.getAbsolutePath());

			try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					list.add(line);
				}
			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		return list;
	}
}
