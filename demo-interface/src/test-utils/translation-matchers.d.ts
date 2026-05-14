// import required for merging with existing types
import 'vitest';
 
type CustomMatchers<R = unknown> = {
  toHaveTranslations(source?: Record<string, unknown>): () => R;
  toHaveTracking(selector: string, expectedCount?: number): () => R;
};

declare module 'vitest' {
  // interface is required for declaration merging
  // eslint-disable-next-line @typescript-eslint/consistent-type-definitions, @typescript-eslint/no-empty-object-type
  interface Matchers<T = unknown> extends CustomMatchers<T> {}
}
