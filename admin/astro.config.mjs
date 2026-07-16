import { defineConfig } from 'astro/config';
import node from '@astrojs/node';
import react from '@astrojs/react';

export default defineConfig({
  server: {
    port: 3000,
  },
  output: 'hybrid',
  adapter: node({
    mode: 'standalone',
  }),
  integrations: [react()],
  vite: {
    ssr: {
      external: ['svgo'],
    },
  },
});
