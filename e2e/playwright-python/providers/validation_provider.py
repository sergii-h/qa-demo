from playwright.sync_api import Page
from interactions.validators.tasks_validator import TasksValidator
from interactions.validators.task_validator import TaskValidator
from interactions.validators.language_validator import LanguageValidator
from interactions.validators.accessibility_validator import AccessibilityValidator


class ValidationProvider:
    def __init__(self, page: Page, is_mobile: bool):
        self.tasks = TasksValidator(page)
        self.task = TaskValidator(page)
        self.language = LanguageValidator(page)
        self.accessibility = AccessibilityValidator()
