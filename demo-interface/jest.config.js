const config = {
    testEnvironment: './FixJSDOMEnvironment.ts',
    restoreMocks: true,
    setupFilesAfterEnv: ["@testing-library/jest-dom/extend-expect"]
};

module.exports = config;
