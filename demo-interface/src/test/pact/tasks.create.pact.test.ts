// @vitest-environment node
import { MatchersV3 } from "@pact-foundation/pact";
import { createTask } from "../../services";
import ITask, { TaskPriority, TaskStatus, TaskWrite } from "../../interfaces/ITask";
import { IMessageErrorResponse } from "../../interfaces/IApiError";
import { createPact, likeTask, TASK_ID } from "./tasks.pact.fixtures";

const { regex, fromProviderState } = MatchersV3;

const pact = createPact("demo-service-tasks-create");

const task: Required<TaskWrite> = {
  title: "Prepare release notes",
  description: "Document release tasks",
  status: TaskStatus.TODO,
  priority: TaskPriority.MEDIUM,
};

const created: Required<ITask> = {
  id: TASK_ID,
  title: task.title,
  description: task.description,
  status: task.status,
  priority: task.priority,
  createdDate: "2026-04-26T09:00:00.000Z",
  updatedDate: "2026-04-26T09:00:00.000Z",
};

const requestBody: { [K in keyof Required<TaskWrite>]: unknown } = {
  title: fromProviderState("${taskTitle}", task.title),
  description: task.description,
  status: task.status,
  priority: task.priority,
};

describe("tasks POST /v1/tasks pact", () => {
  it("should have create task success contract when posting valid task", async () => {
    await pact
      .addInteraction()
      .given("task title is unique")
      .uponReceiving("a valid task creation request")
      .withRequest("POST", "/v1/tasks", (req) => {
        req.headers({ "Content-Type": "application/json" });
        req.jsonBody(requestBody);
      })
      .willRespondWith(201, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody(likeTask(created));
      })
      .executeTest(async (mockServer) => {
        await createTask(task, `${mockServer.url}/v1`);
      });
  });

  it("should have create task duplicate contract when posting duplicate title", async () => {
    const error: IMessageErrorResponse = {
      message: "Task with title 'Prepare release notes' already exists",
    };
    const errorBody: { [K in keyof IMessageErrorResponse]: unknown } = {
      message: regex("^Task with title '.*' already exists$", error.message),
    };

    await pact
      .addInteraction()
      .given("task title already exists")
      .uponReceiving("a task creation request with duplicate title")
      .withRequest("POST", "/v1/tasks", (req) => {
        req.headers({ "Content-Type": "application/json" });
        req.jsonBody(requestBody);
      })
      .willRespondWith(409, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody(errorBody);
      })
      .executeTest(async (mockServer) => {
        await expect(
          createTask(task, `${mockServer.url}/v1`)
        ).rejects.toThrow("already exists");
      });
  });
});
