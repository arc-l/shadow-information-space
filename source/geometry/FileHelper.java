package geometry;

import java.applet.Applet;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Jingjin Yu
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class FileHelper {

	private static Logger logger = Logger.getLogger(FileHelper.class);

	// We want to support different types of properties file
	public static final int FILE_TYPE_UNKNOWN = 0;

	public static final int FILE_TYPE_XML = 1;

	public static final int FILE_TYPE_PROPERTIES = 2;

	private static String baseUrl = null;

	public static void AppletInit(Applet app) {
		baseUrl = app.getCodeBase().getProtocol()
				+ "://"
				+ app.getCodeBase().getHost()
				+ ((app.getCodeBase().getPort() != -1) ? ":"
						+ app.getCodeBase().getPort() : "")
				+ app.getCodeBase().getPath();
	}

	/**
	 * We want to load files relative to code base folder, not the jar file
	 * since sometimes it's tricky to load all types of files from that
	 * location. Also it seems a bit akward to put all different types of files
	 * under java folders.
	 * 
	 */
	public static void regularInit() {
		baseUrl = FileHelper.class.getProtectionDomain().getCodeSource()
				.getLocation().toString();
		if (baseUrl.substring(baseUrl.length() - 4).equals(".jar")) {
			baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);
		}
	}

	/**
	 * Return base url stored. Base url must be set before retrieving. The base
	 * url serves as the location where resources such as images and properties
	 * are stored in a relative folder strucutre
	 * 
	 * @return
	 */
	public static String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Set the base url, which can be a file or any other valid url
	 * 
	 * @param base
	 */
	public static void setBaseUrl(String base) {
		baseUrl = base;
	}

	/**
	 * Wrap a inputstream in a buffered reader
	 * 
	 * @param in
	 * @return
	 */
	public static BufferedReader getBufferedReaderFromStream(InputStream in) {
		return new BufferedReader(new InputStreamReader(in));
	}

	/**
	 * Open file as input stream from default base URL
	 * 
	 * @param file
	 * @return
	 */
	public static InputStream getStreamFromUrl(String file) {
		return getStreamFromUrl(baseUrl, file);
	}

	/**
	 * Open file under base URL as input stream
	 * 
	 * @param baseUrl
	 * @param file
	 * @return
	 */
	public static InputStream getStreamFromUrl(String baseUrl, String file) {
		try {
			URL url = new URL(baseUrl + file);
			BufferedInputStream bis = new BufferedInputStream(url.openStream(),
					10240);
			return bis;
		} catch (Exception e) {
			logger.error("Cannot open file as input stream, base URL: "
					+ baseUrl + ", file: " + file);
		}
		return null;

	}

	/**
	 * Load property file as a file on local file system
	 * 
	 */
	public static Properties loadProfile(String propertyFile) {
		Properties prop = new Properties();
		try {
			FileInputStream fis = new FileInputStream(new File(propertyFile));
			return loadProfile(fis);
		} catch (Exception e) {
			logger.error("Cannot load properties from file: " + propertyFile);
		}
		return prop;
	}

	/**
	 * Load properties from an input stream
	 * 
	 */
	public static Properties loadProfile(InputStream inputStream) {
		Properties prop = new Properties();
		try {
			prop.load(inputStream);
		} catch (Exception e) {
			logger.error("Cannot load properties given stream, error: "
					+ e.getMessage());
		}
		return prop;
	}

	/**
	 * Load property file from a URL path
	 * 
	 */
	public static Properties loadUrlProfile(String sourceUrlPath,
			String propertyFile) {
		Properties prop = new Properties();
		try {
			URL url = new URL(sourceUrlPath + propertyFile);
			BufferedInputStream bis = new BufferedInputStream(url.openStream(),
					10240);
			prop.load(bis);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Cannot load properties from file: " + propertyFile);
		}
		return prop;
	}

	/**
	 * Load property file relative to default base URL
	 * 
	 */
	public static Properties loadUrlProfile(String propertyFile) {
		return loadUrlProfile(getBaseUrl(), propertyFile);
	}

	/**
	 * Write string content to given file
	 * 
	 * @param fileName
	 * @param content
	 * @throws IOException
	 */
	public static void writeBufferToFile(String fileName, String content)
			throws IOException {
		// Open file
		BufferedWriter bos = new BufferedWriter(new FileWriter(fileName));

		// Write and close
		bos.write(content);
		bos.close();

	}

	/**
	 * Append content to given file
	 * 
	 * @param fileName
	 * @param content
	 * @throws IOException
	 */
	public static void appendBufferToFile(String fileName, String content)
			throws IOException {
		// Open file
		BufferedWriter bos = new BufferedWriter(new FileWriter(fileName, true));
		// Write and close
		bos.write(content);
		bos.newLine();
		bos.close();
	}

	/**
	 * This method will open any URL input source and copy the content to local
	 * path as binary file
	 * 
	 * @param fileName
	 * @param sourceUrlPath
	 * @param destPath
	 */
	public static void copyUrlBinaryFile(String fileName, String sourceUrlPath,
			String destPath) {
		try {

			// Check whether the file is already there locally
			File temp = new File(destPath + fileName);
			if (temp.exists())
				return;

			URL url = new URL(sourceUrlPath + fileName);
			BufferedInputStream bis = new BufferedInputStream(url.openStream(),
					10240);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(destPath + fileName), 10240);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = bis.read(buffer)) != -1) {
				if (bytesRead > 0)
					bos.write(buffer, 0, bytesRead);
			}
			bos.close();
			bis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Load properties from file
	 * 
	 * @param fileName
	 * @return
	 */
	public static Properties readPropertiesFromFile(String fileName) {
		Properties prop = new Properties();
		try {
			File propFile = new File(fileName);
			if (!propFile.isDirectory()) {
				FileInputStream PropInStream = new FileInputStream(propFile);
				prop.load(PropInStream);
			}
		} catch (IOException ioExc) {
			ioExc.printStackTrace();
			return null;
		}
		return prop;
	}
}
