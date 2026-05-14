// @vitest-environment node
import { MatchersV3 } from "@pact-foundation/pact";
import { createTask } from "../../services";
import ITask,{ TaskPriority, TaskStatus } from "../../interfaces/ITask";
import { createPact } from "./tasks.pact.fixtures";

const { like, regex, fromProviderState, extractPayload } = MatchersV3;

const pact = createPact("demo-service-tasks-create");
const timestampPattern = "^\\d{4}-\\d{2}-\\d{2}T.*$";

const requestContract = {
  title: fromProviderState("${taskTitle}", "Prepare release notes"),
  description: "Document release tasks",
  status: TaskStatus.TODO,
  priority: TaskPriority.MEDIUM,
};

describe("tasks POST /v1/tasks pact", () => {
  it("should have create task success contract when posting valid task", async () => {
    await pact
      .addInteraction()
      .given("task title is unique")
      .uponReceiving("a valid task creation request")
      .withRequest("POST", "/v1/tasks", (req) => {
        req.headers({ "Content-Type": "application/json" });
        req.jsonBody(requestContract);
      })
      .willRespondWith(201, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody({
          id: regex("^[a-f0-9]{24}$", "507f1f77bcf86cd799439011"),
          title: like("Prepare release notes"),
          description: like("Document release tasks"),
          status: like(TaskStatus.TODO),
          priority: like(TaskPriority.MEDIUM),
          createdDate: regex(timestampPattern, "2026-04-26T09:00:00.000Z"),
          updatedDate: regex(timestampPattern, "2026-04-26T09:00:00.000Z"),
        });
      })
      .executeTest(async (mockServer) => {
        await createTask(extractPayload(requestContract) as unknown as ITask, `${mockServer.url}/v1`);
      });
  });

  it("should have create task duplicate contract when posting duplicate title", async () => {
    await pact
      .addInteraction()
      .given("task title already exists")
      .uponReceiving("a task creation request with duplicate title")
      .withRequest("POST", "/v1/tasks", (req) => {
        req.headers({ "Content-Type": "application/json" });
        req.jsonBody(requestContract);
      })
      .willRespondWith(409, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody({
          message: regex("^Task with title '.*' already exists$", "Task with title 'Prepare release notes' already exists"),
        });
      })
      .executeTest(async (mockServer) => {
        await expect(
          createTask(extractPayload(requestContract) as unknown as ITask, `${mockServer.url}/v1`)
        ).rejects.toThrow("already exists");
      });
  });
});
