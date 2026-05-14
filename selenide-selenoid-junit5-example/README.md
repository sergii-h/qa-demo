# selenide + selenoid example tests
###### Selenoid
- selenoid start: `./cm selenoid start --args "-limit 4" --browsers-json browsers.json`
- selenoid-ui start with port: `./cm selenoid-ui start --port 8081`

###### Tests
##### Configuration
`.src/test/java/resources/test.properties`

Test configuration from test.properties can be overridden by sending properties from command line.

It is recommended to use custom properties file, for example `-Dproperties.file.name=test.local.properties`

test.env values:
- local - tests will be started in local chrome browser without Selenoid
- docker - tests will be started in docker containers managed by Selenoid
- remote - to run tests with GitHub actions

##### Run with maven
- with custom properties file `mvn -Dproperties.file.name=test.local.properties clean test`
- with command line properties `mvn -Dtest.env=remote -Dtest.parallel=false -Dtest.include="**/d*/P*.java" clean test`

##### Run/Debug with IDE
- start application in background `docker compose -f docker/docker-compose-run-application.yml up -d`
- extend run configuration with `properties.file.name=test.local.properties` env variable (Intellij - extend Junit run configuration, Vscode - add vmArgs to java.test.config in settings.json)
- set `test.env=local` in `test.local.properties` file

###### Run tests with selenoid in dockers
- start application in background `docker-compose -f docker/docker-compose-run-application.yml up -d`
- start selenoid in background `docker-compose -f docker/docker-compose-run-selenoid.yml up -d`
- run tests `docker-compose -f docker/docker-compose-run-selenide-junit5-tests.yml up`
- open test-report (allure reporter should be installed first) `docker cp qa-demo-selenide-junit5-tests:target/allure-results allure-results && allure serve allure-results`

###### Arm processors specific options
- reset DOCKER_DEFAULT_PLATFORM variable before test-env run: `export DOCKER_DEFAULT_PLATFORM=`
- update "image" parameter in browsers.json to `dumbdumbych/selenium_vnc_chrome_arm64:91.0.b` arm64 chrome image
