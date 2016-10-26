import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * OFile
 * Opens file to read/write efficiently and intuitively.
 * @author Ofek Gila
 * @since August 2016
 */
public class OFile extends File {

	private BufferedWriter bufferedWriter;
	private BufferedReader bufferedReader;
	private FileWriter fileWriter;
	private boolean appending;
	private boolean writerOpen, readerOpen;

	/**
	 * Constructor with defined path to file.
	 * Creates the file if doesn't exist.
	 * @param  path string path to file
	 */
	public OFile(String path) {
		super(path);
		appending = false;
		writerOpen = readerOpen = false;
		if (!exists())
			try {
				createNewFile();
			}	catch (IOException e) {
				getParentFile().mkdirs();
				try {
					createNewFile();
				}	catch (IOException e2) {}
			}
	}

	/**
	 * Creates OFile given a {@link File} instance.
	 * @param  file file to create OFile from
	 */
	public OFile(File file) {
		this(file.getPath());
	}

	/**
	 * Writes string to file
	 * @param  str string to write
	 * @return     OFile instance
	 */
	public OFile write(String str) {
		if (!writerOpen)
			openWriter();
		try {
			bufferedWriter.write(str);
			return this;
		}	catch (IOException e) {}
		return null;
	}

	/**
	 * Writes string to file with specific appending mode
	 * (remembers appending mode in subsequent calls).
	 * @param  str       string to write
	 * @param  appending appending mode (true to append)
	 * @return           OFile instance
	 */
	public OFile write(String str, boolean appending) {
		if (appending != this.appending) {
			this.appending = appending;
			if (writerOpen)
				closeWriter();
			openWriter();
		}
		return write(str);
	}

	/**
	 * Reads a single line from file as string.
	 * @return the line
	 */
	public String read() {
		if (!readerOpen)
			openReader();
		try {
			return bufferedReader.readLine();
		}	catch (IOException e) {}
		return null;
	}

	/**
	 * Reads whole file super-efficiently as string.
	 * @return the whole file as string
	 */
	public String readFile() {
		close();
		try {
			FileChannel channel = new FileInputStream(this).getChannel();
			ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());
			channel.read(buffer);
			channel.close();
			return new String(buffer.array());
		}	catch (FileNotFoundException e) {}
			catch (IOException e) {}
		return null;
	}

	/**
	 * Creates and opens {@link BufferedWriter} instance,
	 * closing {@link BufferedReader} instance if necessary.
	 * @return OFile instance
	 */
	public OFile openWriter() {
		if (readerOpen)
			closeReader();
		try {
			fileWriter = new FileWriter(this, appending);
			bufferedWriter = new BufferedWriter(fileWriter);
			writerOpen = true;
		}	catch (IOException e) {}
		return writerOpen ? this:null;
	}

	/**
	 * Creates and opens {@link BufferedReader} instance,
	 * closing {@link BufferedWriter} instance if necessary.
	 * @return OFile instance
	 */
	public OFile openReader() {
		if (writerOpen)
			closeWriter();
		try {
			bufferedReader = new BufferedReader(new FileReader(this));
			readerOpen = true;
		}	catch (IOException e) {}
		return readerOpen ? this:null;
	}

	/**
	 * Closes {@link BufferedWriter} instance.
	 * @return OFile instance
	 */
	public OFile closeWriter() {
		try {
			bufferedWriter.close();
			fileWriter.close();
			writerOpen = false;
			return this;
		}	catch (IOException e) {}
		return null;
	}

	/**
	 * Closes {@link BufferedReader} instance.
	 * @return OFile instance
	 */
	public OFile closeReader() {
		try {
			bufferedReader.close();
			readerOpen = false;
			return this;
		}	catch (IOException e) {}
		return null;
	}

	/**
	 * Closes {@link BufferedReader} instance or
	 * {@link BufferedWriter} instance if open.
	 * @return OFile instance
	 */
	public OFile close() {
		if (writerOpen && closeWriter() == null)
			return null;
		if (readerOpen && closeReader() == null)
			return null;
		return this;
	}

	/**
	 * Clears contents of file.
	 * @return OFile instance
	 */
	public OFile clear() {
		if (close() == null || write("", false) == null || close() == null)
			return null;
		return this;
	}

	/**
	 * Gets {@link BufferedWriter} object for file
	 * @return {@link BufferedWriter} object
	 */
	public BufferedWriter getWriter() {
		if (!writerOpen)
			openWriter();
		return bufferedWriter;
	}

	/**
	 * Gets {@link BufferedReader} object for file
	 * @return {@link BufferedReader} object
	 */
	public BufferedReader getReader() {
		if (!readerOpen)
			openReader();
		return bufferedReader;
	}

	/* And now for File -> OFile overrides */

	@Override
	public OFile getAbsoluteFile() {
		return convertFile(super.getAbsoluteFile());
	}

	@Override
	public OFile getCanonicalFile() {
		try {
			return convertFile(super.getCanonicalFile());
		} catch (IOException e) {}
		return null;
	}

	@Override
	public File getParentFile() {
		return convertFile(super.getParentFile());
	}

	@Override
	public OFile[] listFiles() {
		return convertFiles(super.listFiles());
	}

	@Override
	public OFile[] listFiles(FileFilter filter) {
		return convertFiles(super.listFiles(filter));
	}

	@Override
	public OFile[] listFiles(FilenameFilter filter) {
		return convertFiles(super.listFiles(filter));
	}

	private static OFile convertFile(File file) {
		return new OFile(file);
	}

	private static OFile[] convertFiles(File[] files) {
		int numFiles = files.length;
		OFile[] ofiles = new OFile[numFiles];
		for (int i = 0; i < numFiles; i++)
			ofiles[i] = new OFile(files[i]);
		return ofiles;
	}
}