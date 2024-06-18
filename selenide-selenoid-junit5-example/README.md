# selenide + selenoid example tests
###### Selenoid
- selenoid start: `./cm selenoid start --args "-limit 4" --browsers-json browsers.json`
- selenoid-ui start with port: `./cm selenoid-ui start --port 8081`

###### Tests
- `mvn clean test`

Options:
- `test.env` env alias, 'local' by default (for more info see data.Environment class)
- `test.parallel` run tests in parallel, true by default
- `test.enableVNC` run tests with video recordings, false by default (works only with Selenoid)
- `test.tags` include tests to run using junit5, no filter by default
- `test.ignore.tags` exclude tests from run using junit5, no filter by default 
- `test.include` include tests to run using file paths, no filter by default
  `test.exclude` exclude tests from run using file paths, no filter by default

Example:
- `mvn -Dtest.parallel=false -Dtest.enableVNC=true -Dtest.include="**/d*/C*.java" clean test`

###### Run/Debug tests with IDE
Extend JUnit template configuration VM options with `-Dtest.env=local` tests will be started using local chrome browser without Selenoid

###### Run tests with selenoid in dockers
- start application in background `docker-compose -f docker/docker-compose-run-application.yml up -d`
- start selenoid in background `docker-compose -f docker/docker-compose-run-selenoid.yml up -d`
- run tests `docker-compose -f docker/docker-compose-run-selenide-junit5-tests.yml up`
- open test-report (allure reporter should be installed first) `docker cp qa-demo-selenide-junit5-tests:target/allure-results allure-results && allure serve allure-results`
- shutdown test-env `docker-compose -f docker/docker-compose-run-selenide-junit5-tests.yml down && docker-compose -f docker/docker-compose-run-selenoid.yml down -v && docker-compose -f docker/docker-compose-run-application.yml down`

###### Arm processors specific options
- reset DOCKER_DEFAULT_PLATFORM variable before test-env run: `export DOCKER_DEFAULT_PLATFORM=`
- update "image" parameter in browsers.json to `dumbdumbych/selenium_vnc_chrome_arm64:91.0.b` arm64 chrome image
