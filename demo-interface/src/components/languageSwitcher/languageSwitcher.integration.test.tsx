import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import i18n from '../../i18n';
import LanguageSwitcher from './languageSwitcher';

describe('LanguageSwitcher integration', () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(async () => {
    user = userEvent.setup();
    await i18n.changeLanguage('en');
  });

  const selectLanguage = async (languageLabel: 'ES') => {
    await user.click(screen.getByTestId('language-switcher'));
    const options = await screen.findAllByRole('option', { name: languageLabel, hidden: true });
    await user.click(options[options.length - 1]);
  };

  it('should render language switcher with EN and ES options', async () => {
    // when
    render(<LanguageSwitcher />);
    await user.click(screen.getByTestId('language-switcher'));

    // then
    expect((await screen.findAllByRole('option', { name: 'EN', hidden: true })).length).toBeGreaterThan(0);
    expect((await screen.findAllByRole('option', { name: 'ES', hidden: true })).length).toBeGreaterThan(0);
  });

  it('should change current language when user selects ES', async () => {
    // when
    render(<LanguageSwitcher />);
    await selectLanguage('ES');

    // then
    expect(i18n.resolvedLanguage).toBe('es');
  });
});
