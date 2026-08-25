import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';

const MAESTRO_RUNNING_ON_FULL_PATTERN =
  /Running on (.+?) - (iOS [\d.]+|android-[\d]+) - (\S+)/;
const MAESTRO_RUNNING_ON_SHORT_PATTERN = /Running on (\S+)/;
const GENERIC_ANDROID_DEVICE_IDS = new Set(['test', 'default']);

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

function listConnectedAdbDeviceIds(): string[] {
  const adbPath = resolveAdbPath();
  if (!adbPath) {
    return [];
  }

  const result = spawnSync(adbPath, ['devices'], { encoding: 'utf8' });
  if (result.status !== 0) {
    return [];
  }

  return result.stdout
    .split('\n')
    .slice(1)
    .map((line) => line.trim())
    .filter((line) => line.length > 0 && line.endsWith('\tdevice'))
    .map((line) => line.split('\t')[0] ?? '')
    .filter((deviceId) => deviceId.length > 0);
}

function resolveAdbDeviceId(reportedId?: string): string | undefined {
  const connected = listConnectedAdbDeviceIds();
  if (connected.length === 0) {
    return undefined;
  }

  const preferredIds = [
    process.env.MAESTRO_DEVICE,
    reportedId,
  ].filter((value): value is string => Boolean(value));

  for (const preferredId of preferredIds) {
    if (GENERIC_ANDROID_DEVICE_IDS.has(preferredId)) {
      continue;
    }
    if (connected.includes(preferredId)) {
      return preferredId;
    }
  }

  return connected[0];
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

function readEmulatorAvdName(deviceId: string): string | undefined {
  const adbPath = resolveAdbPath();
  if (!adbPath || !deviceId.startsWith('emulator-')) {
    return undefined;
  }

  const result = spawnSync(adbPath, ['-s', deviceId, 'emu', 'avd', 'name'], {
    encoding: 'utf8',
  });

  if (result.status !== 0) {
    return undefined;
  }

  const value = result.stdout.trim();
  return value.length > 0 ? value : undefined;
}

function androidPlatformLabel(deviceId: string): string {
  return deviceId.startsWith('emulator-') ? 'Android Emulator' : 'Android Device';
}

function enrichAndroidDeviceInfo(
  reportedDeviceId: string,
  info: Record<string, string>,
): Record<string, string> {
  const deviceId = resolveAdbDeviceId(reportedDeviceId) ?? reportedDeviceId;
  const model = readAdbProperty(deviceId, 'ro.product.model');
  const androidVersion = readAdbProperty(deviceId, 'ro.build.version.release');
  const apiLevel = readAdbProperty(deviceId, 'ro.build.version.sdk');
  const avdName = readEmulatorAvdName(deviceId);
  const deviceName = avdName ?? info.device ?? model;
  const osVersion =
    androidVersion && apiLevel
      ? `Android ${androidVersion} (API ${apiLevel})`
      : androidVersion
        ? `Android ${androidVersion}`
        : info.os_version;

  return {
    ...info,
    platform: androidPlatformLabel(deviceId),
    device_id: deviceId,
    ...(deviceName ? { device: deviceName } : {}),
    ...(osVersion ? { os_version: osVersion } : {}),
  };
}

export function captureConnectedAndroidDeviceInfo(): Record<string, string> | undefined {
  const deviceId = resolveAdbDeviceId();
  if (!deviceId) {
    return undefined;
  }

  return enrichAndroidDeviceInfo(deviceId, {
    platform: androidPlatformLabel(deviceId),
    device_id: deviceId,
  });
}

export function parseMaestroDeviceOutput(output: string): Record<string, string> | undefined {
  const fullMatch = output.match(MAESTRO_RUNNING_ON_FULL_PATTERN);
  if (fullMatch) {
    const [, device, osVersion, deviceId] = fullMatch;
    if (osVersion.startsWith('iOS')) {
      return {
        platform: 'iOS Simulator',
        device,
        os_version: osVersion,
        device_id: deviceId,
      };
    }

    return enrichAndroidDeviceInfo(deviceId, {
      platform: 'Android Emulator',
      device,
      os_version: osVersion.replace(/^android-/, 'API '),
      device_id: deviceId,
    });
  }

  const shortMatch = output.match(MAESTRO_RUNNING_ON_SHORT_PATTERN);
  if (!shortMatch) {
    return undefined;
  }

  const [, deviceId] = shortMatch;
  return enrichAndroidDeviceInfo(deviceId, {
    platform: androidPlatformLabel(deviceId),
    device_id: deviceId,
  });
}
