import path from "node:path";
import { MatchersV3, PactV4, SpecificationVersion } from "@pact-foundation/pact";
import ITask from "../../interfaces/ITask";

const { like, regex } = MatchersV3;

export const TASK_ID = "507f1f77bcf86cd799439011";
export const timestampPattern = "^\\d{4}-\\d{2}-\\d{2}T.*$";

export function likeTask(task: Required<ITask>): { [K in keyof Required<ITask>]: unknown } {
  return {
    id: regex("^[a-f0-9]{24}$", task.id),
    title: like(task.title),
    description: like(task.description),
    status: like(task.status),
    priority: like(task.priority),
    createdDate: regex(timestampPattern, task.createdDate),
    updatedDate: regex(timestampPattern, task.updatedDate),
  };
}

// PactV4 DSL is used for its correct path-generator serialisation, but the output spec
// is pinned to V3 so the pact file uses plain JSON bodies. pact-jvm 4.6.17 cannot parse
// Pact V4's base64-encoded opaque body format and silently drops those interactions.
export const createPact = (provider: string) =>
  new PactV4({
    consumer: "demo-interface",
    provider,
    dir: path.resolve(process.cwd(), "pacts"),
    spec: SpecificationVersion.SPECIFICATION_VERSION_V3,
  });
