package test.mobile.taskInfo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.MobileTest;
import test.spec.taskInfo.ITaskInfoTest;

import static data.AllureEpic.TASK_MANAGEMENT;

@Epic(TASK_MANAGEMENT)
@Feature("View task info")
@TmsLink("102")
class TaskInfoTest extends MobileTest implements ITaskInfoTest {}
