package interaction.step;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;

public class NavigationStep {

    @Step("Refresh page")
    public void refresh() {
        Selenide.refresh();
    }
}
