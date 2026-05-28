package test.mobile.taskInfo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.MobileTest;
import test.spec.taskInfo.ITaskInfoAccessibilityTest;

import static data.AllureEpic.ACCESSIBILITY;

@Epic(ACCESSIBILITY)
@Feature("View task info")
@TmsLink("102")
class TaskInfoAccessibilityTest extends MobileTest implements ITaskInfoAccessibilityTest {}
