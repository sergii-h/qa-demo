export {};

declare global {
  namespace jest {
    interface Matchers<R> {
      toHaveTranslations(source?: Record<string, unknown>): R;
    }
  }
}
