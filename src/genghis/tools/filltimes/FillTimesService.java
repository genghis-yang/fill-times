package genghis.tools.filltimes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class FillTimesService {

	private static final Logger logger = LogManager.getLogger(FillTimesService.class);

	private static final long TIMEOUT = 60000L;

	private static final int EIGHT_HOUR = 8;

	private static final int ZERO_HOUR = 0;

	private PersonInfo personInfo = new PersonInfo(PropertyReader.get("uid"), PropertyReader.get("password"));

	private WebDriver driver = new FirefoxDriver();

	private Calendar calendar = Calendar.getInstance();

	public FillTimesService() {
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
	}

	public static void main(String[] args) {
		new FillTimesService().fillTimes();
	}

	public void fillTimes() {
		logger.info("==============Start to fill the TIMES==============");
		login();
		selectCurrentWeek();
		fillProjectInfo();
		fillTimeCard();
		submitTimes();
		logger.info("==============End of filling the TIMES=============");
	}

	private void login() {
		driver.get("http://app.huawei.com/timeswww");
		WebElement uidInput = driver.findElement(By.name("uid"));
		uidInput.sendKeys(personInfo.getUid());
		WebElement passwordInput = driver.findElement(By.name("password"));
		passwordInput.sendKeys(personInfo.getPassword());
		WebElement submitElement = driver.findElement(By.className("login_submit_pwd"));
		logger.info("Login to TIMES with user id:" + personInfo.getUid() + " and password:" + personInfo.getPassword());
		submitElement.click();
		String arriviedFlag = driver.getTitle();

		long timeStarted = System.currentTimeMillis();
		long timePassed = System.currentTimeMillis();
		while (!"Times->Claim".equals(arriviedFlag) && timePassed - timeStarted < TIMEOUT) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
			arriviedFlag = driver.getTitle();
		}
		if (timePassed - timeStarted >= TIMEOUT) {
			logger.error("Login to TIMES timeout.");
			throw new RuntimeException("Login to TIMES timeout.");
		}
		logger.info("Arrived the target page:" + arriviedFlag);
	}

	private void selectCurrentWeek() {
		Calendar wednesdayDate = Calendar.getInstance();
		wednesdayDate.setFirstDayOfWeek(Calendar.MONDAY);
		wednesdayDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (calendar.after(wednesdayDate)) {
			WebElement selectElement = driver.findElement(By.id("workweek"));
			String currentWeek = getCurrentWeek(calendar);
			List<WebElement> allOptions = selectElement.findElements(By.tagName("option"));
			for (WebElement option : allOptions) {
				String optionValue = option.getAttribute("value");
				if (StringUtils.startsWith(optionValue, currentWeek)) {
					if (optionValue.endsWith("√")) {
						logger.warn("This week " + currentWeek + " has submit the TIMES.");
						break;
					}
					new Select(selectElement).selectByValue(optionValue);
					logger.info("Select current week " + optionValue);
					break;
				}
			}
		} else {
			logger.info("Today is too early to fill the TIMES. Please wait until Thursday.");
		}
	}

	private String getCurrentWeek(Calendar calendar) {
		StringBuffer currentWeekString = new StringBuffer();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		currentWeekString.append(dateFormat.format(calendar.getTime()));
		currentWeekString.append(" ~ ");
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		currentWeekString.append(dateFormat.format(calendar.getTime()));
		return currentWeekString.toString();
	}

	private void fillProjectInfo() {
		WebElement copyFromLastWeekButton = driver.findElement(By.name("Submit7"));
		copyFromLastWeekButton.click();
		sleepQuietly(5000);
		Alert alert = driver.switchTo().alert();
		logger.info("Confim window text: " + alert.getText());
		alert.accept();
		sleepQuietly(5000);
		logger.info("The project info has been copied from last week.");
	}

	private void sleepQuietly(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	private void fillTimeCard() {
		String[] weekDays = new String[] { "monTime", "tueTime", "wenTime", "thuTime", "friTime", "satTime", "sunTime" };
		Calendar monday = Calendar.getInstance();
		monday.setFirstDayOfWeek(Calendar.MONDAY);
		monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		int mondayNum = monday.get(Calendar.DAY_OF_YEAR) - 1;
		for (int i = 0; i < weekDays.length; i++) {
			if (HuaweiCalendar.isWorkingDay(mondayNum + i)) {
				dealOneDayItem(weekDays[i], EIGHT_HOUR);
			} else {
				dealOneDayItem(weekDays[i], ZERO_HOUR);
			}
		}
	}

	private void dealOneDayItem(String day, int hours) {
		WebElement dayItem = driver.findElement(By.name("timecardVO.timeCardItemList[0]." + day));
		dayItem.clear();
		dayItem.sendKeys(String.valueOf(hours));
	}

	private void submitTimes() {
		WebElement nextStep = driver.findElement(By.id("toSubmit_bnto"));
		nextStep.click();
		WebElement submitButton = driver.findElement(By.id("bnt_submit"));
		submitButton.click();
		sleepQuietly(10000);
		if (isSubmitSuccessfully()) {
			logger.info("TIMES has been submit successfully.");
		} else {
			logger.error("Submit TIMES failed. Unknow reason.");
			throw new RuntimeException("Submit TIMES failed. Unknow reason.");
		}
	}

	private boolean isSubmitSuccessfully() {
		String currentWindowHandle = driver.getWindowHandle();
		Set<String> windowHandles = driver.getWindowHandles();
		for (String windowHandle : windowHandles) {
			if (currentWindowHandle.equals(windowHandle)) {
				continue;
			} else {
				driver.switchTo().window(windowHandle);
				if ("Information".equals(driver.getTitle())) {
					return true;
				}
				continue;
			}
		}
		return false;
	}
}
