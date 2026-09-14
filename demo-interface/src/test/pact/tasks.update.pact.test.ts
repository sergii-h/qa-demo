// @vitest-environment node
import { MatchersV3 } from "@pact-foundation/pact";
import { updateTask } from "../../services";
import ITask, { TaskPriority, TaskStatus, TaskWrite } from "../../interfaces/ITask";
import { IMessageErrorResponse } from "../../interfaces/IApiError";
import { createPact, likeTask, TASK_ID } from "./tasks.pact.fixtures";

const { regex, fromProviderState } = MatchersV3;

const pact = createPact("demo-service-tasks-update");

const taskPath = fromProviderState(`/v1/tasks/\${taskId}`, `/v1/tasks/${TASK_ID}`);

const task: Required<ITask> = {
  id: TASK_ID,
  title: "Prepare release notes - updated",
  description: "Document release tasks in detail",
  status: TaskStatus.IN_PROGRESS,
  priority: TaskPriority.HIGH,
  createdDate: "2026-04-26T09:00:00.000Z",
  updatedDate: "2026-04-26T10:30:00.000Z",
};

const requestBody: { [K in keyof Required<TaskWrite>]: unknown } = {
  title: fromProviderState("${updatedTitle}", task.title),
  description: task.description,
  status: task.status,
  priority: task.priority,
};

describe("tasks PUT /v1/tasks/{id} pact", () => {
  it("should have update task success contract when updating task", async () => {
    await pact
      .addInteraction()
      .given("a task exists to update and title is unique")
      .uponReceiving("a valid task update request")
      .withRequest("PUT", taskPath, (req) => {
        req.headers({ "Content-Type": "application/json" });
        req.jsonBody(requestBody);
      })
      .willRespondWith(200, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody(likeTask(task));
      })
      .executeTest(async (mockServer) => {
        await updateTask(task.id, task, `${mockServer.url}/v1`);
      });
  });

  it("should have update task duplicate contract when updating with duplicate title", async () => {
    const error: IMessageErrorResponse = {
      message: `Task with title '${task.title}' already exists`,
    };
    const errorBody: { [K in keyof IMessageErrorResponse]: unknown } = {
      message: regex("^Task with title '.*' already exists$", error.message),
    };

    await pact
      .addInteraction()
      .given("another task has the requested title")
      .uponReceiving("a task update request with duplicate title")
      .withRequest("PUT", taskPath, (req) => {
        req.headers({ "Content-Type": "application/json" });
        req.jsonBody(requestBody);
      })
      .willRespondWith(409, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody(errorBody);
      })
      .executeTest(async (mockServer) => {
        await expect(
          updateTask(task.id, task, `${mockServer.url}/v1`)
        ).rejects.toThrow("already exists");
      });
  });
});
