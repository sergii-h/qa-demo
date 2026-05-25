# selenide + selenium-grid example tests

###### Test structure

```
test/
├── base/     # TestBase, DesktopTest, MobileTest — browser setup & lifecycle
├── spec/     # *Tests interfaces — shared test logic (mocked BE, a11y, UAT)
├── desktop/  # thin JUnit classes that run specs on desktop
└── mobile/   # thin JUnit classes that run specs on mobile
```

Shared behaviour lives in `spec/`; `desktop/` and `mobile/` only pick the platform base class.

###### Tests
##### Configuration
`.src/test/java/resources/test.properties`

Test configuration from test.properties can be overridden by sending properties from command line.

It is recommended to use custom properties file, for example `-Dproperties.file.name=test.local.properties`

test.env values:
- local - tests will be started in local chrome browser without Selenium Grid
- docker - tests will be started via Selenium Grid running in Docker
- remote - to run tests with GitHub Actions

##### Run with maven
- e2e with mocked BE (frontend only) `mvn -Pe2e -Dproperties.file.name=test.local.properties test`
- accessibility with mocked BE (frontend only) `mvn -Paccessibility -Dproperties.file.name=test.local.properties test`
- UAT smoke test against real backend (full app) `mvn -Puat -Dproperties.file.name=test.local.properties test`
- with command line properties `mvn -Dtest.env=remote -Dtest.parallel=false -Dtest.include="**/desktop/**" clean test`

##### Run/Debug with IDE
- start application in background `docker compose -f docker/docker-compose/run-application.yml up -d`
- extend run configuration with `properties.file.name=test.local.properties` env variable (Intellij - extend Junit run configuration, Vscode - add vmArgs to java.test.config in settings.json)
- set `test.env=local` in `test.local.properties` file

###### Run tests with Selenium Grid in Docker
- create shared Docker network (one-time setup): `docker network create qa-demo-e2e`
- start application in background: `docker compose -f docker/docker-compose/run-application.yml up -d`
- start Selenium Grid in background (tests run inside Docker):
  `SE_NODE_GRID_URL=http://qa-demo-selenium-hub:4444 docker compose -f docker/docker-compose/run-selenium-grid.yml up -d`
- run tests: `docker compose -f docker/docker-compose/run-tests-selenide-junit5-selenium-grid.yml up`
- open test-report (allure reporter should be installed first): `rm -rf allure-results && docker cp qa-demo-selenide-junit5-selenium-grid-tests:target/allure-results allure-results && allure serve allure-results`
