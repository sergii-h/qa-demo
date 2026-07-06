import { ExpoConfig, ConfigContext } from 'expo/config';
import { AndroidConfig, ConfigPlugin, withAndroidManifest } from 'expo/config-plugins';

const materialCommunityIconsFont =
  'node_modules/@expo/vector-icons/build/vendor/react-native-vector-icons/Fonts/MaterialCommunityIcons.ttf';

const withCleartextTraffic: ConfigPlugin = (config) =>
  withAndroidManifest(config, (config) => {
    const application = AndroidConfig.Manifest.getMainApplicationOrThrow(config.modResults);
    application.$['android:usesCleartextTraffic'] = 'true';
    return config;
  });

export default ({ config }: ConfigContext): ExpoConfig => {
  const plugins = (config.plugins ?? []).filter((plugin) => plugin !== 'expo-font');

  plugins.push([
    'expo-font',
    {
      fonts: [materialCommunityIconsFont],
    },
  ]);
  plugins.push(withCleartextTraffic as unknown as (typeof plugins)[number]);

  return {
    ...config,
    name: 'QA Demo Tasks',
    slug: 'demo-react-native',
    ios: {
      ...config.ios,
      infoPlist: {
        ...(config.ios?.infoPlist ?? {}),
        NSAppTransportSecurity: {
          NSAllowsLocalNetworking: true,
        },
      },
    },
    android: {
      ...config.android,
    },
    plugins,
    extra: {
      apiBaseUrl: process.env.API_BASE_URL,
    },
  };
};
