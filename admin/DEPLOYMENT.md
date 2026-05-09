# E-Recadero Admin - Deployment Guide

This guide covers building and deploying the E-Recadero Admin dashboard to production environments.

## Prerequisites

- Node.js 18+ with npm
- Access to production API servers
- Environment variables configured (see `.env.production`)

## Build Process

### Development Build

```bash
npm run build
```

This creates an optimized production build in the `dist/` directory.

### Build Output

```
dist/
├── _app/
│   ├── immutable/
│   │   ├── chunks/
│   │   └── nodes/
│   └── manifest.json
├── client/
├── index.html
└── [other routes]/
```

**Expected build time**: < 30 seconds
**Expected bundle size**: < 500KB (gzipped)

## Environment Variables

### Local Development

```sh
cp .env.local .env.local.bak
cat > .env.local << EOF
VITE_API_URL=http://localhost:8000
EOF
npm run dev
```

### Production Deployment

Configure these environment variables in your deployment environment:

```env
# API endpoint for production
VITE_API_URL=https://api.e-recadero.com

# Node environment
NODE_ENV=production
```

These are read at build time and injected into the frontend code via `import.meta.env.VITE_*`.

## Deployment Steps

### 1. Prepare Build

```bash
# Install dependencies
npm install --production

# Run type checking
npm run check

# Create production build
npm run build
```

### 2. Verify Built Assets

```bash
# Check build output
ls -lh dist/
du -sh dist/

# Quick sanity check - verify key files exist
test -f dist/index.html && echo "✓ index.html found" || echo "✗ index.html missing"
test -f dist/_app/manifest.json && echo "✓ manifest.json found" || echo "✗ manifest.json missing"
```

### 3. Deploy to Server

#### Option A: Static Hosting (Netlify, Vercel, etc.)

```bash
# Upload dist/ directory to your static host
# Most providers auto-detect Astro projects
npm run build
# Then deploy the dist/ folder
```

#### Option B: Node.js Server (Self-hosted)

```bash
# Build and package for Node.js
npm run build

# Copy to server
scp -r dist/ user@server.com:/var/www/e-recadero-admin/

# On server - install and start
cd /var/www/e-recadero-admin
npm install --production
npm run preview  # Start preview server (for testing)
```

Then use `npm run preview` to test the production build locally before deploying.

#### Option C: Docker Container

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --production
COPY dist/ ./dist/
EXPOSE 3000
CMD ["npm", "run", "preview"]
```

Build and push:

```bash
docker build -t e-recadero-admin:1.0 .
docker push your-registry/e-recadero-admin:1.0
```

### 4. Health Checks

After deployment, verify:

```bash
# Check dashboard loads
curl -f https://your-domain.com/ > /dev/null && echo "✓ Dashboard is up" || echo "✗ Dashboard failed"

# Verify API connectivity
curl -f https://api.e-recadero.com/health 2>/dev/null && echo "✓ API is reachable" || echo "✗ API unreachable"

# Check browser console for VITE_API_URL
# Visit https://your-domain.com and open DevTools Console to verify env vars loaded
```

## Performance Optimization

### Build Optimization Settings

These are configured in `astro.config.mjs`:

```javascript
vite: {
  build: {
    minify: 'terser',  // Full JavaScript minification
    cssMinify: true,   // CSS minification
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['astro']  // Split vendor code
        }
      }
    }
  }
}
```

### Performance Targets

| Metric | Target | How to Measure |
|--------|--------|---|
| Page Load Time | < 2s | Lighthouse, WebPageTest |
| Bundle Size | < 500KB | `du -sh dist/` |
| Lighthouse Score | > 90 | Chrome DevTools > Lighthouse |
| First Contentful Paint | < 1.5s | Chrome DevTools > Performance |

## Rollback Procedure

If deployment fails:

```bash
# Keep previous dist/ backup
mv dist/ dist.new/
mv dist.backup/ dist/
npm run preview
# If successful, delete dist.new/
```

## Monitoring

Monitor these metrics in production:

- Server response times
- 5xx error rates
- API connectivity
- Build size trends
- Bundle analysis

Use tools like:
- New Relic
- DataDog
- Google Analytics
- Sentry (error tracking)

## Troubleshooting

### Issue: "Cannot find module" errors

**Solution**: Run `npm install` without `--production` flag during build

```bash
npm install
npm run build
npm prune --production  # Remove dev dependencies before deploy
```

### Issue: API calls return 404

**Solution**: Verify `VITE_API_URL` environment variable is set correctly

```bash
# Check in browser console:
console.log(import.meta.env.VITE_API_URL)
# Should output: https://api.e-recadero.com
```

### Issue: Static assets (CSS, JS) return 404

**Solution**: Check `base` path in `astro.config.mjs` matches deployment path

```javascript
// If deploying to https://domain.com/admin/:
export default defineConfig({
  base: '/admin/',
  // ...
});
```

## Support

For issues or questions, contact the development team or visit:
- Repository: [GitHub link]
- Documentation: [Docs link]
- Bug Reports: [Issues link]
