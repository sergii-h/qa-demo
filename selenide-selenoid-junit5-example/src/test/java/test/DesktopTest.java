package test;

import static data.Platform.DESKTOP;

public abstract class DesktopTest extends TestBase {

    public DesktopTest() {
        super(DESKTOP);
    }

    @Override
    public void setUpByPlatform() {
        System.setProperty("chromeoptions.args", "--remote-allow-origins=*");
    }
}
