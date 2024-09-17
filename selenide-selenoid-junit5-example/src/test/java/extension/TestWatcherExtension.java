package extension;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Optional;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class TestWatcherExtension implements TestWatcher {

    @Step("Get full page screenshot")
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (WebDriverRunner.hasWebDriverStarted()) {
            takeFullPageScreenshot();
            WebDriverRunner.closeWindow();
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        if (WebDriverRunner.hasWebDriverStarted()) {
            WebDriverRunner.closeWindow();
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        if (WebDriverRunner.hasWebDriverStarted()) {
            WebDriverRunner.closeWindow();
        }
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        if (WebDriverRunner.hasWebDriverStarted()) {
            WebDriverRunner.closeWindow();
        }
    }

    private void takeFullPageScreenshot() {
        takeFullPageScreenshot(0);
    }

    private void takeFullPageScreenshot(long stickyHeaderHeight) {
        Selenide.switchTo().defaultContent();

        long pageHeight = Objects.requireNonNull(executeJavaScript("return document.documentElement.scrollHeight"));
        long clientHeight = Objects.requireNonNull(executeJavaScript("return document.documentElement.clientHeight"));

        clientHeight = clientHeight - stickyHeaderHeight;

        if (pageHeight * 1.0 / clientHeight <= 1) {
            addScreenshot("screenshot");
            return;
        }

        for (int i = 0; i < pageHeight / clientHeight + 1; i++) {
            executeJavaScript("window.scrollTo(0," + (clientHeight * i) + ")");
            addScreenshot("screenshot-" + (i + 1));
        }
    }

    private void addScreenshot(String screenshotName) {
        try {
            Allure.addAttachment(
                    screenshotName,
                    new FileInputStream(Objects.requireNonNull(Selenide.screenshot(OutputType.FILE)))
            );
        } catch (FileNotFoundException e) {
            throw new InternalError(e);
        }
    }
}
