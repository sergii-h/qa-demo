package support.mock;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.WebDriver;

public class ApiRouteMockClient implements AutoCloseable {
    private ApiRouteMock apiRouteMock;

    public ApiRouteMock api() {
        if (apiRouteMock == null) {
            WebDriver driver = WebDriverRunner.getWebDriver();
            apiRouteMock = new ApiRouteMock(driver);
        }
        return apiRouteMock;
    }

    public void reset() {
        close();
    }

    @Override
    public void close() {
        if (apiRouteMock != null) {
            apiRouteMock.close();
            apiRouteMock = null;
        }
    }
}
