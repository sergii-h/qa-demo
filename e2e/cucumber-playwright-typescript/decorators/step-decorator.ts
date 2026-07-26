import { test } from '@playwright/test';

export const step = (stepName: string) => {
  return function <T extends (...args: any[]) => any>(target: T) {
    return function (this: ThisParameterType<T>, ...args: Parameters<T>): Promise<Awaited<ReturnType<T>>> {
      return test.step(
        `${replacePlaceholders(stepName, args)}`,
        async () => {
          return await target.call(this, ...args);
        },
        { box: false },
      );
    };
  };
};

const replacePlaceholders = (template: string, values: any[]): string => {
  values.forEach(value => {
    if (typeof value === 'string' || typeof value === 'number') {
      template = template.replace(/({.*?})/, value as any);
    } else if (Array.isArray(value)) {
      template = template.replace(/({.*?})/, formatArray(value));
    } else if (typeof value === 'object' && value !== null) {
      template = template.replace(/({.*?})/, formatObject(value));
    }
  });
  return template;
};

const formatValue = (value: any): string => {
  if (Array.isArray(value)) return formatArray(value);
  if (typeof value === 'object' && value !== null) return formatObject(value);
  return String(value);
};

const formatArray = (arr: any[]): string => `[${arr.map(formatValue).join(', ')}]`;

const formatObject = (obj: Record<string, any>): string => {
  const entries = Object.entries(obj)
    .map(([key, val]) => `${key}: ${formatValue(val)}`)
    .join(', ');
  return `{${entries}}`;
};
