import React from 'react';
import { useTranslation } from 'react-i18next';
import { Dropdown } from 'primereact/dropdown';

const LANGUAGE_OPTIONS = [
    { label: 'EN', value: 'en' },
    { label: 'ES', value: 'es' },
];

export const LanguageSwitcher = () => {
    const { t, i18n } = useTranslation();
    const languageSwitcherId = 'language-switcher-input';

    return (
        <>
            <label htmlFor={languageSwitcherId} className="sr-only">{t('languageSwitcher.label')}</label>
            <Dropdown
                inputId={languageSwitcherId}
                ariaLabel={t('languageSwitcher.label')}
                data-testid="language-switcher"
                value={i18n.resolvedLanguage}
                options={LANGUAGE_OPTIONS}
                onChange={(e) => i18n.changeLanguage(e.value)}
                pt={{
                    select: { 'aria-label': t('languageSwitcher.label') },
                }}
            />
        </>
    );
};

export default LanguageSwitcher;
