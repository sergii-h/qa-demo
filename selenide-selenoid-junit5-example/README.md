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

###### Debug from IDE
Extend JUnit template configuration VM options with `-Dtest.env=local` tests will be started using local chrome browser without Selenoid
