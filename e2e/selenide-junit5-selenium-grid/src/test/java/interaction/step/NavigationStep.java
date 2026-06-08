package interaction.step;

import com.codeborne.selenide.Selenide;
import config.PropertyReader;
import interaction.page.MainPage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;

public class NavigationStep {
    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();
    private static final String TEST_URL = PROPERTIES_READER.getEnvProperty("test.url");
    private final MainPage mainPage = new MainPage();

    @Step("Open main page")
    public void openMainPage() {
        Selenide.open(TEST_URL);
        mainPage.createTaskButton.shouldBe(visible);
    }
}
