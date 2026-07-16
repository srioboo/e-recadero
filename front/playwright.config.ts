import { defineConfig } from '@playwright/test';

const MOCK_BACKEND_PORT = 4310;
const FRONT_PORT = 3001;

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  reporter: 'list',
  use: {
    baseURL: `http://localhost:${FRONT_PORT}`,
  },
  webServer: [
    {
      command: `node tests/e2e/support/mock-backend.mjs`,
      port: MOCK_BACKEND_PORT,
      reuseExistingServer: !process.env.CI,
      env: { MOCK_BACKEND_PORT: String(MOCK_BACKEND_PORT) },
    },
    {
      command: 'npm run dev',
      port: FRONT_PORT,
      reuseExistingServer: !process.env.CI,
      env: {
        API_BASE_URL: `http://localhost:${MOCK_BACKEND_PORT}/api/v1`,
        PUBLIC_API_BASE_URL: `http://localhost:${MOCK_BACKEND_PORT}/api`,
      },
    },
  ],
});
