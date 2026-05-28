package test.mobile.taskTable;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.MobileTest;
import test.spec.taskTable.ITaskTableAccessibilityTest;

import static data.AllureEpic.ACCESSIBILITY;

@Epic(ACCESSIBILITY)
@Feature("Task table")
@TmsLink("98")
class TaskTableAccessibilityTest extends MobileTest implements ITaskTableAccessibilityTest {}
