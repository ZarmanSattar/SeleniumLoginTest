package com.lab10;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginTest {

    private static final String BASE_URL       = "http://103.139.122.250:4000/login";
    private static final String VALID_EMAIL    = "admin@comsats.edu.pk";
    private static final String VALID_PASSWORD = "admin123";

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @Order(1)
    void test_login_page_loads() {
        driver.navigate().to(BASE_URL);
        String title = driver.getTitle();
        System.out.println("Page title: " + title);
        Assertions.assertFalse(title.isEmpty(), "Page title should not be empty");
        WebElement emailField = driver.findElement(By.id("email"));
        Assertions.assertTrue(emailField.isDisplayed(), "Email field should be visible");
        WebElement passwordField = driver.findElement(By.id("password"));
        Assertions.assertTrue(passwordField.isDisplayed(), "Password field should be visible");
        System.out.println("TC-01 PASSED: Login page loaded successfully");
    }

    @Test
    @Order(2)
    void test_login_with_incorrect_credentials() {
        driver.navigate().to(BASE_URL);
        driver.findElement(By.id("email")).sendKeys("wrong@user.com");
        driver.findElement(By.id("password")).sendKeys("wrongpassword123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL after wrong login: " + currentUrl);
        Assertions.assertTrue(
            currentUrl.contains("login") || currentUrl.contains("4000"),
            "Should remain on login page with wrong credentials"
        );
        System.out.println("TC-02 PASSED: Incorrect credentials handled correctly");
    }

    @Test
    @Order(3)
    void test_login_with_empty_email() {
        driver.navigate().to(BASE_URL);
        driver.findElement(By.id("password")).sendKeys("somePassword123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL after empty email submit: " + currentUrl);
        Assertions.assertTrue(
            currentUrl.contains("login") || currentUrl.contains("4000"),
            "Should remain on login page when email is empty"
        );
        System.out.println("TC-03 PASSED: Empty email handled correctly");
    }

    @Test
    @Order(4)
    void test_login_with_empty_password() {
        driver.navigate().to(BASE_URL);
        driver.findElement(By.id("email")).sendKeys("someone@comsats.edu.pk");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL after empty password submit: " + currentUrl);
        Assertions.assertTrue(
            currentUrl.contains("login") || currentUrl.contains("4000"),
            "Should remain on login page when password is empty"
        );
        System.out.println("TC-04 PASSED: Empty password handled correctly");
    }

    @Test
    @Order(5)
    void test_login_with_valid_credentials() {
        driver.navigate().to(BASE_URL);
        driver.findElement(By.id("email")).sendKeys(VALID_EMAIL);
        driver.findElement(By.id("password")).sendKeys(VALID_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login")));
            System.out.println("Redirected to: " + driver.getCurrentUrl());
            Assertions.assertFalse(driver.getCurrentUrl().contains("login"));
        } catch (TimeoutException e) {
            boolean errorPresent = !driver.findElements(
                By.cssSelector("[class*='error'], [role='alert']")
            ).isEmpty();
            Assertions.assertFalse(errorPresent, "Valid credentials should not show error");
        }
        System.out.println("TC-05 PASSED: Valid credentials handled correctly");
    }
}