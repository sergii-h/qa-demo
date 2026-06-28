import {act} from '@testing-library/react-native';

export function runHookAction(action: () => void): void {
  act(action);
}

export async function runHookActionAsync(action: () => Promise<void>): Promise<void> {
  await act(async () => {
    await action();
  });
}
