package config;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChromeRemoteProvider {
    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();
    private static final String DISABLED_NETWORK_FEATURES = String.join(",",
            "BlockInsecurePrivateNetworkRequests",
            "PrivateNetworkAccessSendPreflights",
            "PrivateNetworkAccessRespectPreflightResults",
            "PrivateNetworkAccessPermissionPrompt",
            "LocalNetworkAccessChecks",
            "LocalNetworkAccessChecksForNavigations"
    );

    private final ChromeOptions options;

    public ChromeRemoteProvider(ChromeOptions options) {
        this.options = options;
    }

    public static ChromeOptions createOptions(String testUrl, boolean remote, String... extraArguments) {
        ChromeOptions options = new ChromeOptions();
        List<String> arguments = new ArrayList<>();
        arguments.add("--disable-search-engine-choice-screen");
        arguments.add("--remote-allow-origins=*");
        arguments.add("--disable-features=" + DISABLED_NETWORK_FEATURES);
        arguments.add("--unsafely-treat-insecure-origin-as-secure=" + testUrl);

        if (remote) {
            arguments.add("--no-sandbox");
            arguments.add("--disable-dev-shm-usage");
            arguments.add("--disable-notifications");
            arguments.add("--allow-silent-push");
        }

        arguments.addAll(List.of(extraArguments));
        options.addArguments(arguments);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.content_settings.exceptions.local_network_access.*.setting", 1);
        options.setExperimentalOption("prefs", prefs);

        return options;
    }

    public RemoteWebDriver createDriver() {
        String remoteWebdriverUrl = PROPERTIES_READER.getEnvProperty("test.remote.webdriver.url");

        try {
            return new RemoteWebDriver(new URI(remoteWebdriverUrl).toURL(), options);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new InternalError(e);
        }
    }
}
