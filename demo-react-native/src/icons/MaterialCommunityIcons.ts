import { Platform } from 'react-native';
import { MaterialCommunityIcons as ExpoMaterialCommunityIcons } from '@expo/vector-icons';
import createIconSet from '@expo/vector-icons/build/vendor/react-native-vector-icons/lib/create-icon-set';
import glyphMap from '@expo/vector-icons/build/vendor/react-native-vector-icons/glyphmaps/MaterialCommunityIcons.json';

const NativeMaterialCommunityIcons = createIconSet(
  glyphMap,
  'material-community',
  null,
);

export const MaterialCommunityIcons =
  Platform.OS === 'android' ? NativeMaterialCommunityIcons : ExpoMaterialCommunityIcons;
