const replacePlaceholders = (template, values) => {
  values.forEach((value) => {
    if (!/({.*?})/.test(template)) {
      return;
    }

    if (typeof value === 'string' || typeof value === 'number') {
      template = template.replace(/({.*?})/, String(value));
    } else if (Array.isArray(value)) {
      template = template.replace(/({.*?})/, formatArray(value));
    } else if (typeof value === 'object' && value !== null) {
      template = template.replace(/({.*?})/, formatObject(value));
    }
  });
  return template;
};

const formatValue = (value) => {
  if (Array.isArray(value)) return formatArray(value);
  if (typeof value === 'object' && value !== null) return formatObject(value);
  return String(value);
};

const formatArray = (arr) => `[${arr.map(formatValue).join(', ')}]`;

const formatObject = (obj) => {
  const seen = new WeakSet();

  const format = (value) => {
    if (value === null || typeof value !== 'object') {
      return formatValue(value);
    }

    if (seen.has(value)) {
      return '[Circular]';
    }

    seen.add(value);

    if (Array.isArray(value)) {
      return formatArray(value);
    }

    const entries = Object.entries(value)
      .map(([key, val]) => `${key}: ${format(val)}`)
      .join(', ');
    return `{${entries}}`;
  };

  return format(obj);
};

const step = (stepName, fn) => {
  return function (...args) {
    const resolvedName = replacePlaceholders(stepName, args);
    const result = fn.apply(this, args);
    cy.allure().logStep(resolvedName);
    return result;
  };
};

module.exports = { step };
