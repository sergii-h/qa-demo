import React from 'react';

const reactNativePaper =
  jest.requireActual<typeof import('react-native-paper')>('react-native-paper');

export const paperMenuMock = jest.fn(
  (props: React.ComponentProps<typeof reactNativePaper.Menu>) =>
    React.createElement(reactNativePaper.Menu, props),
);

export function paperMenuModuleMock() {
  const MenuWithItem = Object.assign(paperMenuMock, {
    Item: reactNativePaper.Menu.Item,
  });

  return {
    ...reactNativePaper,
    Menu: MenuWithItem,
  };
}

export function dismissPaperMenu() {
  const onDismiss = paperMenuMock.mock.calls.at(-1)?.[0]?.onDismiss as
    | (() => void)
    | undefined;

  if (!onDismiss) {
    throw new Error('Expected Menu onDismiss callback');
  }

  onDismiss();
}
