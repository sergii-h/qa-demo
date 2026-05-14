// @vitest-environment node
import { MatchersV3 } from "@pact-foundation/pact";
import { deleteTask } from "../../services";
import { createPact } from "./tasks.pact.fixtures";

const { fromProviderState } = MatchersV3;

const pact = createPact("demo-service-tasks-delete");
const taskId = "507f1f77bcf86cd799439011";

const taskPath = fromProviderState(`/v1/tasks/\${taskId}`, `/v1/tasks/${taskId}`);

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
        await deleteTask(taskId, `${mockServer.url}/v1`);
      });
  });
});
