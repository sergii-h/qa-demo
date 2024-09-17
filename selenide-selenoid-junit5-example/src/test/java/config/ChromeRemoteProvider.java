package config;

import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;

public class ChromeRemoteProvider implements WebDriverProvider {
    private final String[] arguments;

    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();
    private static final String TEST_ENV = PROPERTIES_READER.getProperty("test.env");

    public ChromeRemoteProvider(String... arguments) {
        this.arguments = arguments;
    }

    @Nonnull
    public RemoteWebDriver createDriver(@Nonnull Capabilities capabilities) {
        ChromeOptions options = new ChromeOptions();

        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-notifications",
                "--allow-silent-push",
                "--remote-allow-origins=*",
                "--disable-search-engine-choice-screen"
        );

        options.addArguments(arguments);
        options.merge(capabilities);

        String remoteWebdriverUrl = PROPERTIES_READER.getProperty(TEST_ENV + ".test.remote.webdriver.url");

        try {
            return new RemoteWebDriver(new URL(remoteWebdriverUrl), options);
        } catch (final MalformedURLException e) {
            throw new InternalError("Unable to create driver. MalformedURLException", e);
        }
    }
}
