package test.mobile.translation;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import test.base.MobileTest;
import test.spec.translation.ILanguageSupportTest;

import static data.AllureEpic.TRANSLATION;

@Epic(TRANSLATION)
@Feature("Language support")
@TmsLink("104")
class LanguageSupportTest extends MobileTest implements ILanguageSupportTest {}
