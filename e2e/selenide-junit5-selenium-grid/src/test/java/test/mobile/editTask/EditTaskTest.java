package test.mobile.editTask;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.MobileTest;
import test.spec.editTask.IEditTaskTest;

import static data.AllureEpic.TASK_MANAGEMENT;

@Epic(TASK_MANAGEMENT)
@Feature("Edit task")
@TmsLink("101")
class EditTaskTest extends MobileTest implements IEditTaskTest {}
