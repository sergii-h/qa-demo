from playwright.sync_api import Page
from interactions.steps.navigation_step import NavigationStep
from interactions.steps.task_table_step import TaskTableStep
from interactions.steps.language_switcher_step import LanguageSwitcherStep
from interactions.steps.accessibility_step import AccessibilityStep


class StepProvider:
    def __init__(self, page: Page, is_mobile: bool):
        self.navigation = NavigationStep(page)
        self.tasks = TaskTableStep(page)
        self.language = LanguageSwitcherStep(page)
        self.accessibility = AccessibilityStep(page)
