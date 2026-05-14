import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LanguageSwitcher from './languageSwitcher';
import i18n from '../../i18n';

describe('LanguageSwitcher', () => {
    afterEach(async () => {
        await i18n.changeLanguage('en');
    });

    describe('Rendering', () => {
        it('should render language switcher dropdown', () => {
            render(<LanguageSwitcher />);

            expect(screen.getByTestId('language-switcher')).toBeInTheDocument();
        });
    });

    describe('Language switching', () => {
        it('should call changeLanguage with "es" when ES is selected', async () => {
            const user = userEvent.setup();
            const changeLanguageSpy = vi.spyOn(i18n, 'changeLanguage').mockResolvedValue(undefined as any);

            render(<LanguageSwitcher />);
            await user.click(screen.getByTestId('language-switcher'));
            await user.click(await screen.findByRole('option', { name: 'ES', hidden: true }));

            expect(changeLanguageSpy).toHaveBeenCalledWith('es');
        });

        it('should call changeLanguage with "en" when EN is selected', async () => {
            const user = userEvent.setup();
            await i18n.changeLanguage('es');
            const changeLanguageSpy = vi.spyOn(i18n, 'changeLanguage').mockResolvedValue(undefined as any);

            render(<LanguageSwitcher />);
            await user.click(screen.getByTestId('language-switcher'));
            await user.click(await screen.findByRole('option', { name: 'EN', hidden: true }));

            expect(changeLanguageSpy).toHaveBeenCalledWith('en');
        });
    });
});
