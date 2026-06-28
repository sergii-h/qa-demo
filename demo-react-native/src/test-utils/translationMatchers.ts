import enMessages from '../locales/en/translation.json';

export const translationMatchers = {
  toHaveTranslations(received: string[], source?: Record<string, unknown>) {
    const getValueByPath = (obj: Record<string, unknown>, path: string) =>
      path.split('.').reduce<unknown>((acc, part) => {
        if (acc && typeof acc === 'object') {
          return (acc as Record<string, unknown>)[part];
        }
        return undefined;
      }, obj);

    const sourceData = source ?? enMessages;
    const uniqueKeys = [...new Set(received)];

    if (uniqueKeys.length === 0) {
      return {
        pass: false,
        message: () =>
          'No translation keys found. Expected at least one translation to be used.',
      };
    }

    const missing = uniqueKeys.filter((key) => getValueByPath(sourceData, key) === undefined);

    return {
      pass: missing.length === 0,
      message: () => `Missing translation keys:\n${missing.join('\n')}`,
    };
  },
};
