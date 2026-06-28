import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Menu, Text, TouchableRipple} from 'react-native-paper';

import {ENGLISH, setLanguage, SPANISH} from '@/i18n';
import {TestTags} from '@/testTags';

interface LanguageSwitcherProps {
  style?: object;
}

export function LanguageSwitcher({ style }: LanguageSwitcherProps) {
  const { t, i18n } = useTranslation();
  const [visible, setVisible] = useState(false);

  const selectedLabel =
    i18n.language === SPANISH ? t('languageEs') : t('languageEn');

  return (
    <Menu
      visible={visible}
      onDismiss={() => setVisible(false)}
      anchor={
        <TouchableRipple
          onPress={() => setVisible(true)}
          accessibilityLabel={t('languageLabel')}
          testID={TestTags.LANGUAGE_SWITCHER}
          style={style}
        >
          <Text variant="titleMedium" style={{ fontWeight: '600', padding: 8 }}>
            {selectedLabel}
          </Text>
        </TouchableRipple>
      }
    >
      <Menu.Item
        onPress={() => {
          setVisible(false);
          void setLanguage(ENGLISH);
        }}
        title={t('languageEn')}
        testID={TestTags.LANGUAGE_OPTION_EN}
      />
      <Menu.Item
        onPress={() => {
          setVisible(false);
          void setLanguage(SPANISH);
        }}
        title={t('languageEs')}
        testID={TestTags.LANGUAGE_OPTION_ES}
      />
    </Menu>
  );
}
