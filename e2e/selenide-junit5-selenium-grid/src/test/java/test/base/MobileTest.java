package test.base;

import com.codeborne.selenide.Configuration;
import config.PropertyReader;

import static data.Platform.MOBILE;
import static java.lang.String.format;

public abstract class MobileTest extends TestBase {

    public MobileTest() {
        super(MOBILE);
    }

    @Override
    public void setUpByPlatform() {
        PropertyReader propertiesReader = PropertyReader.getInstance();
        Configuration.browserSize = format("%sx%s", propertiesReader.getProperty("test.mobile.browser-width"),
                propertiesReader.getProperty("test.mobile.browser-height"));

        System.setProperty(
                "chromeoptions.args",
                format(
                        "--remote-allow-origins=*, --user-agent=\"%s\"",
                        propertiesReader.getProperty("test.mobile.user-agent")
                )
        );
    }
}
