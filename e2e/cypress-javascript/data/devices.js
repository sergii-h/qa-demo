const DEVICES = {
  desktop: {
    label: 'Desktop Chrome',
    viewportWidth: 1280,
    viewportHeight: 720,
  },
  mobile: {
    label: 'Mobile (iPhone 12 Pro viewport)',
    viewportWidth: 390,
    viewportHeight: 844,
    userAgent:
      'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1',
  },
};

const resolveDevice = (deviceName = 'desktop') => DEVICES[deviceName] ?? DEVICES.desktop;

module.exports = { DEVICES, resolveDevice };
