import i18n from 'i18next';

let translationSpy: jest.SpiedFunction<typeof i18n.t> | undefined;

export function releaseTranslationSpy() {
  translationSpy?.mockRestore();
  translationSpy = undefined;
}

export function trackTranslationCalls() {
  const calls: string[] = [];
  releaseTranslationSpy();
  translationSpy = jest.spyOn(i18n, 't').mockImplementation((key) => {
    const resolvedKey = Array.isArray(key) ? key[0] : key;
    calls.push(resolvedKey);
    return resolvedKey;
  });

  return { calls };
}
