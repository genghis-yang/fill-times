package genghis.tools.filltimes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertyReader {

	private static final Logger logger = LogManager.getLogger(PropertyReader.class);
	
	private static Properties properties = new Properties();

	static {
		try {
			properties.load(new FileInputStream(new File("conf.properties")));
		} catch (IOException e) {
			logger.error("Load properties failed.|", e);
		}
	}

	public static String get(String key) {
		String value = (String) properties.get(key);
		return value == null ? "" : value;
	}
	
	private PropertyReader() {
	}
}
