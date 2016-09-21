import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.NoSuchElementException;

import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

/**
 * OFile
 * Opens file to read/write efficiently.
 * @author Ofek Gila
 * @since August 2016
 * @version September 20th, 2016
 */
public class OFile extends File {

	private BufferedWriter bufferedWriter;
	private BufferedReader bufferedReader;
	private FileWriter fileWriter;
	private boolean appending;
	private boolean writerOpen, readerOpen;

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

	public OFile(OFile file) {
		this(file.getPath());
	}

	public OFile write(String str) {
		if (!writerOpen)
			openWriter();
		try {
			bufferedWriter.write(str);
			return this;
		}	catch (IOException e) {}
		return null;
	}

	public OFile write(String str, boolean appending) {
		if (appending != this.appending) {
			this.appending = appending;
			if (writerOpen)
				closeWriter();
			openWriter();
		}
		return write(str);
	}

	public String read() {
		if (!readerOpen)
			openReader();
		try {
			return bufferedReader.readLine();
		}	catch (IOException e) {}
		return null;
	}

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

	public OFile openReader() {
		if (writerOpen)
			closeWriter();
		try {
			bufferedReader = new BufferedReader(new FileReader(this));
			readerOpen = true;
		}	catch (IOException e) {}
		return readerOpen ? this:null;
	}

	public OFile closeWriter() {
		try {
			bufferedWriter.close();
			fileWriter.close();
			writerOpen = false;
			return this;
		}	catch (IOException e) {}
		return null;
	}

	public OFile closeReader() {
		try {
			bufferedReader.close();
			readerOpen = false;
			return this;
		}	catch (IOException e) {}
		return null;
	}

	public OFile close() {
		if (writerOpen && closeWriter() == null)
			return null;
		if (readerOpen && closeReader() == null)
			return null;
		return this;
	}

	public OFile clear() {
		if (close() == null || write("", false) == null || close() == null)
			return null;
		return this;
	}

	public BufferedWriter getWriter() {
		if (!writerOpen)
			openWriter();
		return bufferedWriter;
	}

	public BufferedReader getReader() {
		if (!readerOpen)
			openReader();
		return bufferedReader;
	}
}