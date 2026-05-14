package util;

import com.codeborne.selenide.Selenide;
import org.awaitility.core.ConditionTimeoutException;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static com.codeborne.selenide.Condition.text;
import static org.awaitility.Awaitility.with;

public final class SelenideUtil {
    private SelenideUtil() {
    }

    public static boolean checkFor(Callable<Boolean> condition, long howLongTimeoutSec, double stepTimeoutSec) {
        try {
            with().pollInSameThread().await()
                    .atMost(Duration.ofSeconds(howLongTimeoutSec))
                    .pollInterval(Duration.ofMillis(Double.valueOf(stepTimeoutSec * 1000).longValue()))
                    .until(condition);
        } catch (ConditionTimeoutException e) {
            return false;
        }

        return true;
    }

    public static void waitForEquals(Supplier<Object> toExecute) {
        waitFor(
                () -> {
                    Object currentTry = toExecute.get();
                    return checkFor(() -> currentTry.equals(toExecute.get()), 1, 0.5);
                },
                5, 0.5
        );
    }

    private static void waitFor(Callable<Boolean> condition, long howLongTimeoutSec, double stepTimeoutSec) {
        try {
            with()
                    .pollInSameThread()
                    .await()
                    .atMost(Duration.ofSeconds(howLongTimeoutSec))
                    .pollInterval(Duration.ofMillis(Double.valueOf(stepTimeoutSec * 1000).longValue()))
                    .until(condition);
        } catch (ConditionTimeoutException | AssertionError e) {
            throwErrorWithSelenideLogger(e.getMessage());
        }
    }

    private static void throwErrorWithSelenideLogger(String exceptionMessage) {
        Selenide.$("non-existing-element").shouldHave(text(exceptionMessage), Duration.ofMillis(1));
    }
}
