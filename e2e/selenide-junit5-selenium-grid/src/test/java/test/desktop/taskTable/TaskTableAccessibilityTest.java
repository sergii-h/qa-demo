package test.desktop.taskTable;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.DesktopTest;
import test.spec.taskTable.ITaskTableAccessibilityTest;

import static data.AllureEpic.ACCESSIBILITY;

@Epic(ACCESSIBILITY)
@Feature("Task table")
@TmsLink("98")
class TaskTableAccessibilityTest extends DesktopTest implements ITaskTableAccessibilityTest {}
