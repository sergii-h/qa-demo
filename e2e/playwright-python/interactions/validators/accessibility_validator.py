import json
import allure
from decorators.step_decorator import step


class AccessibilityValidator:
    @step("Validate no accessibility violations")
    def has_no_violations(self, results) -> None:
        violations = results.response.get("violations", [])
        allure.attach(
            json.dumps(violations, indent=2),
            name="Axe accessibility report",
            attachment_type=allure.attachment_type.JSON,
        )
        assert results.violations_count == 0, (
            f"Expected no accessibility violations, found {results.violations_count}:\n"
            f"{results.generate_report()}"
        )
