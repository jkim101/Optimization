package utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class FileWriter {
	private String fileName;
	private String fileDir;
	private String fileFullPath;
	private PrintStream ps;
	private BufferedOutputStream bufferedOutputStream;
	private static final char SLASH = '/';

	public FileWriter() {
	}

	public FileWriter(final String fileFullPath, final boolean append) {
		setFileFullPath(fileFullPath);
		open(append);
	}

	public FileWriter setFileName(final String fileName) {
		this.fileName = fileName;
		if (fileDir != null) {
			fileFullPath = fileDir + SLASH + fileName;
		}
		return this;
	}

	public FileWriter setFileDir(final String fileDir) {
		this.fileDir = fileDir;
		if (fileName != null) {
			setFileFullPath(fileDir + SLASH + fileName);
		}
		return this;
	}

	public FileWriter setFileFullPath(final String fileFullPath) {
		this.fileFullPath = fileFullPath;
		return this;
	}

	public FileWriter open() {
		return open(false);
	}

	public FileWriter append() {
		return open(true);
	}

	private FileWriter open(final boolean append) {
		if (fileFullPath != null) {
			try {
				// Make all the directories in the full path
				final File file = new File(fileFullPath);
				final File parent = file.getParentFile();
				if (!parent.mkdirs() && !parent.exists()) {
					throw new GSimsBaseRuntimeException("Failed to create directory structure for: " + fileFullPath);
				}
				// create a new print stream
				bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file, append));
				ps = new PrintStream(bufferedOutputStream, false, "UTF8");
			} catch (final FileNotFoundException e) {
				throw new GSimsBaseRuntimeException("fileFullPath[" + fileFullPath + "] not found!", e);
			} catch (final UnsupportedEncodingException e) {
				throw new GSimsBaseRuntimeException(e);
			}
		}
		return this;
	}

	public void write(final String line) {
		ps.print(line);
	}

	public void write(final char c) {
		ps.print(c);
	}

	public void write(final byte[] bytes) {
		try {
			ps.write(bytes);
		} catch (final IOException e) {
			throw new GSimsBaseRuntimeException("Error writing to fileFullPath[" + fileFullPath + "]!", e);
		}
	}

	public void writeAll(final List<String> lstData) {
		//try {
		for (final String line : lstData) {
			ps.println(line);
		}
		//		} catch (final Exception e) {
		//			throw new GSimsBaseRuntimeException("Error writing to fileFullPath[" + fileFullPath + "]!", e);
		//		} finally {
		//			if (ps != null) {
		//				try {
		//					bufferedOutputStream.flush();
		//					bufferedOutputStream.close();
		//					ps.close();
		//				} catch (final Exception e) {
		//					throw new GSimsBaseRuntimeException("Error while closing ps in finally of FileWriter", e);
		//				} finally {
		//					ps = null;
		//				}
		//			}// if ps
		//		}
	}

	public void close() {
		if (ps != null) {
			try {
				bufferedOutputStream.flush();
				bufferedOutputStream.close();
				ps.close();
			} catch (final IOException e) {
				throw new GSimsBaseRuntimeException("Error while closing ps in close() of FileWriter", e);
			} finally {
				ps = null;
			}
		}// if ps
	}

	public void write(final double dbl) {
		ps.print(dbl);
	}

	public void write(final int val) {
		ps.print(val);
	}
}