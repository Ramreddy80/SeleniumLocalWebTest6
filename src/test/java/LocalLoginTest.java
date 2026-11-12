import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;

public class LocalLoginTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");

        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.trim().isEmpty()) {
            File binFile = new File(chromeBin);
            if (binFile.exists()) {
                options.setBinary(chromeBin);
            }
        }

        String driverPath = System.getenv("CHROMEDRIVER_PATH");
        if (driverPath != null && !driverPath.trim().isEmpty()) {
            File dFile = new File(driverPath);
            if (dFile.exists()) {
                System.setProperty("webdriver.chrome.driver", driverPath);
            }
        } else {
            File defaultDriver = new File("/usr/bin/chromedriver");
            if (defaultDriver.exists()) {
                System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
            }
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                driver = new ChromeDriver(options);
                break;
            } catch (Exception ex) {
                if (attempt == 3) {
                    throw ex;
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {}
            }
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        File htmlFile = new File("webpage/login.html");
        driver.get(htmlFile.toURI().toString());
    }

    @Test
    public void validLoginTest() {
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("loginBtn"));

        usernameInput.clear();
        usernameInput.sendKeys("student");
        passwordInput.clear();
        passwordInput.sendKeys("12345");
        loginBtn.click();

        WebElement messageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message")));
        String messageText = messageElement.getText();

        Assert.assertEquals(messageText, "Login Successful", "Valid login should show 'Login Successful'");
    }

    @Test
    public void invalidLoginTest() {
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("loginBtn"));

        usernameInput.clear();
        usernameInput.sendKeys("invalidUser");
        passwordInput.clear();
        passwordInput.sendKeys("wrongPass");
        loginBtn.click();

        WebElement messageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message")));
        String messageText = messageElement.getText();

        Assert.assertEquals(messageText, "Invalid Username or Password", "Invalid login should show 'Invalid Username or Password'");
    }

    @Test
    public void checkBackgroundColor() {
        WebElement loginBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginBox")));
        String cssColor = loginBox.getCssValue("background-color");
        String rgbaColor = Color.fromString(cssColor).asRgba();

        Assert.assertEquals(rgbaColor, "rgba(173, 216, 230, 1)", "Login box background color should be rgba(173, 216, 230, 1)");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
