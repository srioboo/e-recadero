import { defineConfig } from 'astro/config';
import node from '@astrojs/node';

export default defineConfig({
  server: {
    port: 3000,
  },
  output: 'hybrid',
  adapter: node({
    mode: 'standalone',
  }),
  vite: {
    ssr: {
      external: ['svgo'],
    },
  },
});
