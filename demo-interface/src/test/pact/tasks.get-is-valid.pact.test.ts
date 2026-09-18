// @vitest-environment node
import { MatchersV3 } from "@pact-foundation/pact";
import { getIsValid } from "../../services";
import { createPact, TASK_ID } from "./tasks.pact.fixtures";

const { boolean, fromProviderState } = MatchersV3;

const pact = createPact("demo-service-tasks-get-is-valid");

const isValidPath = fromProviderState(`/v1/tasks/isValid/\${taskId}`, `/v1/tasks/isValid/${TASK_ID}`);

describe("tasks GET /v1/tasks/isValid/{id} pact", () => {
  it("should have get is valid contract", async () => {
    const isValid: boolean = true;

    await pact
      .addInteraction()
      .given("validation result is true for the task")
      .uponReceiving("a request for task validation status")
      .withRequest("GET", isValidPath, (req) => {
        req.headers({ "Content-Type": "application/json" });
      })
      .willRespondWith(200, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody(boolean(isValid));
      })
      .executeTest(async (mockServer) => {
        await getIsValid(TASK_ID, `${mockServer.url}/v1`);
      });
  });
});
