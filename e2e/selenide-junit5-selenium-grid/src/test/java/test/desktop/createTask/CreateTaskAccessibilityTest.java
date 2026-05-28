package test.desktop.createTask;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.DesktopTest;
import test.spec.createTask.ICreateTaskAccessibilityTest;

import static data.AllureEpic.ACCESSIBILITY;

@Epic(ACCESSIBILITY)
@Feature("Create task")
@TmsLink("100")
class CreateTaskAccessibilityTest extends DesktopTest implements ICreateTaskAccessibilityTest {}
