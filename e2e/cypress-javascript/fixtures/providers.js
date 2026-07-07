const { StepProvider } = require('@/providers/StepProvider');
const { ValidationProvider } = require('@/providers/ValidationProvider');
const { SupportProvider } = require('@/providers/SupportProvider');

const step = new StepProvider();
const validate = new ValidationProvider();
const support = new SupportProvider();

module.exports = { step, validate, support };
