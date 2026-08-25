const MAESTRO_RUNNING_ON_FULL_PATTERN =
  /Running on (.+?) - (iOS [\d.]+|android-[\d]+) - (\S+)/;
const MAESTRO_RUNNING_ON_SHORT_PATTERN = /Running on (\S+)/;

export function parseMaestroDeviceOutput(output: string): Record<string, string> | undefined {
  const fullMatch = output.match(MAESTRO_RUNNING_ON_FULL_PATTERN);
  if (fullMatch) {
    const [, device, osVersion, deviceId] = fullMatch;
    const platform = osVersion.startsWith('iOS')
      ? 'iOS Simulator'
      : 'Android Emulator';

    return {
      platform,
      device,
      os_version: osVersion,
      device_id: deviceId,
    };
  }

  const shortMatch = output.match(MAESTRO_RUNNING_ON_SHORT_PATTERN);
  if (!shortMatch) {
    return undefined;
  }

  const [, deviceId] = shortMatch;
  const platform = deviceId.startsWith('emulator-')
    ? 'Android Emulator'
    : 'Android Device';

  return {
    platform,
    device_id: deviceId,
  };
}
