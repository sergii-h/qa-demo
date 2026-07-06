import { execSync } from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';

export function resolveMaestroCli(): string {
  const candidates = [
    process.env.MAESTRO_BIN,
    path.join(os.homedir(), '.maestro', 'bin', 'maestro'),
    process.env.MAESTRO_HOME
      ? path.join(process.env.MAESTRO_HOME, 'bin', 'maestro')
      : undefined,
  ].filter((value): value is string => Boolean(value));

  for (const candidate of candidates) {
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }

  try {
    execSync('command -v maestro', { stdio: 'pipe' });
    return 'maestro';
  } catch {
    throw new Error(
      'Maestro CLI not found. Install it and ensure it is on PATH, or set MAESTRO_BIN:\n\n' +
        '  curl -Ls "https://get.maestro.mobile.dev" | bash\n' +
        '  export PATH="$HOME/.maestro/bin:$PATH"\n',
    );
  }
}
