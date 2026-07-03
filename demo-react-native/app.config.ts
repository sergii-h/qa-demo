import { ExpoConfig, ConfigContext } from 'expo/config';

const materialCommunityIconsFont =
  'node_modules/@expo/vector-icons/build/vendor/react-native-vector-icons/Fonts/MaterialCommunityIcons.ttf';

export default ({ config }: ConfigContext): ExpoConfig => {
  const plugins = (config.plugins ?? []).filter((plugin) => plugin !== 'expo-font');

  plugins.push([
    'expo-font',
    {
      fonts: [materialCommunityIconsFont],
    },
  ]);

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
      usesCleartextTraffic: true,
    },
    plugins,
    extra: {
      apiBaseUrl: process.env.API_BASE_URL,
    },
  };
};
