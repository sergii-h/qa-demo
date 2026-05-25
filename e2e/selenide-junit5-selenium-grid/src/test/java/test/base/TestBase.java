package test.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.junit5.ScreenShooterExtension;
import com.codeborne.selenide.logevents.SelenideLogger;
import config.ChromeRemoteProvider;
import config.PropertyReader;
import data.Platform;
import extension.TestWatcherExtension;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import provider.SupportProvider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

import static data.Platform.DESKTOP;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@ExtendWith({ScreenShooterExtension.class, TestWatcherExtension.class})
public abstract class TestBase {
    private final Platform platform;

    private static String browserInfo;
    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();
    private static final boolean IS_LOCAL = PROPERTIES_READER.getProperty("test.env").equals("local");
    private static final String TEST_URL = PROPERTIES_READER.getEnvProperty("test.url");

    protected final SupportProvider support = new SupportProvider();

    public SupportProvider support() { return support; }

    public TestBase(Platform platform) {
        this.platform = platform;

        SelenideLogger.addListener(
                "AllureSelenide",
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(false)
                        .enableLogs(LogType.BROWSER, Level.SEVERE)
        );

        Awaitility.setDefaultTimeout(Duration.ofSeconds(
                Long.parseLong(PROPERTIES_READER.getProperty("test.timeout.awaitility.default"))
        ));

        Configuration.timeout = Long.parseLong(PROPERTIES_READER.getProperty("test.timeout.selenide.default")) * 1000;
        Configuration.pageLoadTimeout = Long.parseLong(
                PROPERTIES_READER.getProperty("test.timeout.selenide.page-load")
        ) * 1000;

        Configuration.baseUrl = TEST_URL;
    }

    @BeforeEach
    protected void setUpBrowser() {
        setUpByPlatform();

        String disableSearchEngine = "--disable-search-engine-choice-screen";
        String chromeOptions = System.getProperty("chromeoptions.args", disableSearchEngine);

        if (!chromeOptions.contains(disableSearchEngine)) {
            System.setProperty("chromeoptions.args", chromeOptions + ", " + disableSearchEngine);
        }

        if (!IS_LOCAL) {
            initRemoteDriver();
        }
    }

    @AfterEach
    public void tearDown() {
        support.clearMocks();
        Selenide.closeWebDriver();
    }

    @AfterAll
    public static void afterAll() {
        if (IS_LOCAL) {
            return;
        }

        Properties prop = new Properties();
        prop.setProperty("browser", Optional.ofNullable(browserInfo).orElse(EMPTY));

        try {
            prop.store(new FileOutputStream(System.getProperty("allure.results.directory") + "/environment.properties"), null);
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

    public abstract void setUpByPlatform();

    private void initRemoteDriver() {
        String desktopBrowserSize = PROPERTIES_READER.getProperty("test.desktop.browser-size");
        String mobileBrowserWidth = PROPERTIES_READER.getProperty("test.mobile.browser-width");
        String mobileBrowserHeight = PROPERTIES_READER.getProperty("test.mobile.browser-height");
        String mobileUserAgent = PROPERTIES_READER.getProperty("test.mobile.user-agent");

        RemoteWebDriver remoteWebDriver = platform.equals(DESKTOP)
                    ? new ChromeRemoteProvider(format("window-size=%s", desktopBrowserSize))
                    .createDriver(new DesiredCapabilities())
                    : new ChromeRemoteProvider(
                    format("window-size=%s,%s", mobileBrowserWidth, mobileBrowserHeight),
                    format("user-agent=%s", mobileUserAgent)
            ).createDriver(new DesiredCapabilities());

            browserInfo = remoteWebDriver.getCapabilities().getBrowserName() + " " +
                    remoteWebDriver.getCapabilities().getBrowserVersion();

            remoteWebDriver.setFileDetector(new LocalFileDetector());
            WebDriver driver = new Augmenter().augment(remoteWebDriver);
            WebDriverRunner.setWebDriver(driver);
    }
}
