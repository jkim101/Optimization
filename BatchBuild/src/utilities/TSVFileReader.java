package utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class TSVFileReader implements Iterable<String[]>, Iterator<String[]> {
	private int columns;
	private InputStream stream;
	private static final int MAX_COL = Short.MAX_VALUE;
	private final byte[] colData = new byte[MAX_COL];
	private String fileName;
	private String[] headers;
	private boolean failOnMissing = true;

	public static TSVFileReader getReader(final String fileName, final int columns) {
		final TSVFileReader reader = new TSVFileReader(fileName);

		if (reader.stream == null) {
			throw new GSimsBaseRuntimeException("File not found: " + fileName);
		}
		reader.columns = columns;
		return reader;
	}

//	public TSVFileReader(final Path filePath) {
//		this(filePath.getInputStream());
//	}

	public TSVFileReader(final InputStream source) {
		stream = source;
		if (stream != null) {
			stream = new BufferedInputStream(stream);
			readHeaders();
		}
	}

	public TSVFileReader(final String fileName) {
		this(getFileStream(fileName));
		this.fileName = fileName;
	}

	public TSVFileReader(final String fileName, final int columns) {
		this(getFileStream(fileName), columns);
		this.fileName = fileName;
	}

//	public TSVFileReader(final Path filePath, final int columns) {
//		this(filePath.getInputStream(), columns);
//	}

//	public TSVFileReader(final Path filePath, final int columns, final boolean failOnMissing) {
//		this(failOnMissing || filePath.exists() ? filePath.getInputStream() : null, columns);
//		this.failOnMissing = failOnMissing;
//	}

	public TSVFileReader(final InputStream inputStream, final int columns) {
		this.columns = columns;
		if (inputStream != null) {
			try {
				if (inputStream.markSupported()) {
					inputStream.reset();
				}
			} catch (final IOException e) {
				throw new GSimsBaseRuntimeException(e);
			}
			stream = new BufferedInputStream(inputStream);
			skipHeader();
		}
	}

	private void skipHeader() {
		int data;
		try {
			data = stream.read();
			while (data != -1 && data != '\n') {
				data = stream.read();
			}
		} catch (final IOException e) {
			throw new GSimsBaseRuntimeException(e);
		}
	}

	private void readHeaders() {
		final LinkedList<String> list = new LinkedList<String>();
		final StringBuilder curCol = new StringBuilder();
		int data;
		try {
			data = stream.read();
			do {
				if (data == '\t') {
					list.add(curCol.toString());
					curCol.setLength(0);
				} else if (data != '\r') { // For windows files
					curCol.append((char) data);
				}
				data = stream.read();
			} while (data != -1 && data != '\n');
		} catch (final IOException e) {
			throw new GSimsBaseRuntimeException("Unable to parse file: " + fileName, e);
		}
		list.add(curCol.toString());
		curCol.setLength(0);

		headers = list.toArray(new String[list.size()]);
		columns = headers.length;
	}

	public String[] getHeaders() {
		return headers;
	}

	private static InputStream getFileStream(final String newFileName) {
		final File file = new File(newFileName);
		FileInputStream fileStream;
		try {
			fileStream = new FileInputStream(file);
		} catch (final FileNotFoundException e) {
			return null;
		}
		return fileStream;
	}

	public boolean exists() {
		return stream != null;
	}

	public String[] readLine() {
		try {
			int data = stream.read();
			if (data == -1) {
				close();
				return null;
			} else {
				final String[] line = new String[columns];
				int index = 0;
				int dataIndex = 0;
				do {
					if (index != columns) { // ignore columns not asked for
						if (data == '\t') {
							line[index] = parse(dataIndex);
							dataIndex = 0;
							index++;
						} else if (data != '\r') { // For windows files
							colData[dataIndex++] = (byte) data;
						}
					}
					data = stream.read();
				} while (!(data == '\n' || data == -1));
				// With proper data, this should always run once. It will fill in
				// empty columns as well.
				while (index < columns) {
					line[index] = parse(dataIndex);
					dataIndex = 0;
					index++;
				}
				return line;
			}
		} catch (final NullPointerException e) {
			throw new GSimsBaseRuntimeException("File not found:" + fileName);
		} catch (final IOException e) {
			throw new GSimsBaseRuntimeException(e);
		}
	}

	private String parse(final int dataIndex) {
		try {
			return new String(colData, 0, dataIndex, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new GSimsBaseRuntimeException(e);
		}
	}

	public void close() {
		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException e) {
				throw new GSimsBaseRuntimeException(e);
			}
			stream = null;
		}
	}

	String[] next;

	@Override
	public Iterator<String[]> iterator() {
		try {
			if (failOnMissing || exists()) {
				next = readLine();
			} else {
				next = null;
			}
			return this;
		} catch (final Exception e) {
			throw new GSimsBaseRuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String[] next() {
		if (next == null) {
			throw new NoSuchElementException("readLine() past end of file.");
		}
		final String[] current = next;
		next = readLine();
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
