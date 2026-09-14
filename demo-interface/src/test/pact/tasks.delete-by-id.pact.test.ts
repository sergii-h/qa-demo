// @vitest-environment node
import { MatchersV3 } from "@pact-foundation/pact";
import { deleteTask } from "../../services";
import { createPact, TASK_ID } from "./tasks.pact.fixtures";

const { fromProviderState } = MatchersV3;

const pact = createPact("demo-service-tasks-delete");

const taskPath = fromProviderState(`/v1/tasks/\${taskId}`, `/v1/tasks/${TASK_ID}`);

describe("tasks DELETE /v1/tasks/{id} pact", () => {
  it("should have delete task contract when deleting task by id", async () => {
    await pact
      .addInteraction()
      .given("a task exists to delete")
      .uponReceiving("a request to delete a task")
      .withRequest("DELETE", taskPath, (req) => {
        req.headers({ "Content-Type": "application/json" });
      })
      .willRespondWith(204)
      .executeTest(async (mockServer) => {
        await deleteTask(TASK_ID, `${mockServer.url}/v1`);
      });
  });
});
