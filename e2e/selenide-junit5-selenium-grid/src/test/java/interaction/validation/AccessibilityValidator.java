package interaction.validation;

import com.deque.html.axecore.results.Results;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class AccessibilityValidator {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Step("Validate no accessibility violations")
    public void hasNoViolations(Results results) {
        attachAxeReport(results);
        assertThat("Expected no accessibility violations", results.getViolations(), empty());
    }

    private static void attachAxeReport(Results results) {
        try {
            Allure.addAttachment(
                    "Axe accessibility report",
                    "application/json",
                    OBJECT_MAPPER.writeValueAsString(results.getViolations())
            );
        } catch (JsonProcessingException e) {
            throw new InternalError(e);
        }
    }
}
