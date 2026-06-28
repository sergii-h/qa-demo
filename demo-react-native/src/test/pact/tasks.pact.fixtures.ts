import path from 'node:path';
import {PactV4, SpecificationVersion} from '@pact-foundation/pact';

export const createPact = (provider: string) =>
  new PactV4({
    consumer: 'demo-react-native',
    provider,
    dir: path.resolve(process.cwd(), 'pacts'),
    spec: SpecificationVersion.SPECIFICATION_VERSION_V3,
  });
