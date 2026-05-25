package config;

import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class ChromeRemoteProvider implements WebDriverProvider {
    private final String[] platformArguments;

    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();

    public ChromeRemoteProvider(String... platformArguments) {
        this.platformArguments = platformArguments;
    }

    @Override
    public RemoteWebDriver createDriver(Capabilities capabilities) {
        ChromeOptions options = new ChromeOptions();

        String chromeOptions = System.getProperty("chromeoptions.args", "");
        if (!chromeOptions.isBlank()) {
            options.addArguments(Arrays.stream(chromeOptions.split(", "))
                    .map(String::trim)
                    .filter(arg -> !arg.isEmpty())
                    .toList());
        }

        options.addArguments(platformArguments);
        options.merge(capabilities);

        String remoteWebdriverUrl = PROPERTIES_READER.getEnvProperty("test.remote.webdriver.url");

        try {
            return new RemoteWebDriver(new URI(remoteWebdriverUrl).toURL(), options);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new InternalError(e);
        }
    }
}
