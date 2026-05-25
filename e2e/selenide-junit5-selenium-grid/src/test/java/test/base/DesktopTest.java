package test.base;

import static data.Platform.DESKTOP;

public abstract class DesktopTest extends TestBase {

    public DesktopTest() {
        super(DESKTOP);
    }

    @Override
    public void setUpByPlatform() {
    }
}
