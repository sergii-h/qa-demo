import path from "node:path";
import { PactV4, SpecificationVersion } from "@pact-foundation/pact";

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
