// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';
import {beforeEach, jest} from "@jest/globals";

function ignoreLogs(logType: 'log' | 'info' | 'warn' | 'error', match: RegExp) {
    const logFn = console[logType];
    jest.spyOn(console, logType).mockImplementation((...args) => {
        if (typeof args[0] === 'string' && args[0].match(match)) {
            return;
        }

        logFn(...args);
    });
}

beforeEach(() => {
    ignoreLogs('error', /Support for defaultProps will be removed/);
});
