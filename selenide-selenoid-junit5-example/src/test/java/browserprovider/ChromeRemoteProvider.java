package browserprovider;

import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;

import static test.TestBase.ENV;

public class ChromeRemoteProvider implements WebDriverProvider {
    private final String[] arguments;

    public ChromeRemoteProvider(String... arguments) {
        this.arguments = arguments;
    }

    @Nonnull
    @Override
    public RemoteWebDriver createDriver(@Nonnull Capabilities capabilities) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-notifications",
                "--allow-silent-push",
                "--remote-allow-origins=*",
                "--disable-search-engine-choice-screen"
        );

        options.addArguments(arguments);
        options = options.merge(capabilities);

        try {
            return new RemoteWebDriver(new URL(ENV.remoteWebDriverUrl), options);
        } catch (final MalformedURLException e) {
            throw new InternalError("Unable to create driver", e);
        }
    }
}