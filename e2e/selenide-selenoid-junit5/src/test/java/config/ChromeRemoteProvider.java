package config;

import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class ChromeRemoteProvider implements WebDriverProvider {
    private final String[] arguments;

    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();

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

        String remoteWebdriverUrl = PROPERTIES_READER.getEnvProperty("test.remote.webdriver.url");

        try {
            return new RemoteWebDriver(new URI(remoteWebdriverUrl).toURL(), options);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new InternalError(e);
        }
    }
}
