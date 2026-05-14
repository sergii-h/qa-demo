import React from 'react';
import { useTranslation } from 'react-i18next';
import { Dropdown } from 'primereact/dropdown';

const LANGUAGE_OPTIONS = [
    { label: 'EN', value: 'en' },
    { label: 'ES', value: 'es' },
];

export const LanguageSwitcher = () => {
    const { i18n } = useTranslation();

    return (
        <Dropdown
            data-testid="language-switcher"
            value={i18n.resolvedLanguage}
            options={LANGUAGE_OPTIONS}
            onChange={(e) => i18n.changeLanguage(e.value)}
        />
    );
};

export default LanguageSwitcher;
