package interaction.step;

import com.codeborne.selenide.WebDriverRunner;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.selenium.AxeBuilder;
import io.qameta.allure.Step;

public class AccessibilityStep {

    @Step("Analyze page accessibility with axe")
    public Results analyze() {
        return new AxeBuilder().analyze(WebDriverRunner.getWebDriver());
    }
}
