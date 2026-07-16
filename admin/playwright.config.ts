import { defineConfig } from '@playwright/test';

const MOCK_BACKEND_PORT = 4320;
const ADMIN_PORT = 3000;

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: `http://localhost:${ADMIN_PORT}`,
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
      port: ADMIN_PORT,
      reuseExistingServer: !process.env.CI,
      env: {
        PUBLIC_API_V1_BASE_URL: `http://localhost:${MOCK_BACKEND_PORT}/api/v1`,
        PUBLIC_API_BASE_URL: `http://localhost:${MOCK_BACKEND_PORT}/api`,
      },
    },
  ],
});
