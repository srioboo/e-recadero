# E-Recadero Front - Deployment Guide

This guide covers building and deploying the E-Recadero public-facing web application to production environments.

## Prerequisites

- Node.js 18+ with npm
- Access to production API servers
- CDN or static hosting service (optional but recommended)
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
└── [routes]/
```

**Expected build time**: < 25 seconds
**Expected bundle size**: < 300KB (gzipped)

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

#### Option A: Static Hosting (Netlify, Vercel, GitHub Pages, etc.)

```bash
# Most providers auto-detect Astro projects
npm run build

# Then connect your repository and deploy
# Netlify/Vercel will automatically detect astro.config.mjs
```

#### Option B: Content Delivery Network (CDN)

```bash
# Build
npm run build

# Upload to CDN (example: AWS S3 + CloudFront)
aws s3 sync dist/ s3://e-recadero-cdn/ --delete
aws cloudfront create-invalidation --distribution-id E123456789 --paths "/*"
```

#### Option C: Node.js Server (Self-hosted)

```bash
# Build and package for Node.js
npm run build

# Copy to server
scp -r dist/ user@server.com:/var/www/e-recadero-front/

# On server
cd /var/www/e-recadero-front
npm install --production
npm run preview
```

#### Option D: Docker Container

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --production
COPY dist/ ./dist/
EXPOSE 3001
CMD ["npm", "run", "preview"]
```

Build and push:

```bash
docker build -t e-recadero-front:1.0 .
docker push your-registry/e-recadero-front:1.0
```

### 4. Health Checks

After deployment, verify:

```bash
# Check homepage loads
curl -f https://your-domain.com/ > /dev/null && echo "✓ Homepage is up" || echo "✗ Homepage failed"

# Verify i18n support
curl -s https://your-domain.com/ | grep -q "localStorage" && echo "✓ i18n ready" || echo "✗ i18n check failed"

# Check browser console for VITE_API_URL
# Visit https://your-domain.com and open DevTools Console to verify env vars loaded
```

## Performance Optimization

### Build Optimization Settings

These are configured in `astro.config.mjs`:

```javascript
vite: {
  build: {
    minify: 'terser',     // Full JavaScript minification
    cssMinify: true,      // CSS minification
    terserOptions: {
      compress: {
        passes: 2         // Multiple compression passes
      }
    }
  }
}
```

### Caching Strategy

```
# .htaccess (Apache) or equivalent
<FilesMatch "\.(js|css|woff|svg)$">
  Header set Cache-Control "public, max-age=31536000, immutable"
</FilesMatch>

<FilesMatch "\.html$">
  Header set Cache-Control "public, max-age=3600"
</FilesMatch>
```

### Performance Targets

| Metric | Target | How to Measure |
|--------|--------|---|
| Page Load Time | < 1.5s | Lighthouse, WebPageTest |
| Bundle Size | < 300KB | `du -sh dist/` |
| Lighthouse Score | > 90 | Chrome DevTools > Lighthouse |
| First Contentful Paint | < 1s | Chrome DevTools > Performance |
| Cumulative Layout Shift | < 0.05 | Lighthouse |

## Rollback Procedure

If deployment fails or has issues:

```bash
# Keep current dist/ as backup
mv dist/ dist.failed/

# Restore previous version
mv dist.backup/ dist/

# Restart and verify
npm run preview
# Test before marking as stable
```

## Internationalization (i18n)

The app supports English (en) and Spanish (es).

### Testing i18n in Production

```bash
# Verify message files are deployed
curl -s https://your-domain.com/messages/en.json
curl -s https://your-domain.com/messages/es.json

# Test language switching in browser console:
localStorage.setItem('user_locale', 'es')
location.reload()
```

## Monitoring

Monitor these metrics in production:

- Page load times
- Language switching behavior
- API endpoint reachability
- 4xx/5xx error rates
- Build size trends

Use tools like:
- Google Analytics
- Sentry (error tracking)
- DataDog
- New Relic

## Troubleshooting

### Issue: "Cannot find module" errors

**Solution**: Ensure dev dependencies are included during build

```bash
npm install  # Include devDependencies
npm run build
npm prune --production  # Remove dev deps after build
```

### Issue: API calls return 404

**Solution**: Verify `VITE_API_URL` is configured correctly

```bash
# Check in browser console:
console.log(import.meta.env.VITE_API_URL)
# Should output: https://api.e-recadero.com
```

### Issue: Language switching doesn't work

**Solution**: Check localStorage is enabled and messages are accessible

```bash
# Browser console:
localStorage.getItem('user_locale')
// Should return 'en', 'es', or null
```

### Issue: Static assets return 404

**Solution**: Verify `base` path in `astro.config.mjs` matches deployment URL

```javascript
// If deploying to https://domain.com/:
export default defineConfig({
  base: '/',
  // ...
});
```

## Support

For issues or questions:
- Repository: [GitHub link]
- Documentation: [Docs link]
- Community: [Discord/Slack link]
- Bug Reports: [Issues link]
