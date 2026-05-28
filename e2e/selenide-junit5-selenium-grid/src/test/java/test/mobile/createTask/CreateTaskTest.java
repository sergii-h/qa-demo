package test.mobile.createTask;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.MobileTest;
import test.spec.createTask.ICreateTaskTest;

import static data.AllureEpic.TASK_MANAGEMENT;

@Epic(TASK_MANAGEMENT)
@Feature("Create task")
@TmsLink("100")
class CreateTaskTest extends MobileTest implements ICreateTaskTest {}
