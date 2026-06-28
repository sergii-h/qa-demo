import {fireEvent, waitFor} from '@testing-library/react-native';

import {EnumPicker} from './EnumPicker';
import {dismissPaperMenu, paperMenuMock} from '@/test-utils/paperMenuSpy';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

jest.mock('react-native-paper', () =>
  require('../test-utils/paperMenuSpy').paperMenuModuleMock(),
);

const enumPickerProps = {
  label: 'Status',
  value: 'TODO' as const,
  options: ['TODO', 'DONE'] as const,
  optionLabel: (value: 'TODO' | 'DONE') => value,
  testID: 'status-dropdown',
  optionTestID: (value: 'TODO' | 'DONE') => `status-option-${value}`,
};

describe('EnumPicker', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render dropdown with selected value', () => {
    // Given
    const onSelected = jest.fn();

    // When
    const { getByTestId } = renderWithProviders(
      <EnumPicker {...enumPickerProps} onSelected={onSelected} />,
    );

    // Then
    expect(getByTestId('status-dropdown')).toHaveDisplayValue('TODO');
  });

  it('should open menu when dropdown icon is pressed', async () => {
    // Given
    const onSelected = jest.fn();
    const { getByTestId, queryByTestId } = renderWithProviders(
      <EnumPicker {...enumPickerProps} onSelected={onSelected} />,
    );

    // When
    fireEvent.press(getByTestId('right-icon-adornment'));

    // Then
    await waitFor(() => {
      expect(queryByTestId('status-option-DONE')).not.toBeNull();
    });
  });

  it('should call onSelected when option is chosen', async () => {
    // Given
    const onSelected = jest.fn();
    const { getByTestId } = renderWithProviders(
      <EnumPicker {...enumPickerProps} onSelected={onSelected} />,
    );

    // When
    fireEvent.press(getByTestId('right-icon-adornment'));
    await waitFor(() => {
      expect(getByTestId('status-option-DONE')).toBeOnTheScreen();
    });
    fireEvent.press(getByTestId('status-option-DONE'));

    // Then
    expect(onSelected).toHaveBeenCalledWith('DONE');
  });

  it('should close menu when dismissed', async () => {
    // Given
    const onSelected = jest.fn();
    const { getByTestId } = renderWithProviders(
      <EnumPicker {...enumPickerProps} onSelected={onSelected} />,
    );
    fireEvent.press(getByTestId('right-icon-adornment'));
    expect(paperMenuMock.mock.calls.at(-1)?.[0].visible).toBe(true);

    // When
    dismissPaperMenu();

    // Then
    await waitFor(() => {
      expect(paperMenuMock.mock.calls.at(-1)?.[0].visible).toBe(false);
    });
  });
});
