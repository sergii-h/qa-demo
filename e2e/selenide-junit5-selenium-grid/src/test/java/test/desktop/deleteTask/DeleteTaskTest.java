package test.desktop.deleteTask;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.DesktopTest;
import test.spec.deleteTask.IDeleteTaskTest;

import static data.AllureEpic.TASK_MANAGEMENT;

@Epic(TASK_MANAGEMENT)
@Feature("Delete task")
@TmsLink("99")
class DeleteTaskTest extends DesktopTest implements IDeleteTaskTest {}
