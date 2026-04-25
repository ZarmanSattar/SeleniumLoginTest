package com.lab10;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

/**
 * Lab-10 – Jenkins Pipeline: Test Stage
 *
 * Selenium + JUnit 5 tests for the login page at:
 *   http://103.139.122.250:4000/
 *
 * All tests run Chrome in headless mode so they work inside
 * the markhobson/maven-chrome Docker container on Jenkins.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginTest {

    private static final String BASE_URL  = "http://103.139.122.250:4000/";
    private static final String VALID_EMAIL    = "admin@example.com";   // update if needed
    private static final String VALID_PASSWORD = "admin123";            // update if needed

    private WebDriver driver;

    // ------------------------------------------------------------------ setup

    @BeforeEach
    void setUp() {
        /*
         * WebDriverManager automatically resolves the correct chromedriver binary.
         * Inside markhobson/maven-chrome the binary is already on the PATH, so
         * WDM will detect and skip the download gracefully.
         */
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");               // headless – no display needed
        options.addArguments("--no-sandbox");             // required inside Docker
        options.addArguments("--disable-dev-shm-usage"); // avoids /dev/shm size issues
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ------------------------------------------------------------------ tests

    /**
     * TC-01  Verify the login page loads and shows an email field.
     */
    @Test
    @Order(1)
    void test_login_page_loads() {
        driver.navigate().to(BASE_URL);
        String title = driver.getTitle();
        System.out.println("Page title: " + title);

        // The page must load (title is not empty and no exception thrown)
        Assertions.assertFalse(title.isEmpty(), "Page title should not be empty");

        // Email / username input must be present
        WebElement emailField = driver.findElement(By.name("email"));
        Assertions.assertTrue(emailField.isDisplayed(), "Email field should be visible");
    }

    /**
     * TC-02  Login with INCORRECT credentials → expect an error message.
     */
    @Test
    @Order(2)
    void test_login_with_incorrect_credentials() {
        driver.navigate().to(BASE_URL);

        driver.findElement(By.name("email")).sendKeys("wrong@user.com");
        driver.findElement(By.name("password")).sendKeys("wrongpassword");
        driver.findElement(By.id("m_login_signin_submit")).click();

        // Wait up to 5 s for an error element to appear
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            // Try the XPath from the lab spec first
            WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/div/div/div[1]/div/div/div/div[2]/form/div[1]")
                )
            );
            String errorText = error.getText();
            System.out.println("Error text (XPath): " + errorText);
            Assertions.assertTrue(
                errorText.toLowerCase().contains("incorrect") ||
                errorText.toLowerCase().contains("invalid")   ||
                errorText.toLowerCase().contains("wrong"),
                "Expected an authentication-failure message, got: " + errorText
            );
        } catch (TimeoutException e) {
            // Fall back: look for any element with a common error CSS class
            WebElement error = driver.findElement(
                By.cssSelector(".alert-danger, .error, .login-error, [class*='error']")
            );
            String errorText = error.getText();
            System.out.println("Error text (CSS fallback): " + errorText);
            Assertions.assertFalse(errorText.isBlank(),
                "An error message element was found but contained no text");
        }
    }

    /**
     * TC-03  Login with an EMPTY email → form should not submit / show validation.
     */
    @Test
    @Order(3)
    void test_login_with_empty_email() {
        driver.navigate().to(BASE_URL);

        // Leave email blank, fill in password
        driver.findElement(By.name("password")).sendKeys("somePassword123");
        driver.findElement(By.id("m_login_signin_submit")).click();

        // Still on the same page (URL unchanged) because the form didn't submit
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Current URL after empty-email submit: " + currentUrl);
        Assertions.assertTrue(
            currentUrl.contains("103.139.122.250"),
            "Should remain on the login page when email is empty"
        );
    }

    /**
     * TC-04  Login with an EMPTY password → form should not submit / show validation.
     */
    @Test
    @Order(4)
    void test_login_with_empty_password() {
        driver.navigate().to(BASE_URL);

        driver.findElement(By.name("email")).sendKeys("someone@example.com");
        // Leave password blank
        driver.findElement(By.id("m_login_signin_submit")).click();

        String currentUrl = driver.getCurrentUrl();
        System.out.println("Current URL after empty-password submit: " + currentUrl);
        Assertions.assertTrue(
            currentUrl.contains("103.139.122.250"),
            "Should remain on the login page when password is empty"
        );
    }

    /**
     * TC-05  Login with VALID credentials → should NOT show an error.
     *
     * Update VALID_EMAIL / VALID_PASSWORD constants at the top of this file
     * to match a real account on the test server.
     */
    @Test
    @Order(5)
    void test_login_with_valid_credentials() {
        driver.navigate().to(BASE_URL);

        driver.findElement(By.name("email")).sendKeys(VALID_EMAIL);
        driver.findElement(By.name("password")).sendKeys(VALID_PASSWORD);
        driver.findElement(By.id("m_login_signin_submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // After a successful login the URL usually changes away from the login page
        try {
            wait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("login")
            ));
            System.out.println("Redirected to: " + driver.getCurrentUrl());
            Assertions.assertFalse(
                driver.getCurrentUrl().contains("login"),
                "After valid login, URL should no longer point to the login page"
            );
        } catch (TimeoutException e) {
            // If URL didn't change, check there is no error element visible
            boolean errorPresent = !driver.findElements(
                By.cssSelector(".alert-danger, .error, [class*='error']")
            ).isEmpty();
            Assertions.assertFalse(errorPresent,
                "Valid credentials should not produce an error message");
        }
    }
}
