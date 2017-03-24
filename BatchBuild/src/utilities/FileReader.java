package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;



/***
 * @author CHARISH
 ***/
public class FileReader {
	private String fileName;
	
	private String fileDir;
	
	private String fileFullPath;
	
	private FileInputStream fInput;
	
	private FileChannel fChan;
	
	private BufferedReader reader;
	
	private static final String SLASH = "/";
	
	public FileReader() {
	}
	
	/**
	 * @param fileName
	 * @return
	 */
	public FileReader setFileName(String fileName) {
		this.fileName = fileName;
		if (this.fileDir != null)
			fileFullPath = fileDir + SLASH + fileName;
		return this;
	}

	/**
	 * @param fileDir
	 * @return
	 */
	public FileReader setFileDir(String fileDir) {
		this.fileDir = fileDir;
		if (this.fileName != null) {
			setFileFullPath(fileDir + SLASH + fileName);
		}
		return this;
	}
	
	/**
	 * @param fileFullPath
	 * @return
	 */
	public FileReader setFileFullPath(String fileFullPath) {
		this.fileFullPath = fileFullPath;
		return this;
	}
	
	/**
	 * @return
	 */
	public FileReader open() {
		return open("ISO-8859-1");
	}
	
	public FileReader open(final String encoding) {
		if (this.fileFullPath != null) {
			try {
				this.fInput = new FileInputStream(this.fileFullPath);
				this.fChan = fInput.getChannel();
				this.reader = new BufferedReader(Channels.newReader(fChan,
				encoding));
			} catch (FileNotFoundException e) {
				this.fInput = null;
				this.fChan = null;
				this.reader = null;
//				Log.logger.error(this.fileFullPath, e);
			}
		}

		return this;
	}

	/**
	 * @return
	 */
	public String readLine() {
		try {
			return this.reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			if (this.reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
//					Log.logger.error("Error while reading " + this.fileFullPath, e);
					System.out.printf("Error while reading " + this.fileFullPath, e);
				} finally {
					reader = null;
				}					
			}// if br
			
			if (fChan != null) {
				try {
					fChan.close();
				} catch (IOException e2) {
//					Log.logger.error("Error while closing reader for " + this.fileFullPath, e2);
				} finally {
					fChan = null;
				}					
			}// if fChan
			
			if (fInput != null) {
				try {
					fInput.close();
				} catch (IOException e3) {
//					Log.logger.error("Error while closing fInput for " + this.fileFullPath, e3);
				} finally {
					fInput = null;
				}					
			}// if fInput

			return null;
		} 
	}
	
	/**
	 * @return
	 */
	public List<String> readAll(){
		ArrayList<String> list = new ArrayList<String>();
		
		String line = null;
		
		try {
			while((line = this.reader.readLine())!=null){
				list.add(line);
			}
		} catch (IOException e) {
//			Log.logger.error("Error while reading " + this.fileFullPath, e);
		}
		
		return list;
	}
	
	/**
	 * @return
	 */
	public boolean close(){
		
		boolean flag = true;
		
		if (this.reader != null) {
			try {
				reader.close();
			} catch (IOException e1) {
//				Log.logger.error("Error while closing reader for " + this.fileFullPath, e1);
				flag = false;
			} finally {
				reader = null;
			}					
		}// if br
		
		if (fChan != null) {
			try {
				fChan.close();
			} catch (IOException e2) {
//				Log.logger.error("Error while closing reader for " + this.fileFullPath, e2);
				flag = false;
			} finally {
				fChan = null;
			}					
		}// if fChan
		
		if (fInput != null) {
			try {
				fInput.close();
			} catch (IOException e3) {
//				Log.logger.error("Error while closing reader for " + this.fileFullPath, e3);
				flag = false;
			} finally {
				fInput = null;
			}					
		}// if fInput
		
		return flag;
	}
	
	public boolean exists() {
		return FileReader.exists(this.fileFullPath);
	}
	
	public static boolean exists(String path) {
		return (new File(path)).exists();
	}
}