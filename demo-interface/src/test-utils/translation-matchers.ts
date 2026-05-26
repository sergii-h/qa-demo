import enMessages from '../locales/en/translation.json';

const translationMatchers = {
  toHaveTranslations(received: Array<string>, source?: Record<string, unknown>) {
    const getValueByPath = (obj: Record<string, unknown>, path: string) => {
      return path.split('.').reduce((acc, part) => {
        if (acc && typeof acc === 'object') {
          return (acc as Record<string, unknown>)[part];
        }
        return undefined;
      }, obj as unknown);
    };

    const sourceData = source ?? enMessages;

    const uniqueKeys = [...new Set(received)];

    if (uniqueKeys.length === 0) {
      return {
        pass: false,
        message: () => 'No translation keys found. Expected at least one translation to be used.',
      };
    }

    const missing = uniqueKeys.filter(key => getValueByPath(sourceData, key) === undefined);

    return {
      pass: missing.length === 0,
      message: () => `Missing translation keys:\n${missing.join('\n')}`,
    };
  },
};

const trackingMatchers = {
  toHaveTracking(doc: Document, expectedSelector: string, expectedCount: number = 1) {
    const elements = doc.querySelectorAll(expectedSelector);
    const pass = elements.length === expectedCount;

    return {
      pass,
      message: () => {
        if (elements.length === 0) {
          return `Element not found: ${expectedSelector}`;
        }
        return `Expected ${expectedCount} element(s) with selector "${expectedSelector}", but found ${elements.length}`;
      },
    };
  },
};

export { trackingMatchers, translationMatchers };
