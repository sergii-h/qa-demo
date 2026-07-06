import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';

const MAESTRO_RUNNING_ON_FULL_PATTERN =
  /Running on (.+?) - (iOS [\d.]+|android-[\d]+) - (\S+)/;
const MAESTRO_RUNNING_ON_SHORT_PATTERN = /Running on (\S+)/;

function resolveAdbPath(): string | undefined {
  const fromPath = spawnSync('which', ['adb'], { encoding: 'utf8' });
  if (fromPath.status === 0) {
    const adbPath = fromPath.stdout.trim();
    if (adbPath.length > 0) {
      return adbPath;
    }
  }

  const sdkRoots = [
    process.env.ANDROID_HOME,
    process.env.ANDROID_SDK_ROOT,
    path.join(os.homedir(), 'Library', 'Android', 'sdk'),
    path.join(os.homedir(), 'Android', 'Sdk'),
  ].filter((value): value is string => Boolean(value));

  for (const sdkRoot of sdkRoots) {
    const adbPath = path.join(sdkRoot, 'platform-tools', 'adb');
    if (fs.existsSync(adbPath)) {
      return adbPath;
    }
  }

  return undefined;
}

function readAdbProperty(deviceId: string, property: string): string | undefined {
  const adbPath = resolveAdbPath();
  if (!adbPath) {
    return undefined;
  }

  const result = spawnSync(adbPath, ['-s', deviceId, 'shell', 'getprop', property], {
    encoding: 'utf8',
  });

  if (result.status !== 0) {
    return undefined;
  }

  const value = result.stdout.trim();
  return value.length > 0 ? value : undefined;
}

function enrichAndroidDeviceInfo(
  deviceId: string,
  info: Record<string, string>,
): Record<string, string> {
  const model = readAdbProperty(deviceId, 'ro.product.model');
  const androidVersion = readAdbProperty(deviceId, 'ro.build.version.release');
  const apiLevel = readAdbProperty(deviceId, 'ro.build.version.sdk');

  return {
    ...info,
    ...(model ? { device: model } : {}),
    ...(androidVersion
      ? { os_version: apiLevel ? `Android ${androidVersion} (API ${apiLevel})` : `Android ${androidVersion}` }
      : {}),
  };
}

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

  return enrichAndroidDeviceInfo(deviceId, {
    platform,
    device_id: deviceId,
  });
}
