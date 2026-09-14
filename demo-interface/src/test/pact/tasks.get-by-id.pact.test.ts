// @vitest-environment node
import { MatchersV3 } from "@pact-foundation/pact";
import { getTask } from "../../services";
import ITask, { TaskPriority, TaskStatus } from "../../interfaces/ITask";
import { createPact, likeTask, TASK_ID } from "./tasks.pact.fixtures";

const { fromProviderState } = MatchersV3;

const pact = createPact("demo-service-tasks-get-by-id");

const taskPath = fromProviderState(`/v1/tasks/\${taskId}`, `/v1/tasks/${TASK_ID}`);

describe("tasks GET /v1/tasks/{id} pact", () => {
  it("should have get task by id contract when requesting task details", async () => {
    const task: Required<ITask> = {
      id: TASK_ID,
      title: "Prepare release notes",
      description: "Document release tasks",
      status: TaskStatus.TODO,
      priority: TaskPriority.MEDIUM,
      createdDate: "2024-01-15T10:00:00.000Z",
      updatedDate: "2024-01-16T12:00:00.000Z",
    };

    await pact
      .addInteraction()
      .given("a task exists")
      .uponReceiving("a request for a task by id")
      .withRequest("GET", taskPath, (req) => {
        req.headers({ "Content-Type": "application/json" });
      })
      .willRespondWith(200, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody(likeTask(task));
      })
      .executeTest(async (mockServer) => {
        await getTask(TASK_ID, `${mockServer.url}/v1`);
      });
  });
});
