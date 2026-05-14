// @vitest-environment node
import { MatchersV3 } from "@pact-foundation/pact";
import { getTasks } from "../../services";
import { TaskPriority, TaskStatus } from "../../interfaces/ITask";
import { createPact } from "./tasks.pact.fixtures";

const { like, regex, eachLike } = MatchersV3;

const pact = createPact("demo-service-tasks-get-all");
const timestampPattern = "^\\d{4}-\\d{2}-\\d{2}T.*$";

describe("tasks GET /v1/tasks pact", () => {
  it("should have get tasks contract when requesting all tasks", async () => {
    await pact
      .addInteraction()
      .given("tasks exist")
      .uponReceiving("a request for all tasks")
      .withRequest("GET", "/v1/tasks", (req) => {
        req.headers({ "Content-Type": "application/json" });
      })
      .willRespondWith(200, (res) => {
        res.headers({ "Content-Type": "application/json" });
        res.jsonBody(eachLike({
          id: regex("^[a-f0-9]{24}$", "507f1f77bcf86cd799439011"),
          title: like("Prepare release notes"),
          description: like("Document release tasks"),
          status: like(TaskStatus.TODO),
          priority: like(TaskPriority.MEDIUM),
          createdDate: regex(timestampPattern, "2026-04-26T09:00:00.000Z"),
          updatedDate: regex(timestampPattern, "2026-04-26T09:00:00.000Z"),
        }, 1));
      })
      .executeTest(async (mockServer) => {
        await getTasks(`${mockServer.url}/v1`);
      });
  });
});
