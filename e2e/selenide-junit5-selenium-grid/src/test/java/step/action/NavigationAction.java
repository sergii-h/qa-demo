package step.action;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;

public class NavigationAction {

    @Step("Refresh page")
    public void refresh() {
        Selenide.refresh();
    }
}
