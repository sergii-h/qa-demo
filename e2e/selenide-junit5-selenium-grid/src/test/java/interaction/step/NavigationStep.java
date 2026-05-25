package interaction.step;

import com.codeborne.selenide.Selenide;
import config.PropertyReader;
import io.qameta.allure.Step;

public class NavigationStep {
    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();
    private static final String TEST_URL = PROPERTIES_READER.getEnvProperty("test.url");

    @Step("Open main page")
    public void openMainPage() {
        Selenide.open(TEST_URL);
    }
}
