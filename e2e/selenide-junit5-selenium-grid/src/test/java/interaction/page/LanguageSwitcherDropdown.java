package interaction.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class LanguageSwitcherDropdown {
    public SelenideElement dropdown = $("[data-testid='language-switcher']");
    public SelenideElement dropdownValue = dropdown.$("span.p-dropdown-label");
    public ElementsCollection items = $$(".p-dropdown-item");
}
