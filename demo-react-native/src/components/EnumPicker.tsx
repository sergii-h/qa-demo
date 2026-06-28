import {useState} from 'react';
import {View} from 'react-native';
import {Menu, TextInput} from 'react-native-paper';

interface EnumPickerProps<T extends string> {
  label: string;
  value: T;
  options: readonly T[];
  optionLabel: (value: T) => string;
  onSelected: (value: T) => void;
  testID: string;
  optionTestID: (value: T) => string;
}

export function EnumPicker<T extends string>({
  label,
  value,
  options,
  optionLabel,
  onSelected,
  testID,
  optionTestID,
}: EnumPickerProps<T>) {
  const [visible, setVisible] = useState(false);
  const openMenu = () => setVisible(true);

  return (
    <View>
      <Menu
        visible={visible}
        onDismiss={() => setVisible(false)}
        anchor={
          <TextInput
            label={label}
            value={optionLabel(value)}
            mode="outlined"
            editable={false}
            testID={testID}
            right={
              <TextInput.Icon
                icon="menu-down"
                onPress={openMenu}
              />
            }
            onPressIn={openMenu}
          />
        }
      >
        {options.map((option) => (
          <Menu.Item
            key={option}
            onPress={() => {
              onSelected(option);
              setVisible(false);
            }}
            title={optionLabel(option)}
            testID={optionTestID(option)}
          />
        ))}
      </Menu>
    </View>
  );
}
