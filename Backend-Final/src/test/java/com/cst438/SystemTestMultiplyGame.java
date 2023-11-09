package com.cst438;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SystemTestMultiplyGame {

	public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver_win32/chromedriver.exe";
	public static final String URL = "http://localhost:3000";
	public static final String ALIAS_NAME = "test";
	public static final int SLEEP_DURATION = 1000; // 1 second.
	
	WebDriver driver;
	
	@BeforeEach
	public void testSetup() throws Exception {

		// set the driver location and start driver
		//@formatter:off
		//
		// browser	property name 				Java Driver Class
		// -------  ------------------------    ----------------------
		// Edge 	webdriver.edge.driver 		EdgeDriver
		// FireFox 	webdriver.firefox.driver 	FirefoxDriver
		// IE 		webdriver.ie.driver 		InternetExplorerDriver
		// Chrome   webdriver.chrome.driver     ChromeDriver
		//
		//@formatter:on

        //TODO if you are not using Chrome, the following lines will be different. 
		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		ChromeOptions ops = new ChromeOptions();
		ops.addArguments("--remote-allow-origins=*");
		driver = new ChromeDriver(ops);

		driver.get(URL);
        // must have a short wait to allow time for the page to download 
		Thread.sleep(SLEEP_DURATION);
	}
	
	@Test
	public void playGoodGame() throws Exception {
		// find web elements by id= or name= 
		WebElement factora = driver.findElement(By.id("factora"));
		WebElement factorb = driver.findElement(By.id("factorb"));
		WebElement attempt = driver.findElement(By.name("attempt"));
		WebElement message = driver.findElement(By.id("message"));
		WebElement submit = driver.findElement(By.id("submit"));
		WebElement alias = driver.findElement(By.name("alias"));
		
		int a = Integer.parseInt(factora.getText());
		int b = Integer.parseInt(factorb.getText());
		
		// enter a correct answer
		attempt.sendKeys(Integer.toString(a*b));
		alias.sendKeys("dw");
		Thread.sleep(SLEEP_DURATION);
		
		
		// submit the problem
		submit.click();
		Thread.sleep(SLEEP_DURATION);
		
		// verify the message  
		assertEquals("Correct.", message.getText());
		
		WebElement historyLink = driver.findElement(By.xpath("//a[@href='/history']"));
		// go to history page
		historyLink.click();
		Thread.sleep(SLEEP_DURATION);
		
		// find the second <tr> node
		List<WebElement> listTd = driver.findElements(By.xpath("//td"));
		// the first 5 <td> nodes are the first row in the history table
		assertThat(listTd.size()).isGreaterThanOrEqualTo(5);
		// verify factorA and factorB
		assertThat(Integer.parseInt(listTd.get(0).getText())).isEqualTo(a);
		assertThat(Integer.parseInt(listTd.get(1).getText())).isEqualTo(b);
		assertThat(Integer.parseInt(listTd.get(2).getText())).isEqualTo(a*b);
		assertThat(Integer.parseInt(listTd.get(3).getText())).isEqualTo(a*b);
		// verify that "correct" column has the word "true"
		assertThat(listTd.get(4).getText()).isEqualTo("true");
		
	}
	
	
	@AfterEach
	public void cleanup() {
		if (driver!=null) {
			driver.close();
			driver.quit();
			driver=null;
		}
	}
}
