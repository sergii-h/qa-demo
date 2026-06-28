import { ExpoConfig, ConfigContext } from 'expo/config';

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,
  name: 'QA Demo Tasks',
  slug: 'demo-react-native',
  extra: {
    apiBaseUrl: process.env.API_BASE_URL,
  },
});
