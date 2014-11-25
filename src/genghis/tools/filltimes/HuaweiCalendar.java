package genghis.tools.filltimes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HuaweiCalendar {

	private static final Logger logger = LogManager.getLogger(HuaweiCalendar.class);

	private static List<Boolean> huaweiCalendar;
	
	static {
		init();
	}

	private static void init() {
		try {
			File huaweiCalendarFile = new File("huawei_calendar.csv");
			String huaweiCalendarContent = FileUtils.readFileToString(huaweiCalendarFile, "UTF-8");
			String[] huaweiCalendarArray = huaweiCalendarContent.split(",");
			if (huaweiCalendarArray.length != 365 && huaweiCalendarArray.length != 366) {
				logger.error("Format not correct. File: " + huaweiCalendarFile.getAbsolutePath());
				throw new RuntimeException("Format not correct. File: " + huaweiCalendarFile.getAbsolutePath());
			}
			Boolean[] booleanArray = new Boolean[huaweiCalendarArray.length];
			for (int i = 0; i < huaweiCalendarArray.length; i++) {
				booleanArray[i] = ("1".equals(huaweiCalendarArray[i]));
			}
			huaweiCalendar = new ArrayList<Boolean>(Arrays.asList(booleanArray));
			logger.info("Read huawei calendar: " + huaweiCalendar);
		} catch (IOException e) {
			logger.fatal("Can not read the huawei calendar file.", e);
		}
	}
	
	public static List<Boolean> getHuaweiCalendar() {
		return huaweiCalendar;
	}
	
	public static Boolean isWorkingDay(Integer dayOfYear) {
		return huaweiCalendar.get(dayOfYear);
	}
	
	private HuaweiCalendar() {
	}

}
