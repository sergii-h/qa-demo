package test;

import browserprovider.ChromeRemoteProvider;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.junit5.ScreenShooterExtension;
import com.codeborne.selenide.logevents.SelenideLogger;
import data.Environment;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

import static com.codeborne.selenide.Selenide.executeJavaScript;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;
import static test.TestBase.Platform.DESKTOP;
import static test.TestBase.Platform.MOBILE;

@Slf4j
@ExtendWith({ScreenShooterExtension.class})
public abstract class TestBase implements TestWatcher, BeforeAllCallback, BeforeEachCallback, AfterAllCallback {
    public static final Environment ENV = Environment.builder(System.getProperty("test.env", "local"))
            .externalUrl(System.getProperty("test.ext.url", ""))
            .build();

    public enum Platform {
        DESKTOP, MOBILE
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        SelenideLogger.addListener(
                "AllureSelenide",
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(false)
                        .enableLogs(LogType.BROWSER, Level.SEVERE)
        );

        Configuration.timeout = 30000;
        Configuration.baseUrl = ENV.url;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (!ENV.isLocal) {
            RemoteWebDriver remoteWebDriver = (RemoteWebDriver) context
                    .getStore(GLOBAL)
                    .get("remoteWebDriver");

            ENV.browserInfo = remoteWebDriver.getCapabilities().getBrowserName() + " " +
                    remoteWebDriver.getCapabilities().getBrowserVersion();

            remoteWebDriver.setFileDetector(new LocalFileDetector());
            WebDriverRunner.setWebDriver(remoteWebDriver);

            log.info(context.getDisplayName() + " WebDriver session id: " + remoteWebDriver.getSessionId());
        }

        Selenide.open(ENV.url);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (ENV.isLocal) return;

        Properties prop = new Properties();
        prop.setProperty("browser", Optional.ofNullable(ENV.browserInfo).orElse(""));
        prop.setProperty("env", Optional.ofNullable(ENV.id).orElse(""));
        String envFilePath = System.getProperty("allure.results.directory") + "/environment.properties";

        try {
            prop.store(new FileOutputStream(envFilePath), null);
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        if (WebDriverRunner.hasWebDriverStarted()) WebDriverRunner.closeWebDriver();
    }

    @Step("Get full page screenshot")
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (WebDriverRunner.hasWebDriverStarted()) {
            takeFullPageScreenshot();
            WebDriverRunner.closeWebDriver();
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        if (WebDriverRunner.hasWebDriverStarted()) WebDriverRunner.closeWebDriver();
    }

    private void takeFullPageScreenshot() {
        takeFullPageScreenshot(0);
    }

    private void takeFullPageScreenshot(long stickyHeaderHeight) {
        Selenide.switchTo().defaultContent();

        long pageHeight = Objects.requireNonNull(executeJavaScript("return document.documentElement.scrollHeight"));
        long clientHeight = Objects.requireNonNull(executeJavaScript("return document.documentElement.clientHeight"));

        clientHeight = clientHeight - stickyHeaderHeight;

        if (pageHeight * 1.0 / clientHeight <= 1) {
            addScreenshot("screenshot");
            return;
        }

        for (int i = 0; i < pageHeight/clientHeight+1; i++) {
            executeJavaScript("window.scrollTo(0," + (clientHeight*i) + ")");
            addScreenshot("screenshot-" + (i + 1));
        }
    }

    private void addScreenshot(String screenshotName) {
        try {
            Allure.addAttachment(
                    screenshotName,
                    new FileInputStream(Objects.requireNonNull(Selenide.screenshot(OutputType.FILE)))
            );
        } catch (FileNotFoundException e) {
            throw new InternalError(e);
        }
    }

    public static class Desktop extends TestBase {
        @Override
        public void beforeEach(ExtensionContext context) {
            if (ENV.isLocal) {
                System.setProperty("chromeoptions.args", "--remote-allow-origins=*");
            } else {
                RemoteWebDriver webDriver = new ChromeRemoteProvider("window-size=1920,1080")
                        .createDriver(new DesiredCapabilities());

                context.getStore(GLOBAL).put("remoteWebDriver", webDriver);
            }

            context.getStore(GLOBAL).put("platform", DESKTOP);
            super.beforeEach(context);
        }
    }

    public static class Mobile extends TestBase {
        @Override
        public void beforeEach(ExtensionContext context) {
            String userAgentValue = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 " +
                    "(KHTML, like Gecko) Mobile/15E148";
            String browserWidth = "375";
            String browserHeight = "812";

            if (ENV.isLocal) {
                Configuration.browserSize= browserWidth + "x" + browserHeight;
                System.setProperty("chromeoptions.args", "--user-agent=" +"\"" + userAgentValue + "\"");
            } else {
                RemoteWebDriver webDriver = new ChromeRemoteProvider(
                        "window-size=" + browserWidth + "," + browserHeight,
                        "user-agent=" + userAgentValue
                ).createDriver(new DesiredCapabilities());

                context.getStore(GLOBAL).put("remoteWebDriver", webDriver);
                super.beforeEach(context);
            }

            context.getStore(GLOBAL).put("platform", MOBILE);
            Selenide.open(ENV.url);
        }
    }
}
