# Quickstart: Migrate Admin & Front from Svelte to Astro

**Date**: 2026-05-09  
**Feature**: Migrate Admin & Front from Svelte to Astro  
**Goal**: Get both Astro projects running locally in 5 minutes

## Prerequisites

- Node.js 18.x or higher
- npm 9.x or higher
- Git (for version control)

**Verify Installation**:
```bash
node --version  # Should be v18.x or higher
npm --version   # Should be 9.x or higher
```

## Quick Setup

### Step 1: Project Initialization (One-time per project)

The old Svelte projects (`admin/` and `front/`) will be deleted and replaced with Astro projects.

```bash
# Remove old Svelte projects (save any custom config first!)
rm -rf admin
rm -rf front

# Create new Astro projects
npm create astro@latest -- --template minimal admin
npm create astro@latest -- --template minimal front

cd admin && npm install
cd ../front && npm install
```

### Step 2: Configure Ports

**admin/.package.json** - Update dev script:
```json
"scripts": {
  "dev": "astro dev --port 3000"
}
```

**front/package.json** - Update dev script:
```json
"scripts": {
  "dev": "astro dev --port 3001"
}
```

### Step 3: Copy i18n Files

Both projects reuse existing translations:

```bash
# For admin project
cp ../messages/*.json admin/messages/

# For admin project
cp ../messages/*.json front/messages/
```

### Step 4: Create Environment Files

**admin/.env.local**:
```env
VITE_API_URL=http://localhost:8000
```

**front/.env.local**:
```env
VITE_API_URL=http://localhost:8000
```

### Step 5: Install Dependencies

```bash
# Admin
cd admin
npm install

# Front (from root)
cd ../front
npm install
```

## Running Development Servers

### Option 1: Terminal Windows (Recommended for Beginners)

**Terminal 1** - Admin Dashboard:
```bash
cd admin
npm run dev
# Output: Server running at http://localhost:3000
```

**Terminal 2** - Front App:
```bash
cd front
npm run dev
# Output: Server running at http://localhost:3001
```

### Option 2: Concurrent (npm-run-all)

From repository root:

```bash
npm install -D npm-run-all

# Add to root package.json scripts
"dev": "npm-run-all --parallel dev:admin dev:front",
"dev:admin": "npm --prefix admin run dev",
"dev:front": "npm --prefix front run dev"

# Then run
npm run dev
```

## Accessing the Applications

After starting dev servers:

- **Admin Dashboard**: http://localhost:3000
- **Front App**: http://localhost:3001

## Building for Production

### Admin Build
```bash
cd admin
npm run build
# Output: dist/ directory ready for deployment
```

### Front Build
```bash
cd front
npm run build
# Output: dist/ directory ready for deployment
```

## Common Commands

| Command | Purpose |
|---------|---------|
| `npm run dev` | Start development server with HMR |
| `npm run build` | Create production-ready build |
| `npm run preview` | Preview production build locally |
| `npm run check` | TypeScript type checking |
| `npm run astro -- --version` | Show Astro version |

## Project Structure Overview

```
admin/
├── src/
│   ├── components/    # Astro components
│   ├── pages/         # File-based routes
│   ├── layouts/       # Shared layouts
│   └── lib/           # Utilities & helpers
├── messages/          # i18n translations (en.json, es.json)
├── astro.config.mjs   # Astro configuration
├── tsconfig.json      # TypeScript config
└── package.json       # Dependencies

front/
└── [Same structure as admin]
```

## Troubleshooting

### Issue: "Port 3000 already in use"

**Solution**: Kill the process or change port in package.json:
```bash
lsof -i :3000  # Find process
kill -9 <PID>  # Kill process

# OR change port in package.json scripts
"dev": "astro dev --port 3050"
```

### Issue: "Module not found: messages"

**Solution**: Ensure message files are copied:
```bash
ls admin/messages/    # Should show en.json, es.json
ls front/messages/    # Should show en.json, es.json
```

### Issue: TypeScript errors in IDE

**Solution**: Ensure TypeScript version is consistent:
```bash
npm install -D typescript@latest
npm run check  # TypeScript check
```

### Issue: Slow dev server startup

**Solution**: Clear cache and reinstall:
```bash
rm -rf node_modules/ .astro/
npm install
npm run dev
```

## Next Steps

1. **Explore components**: Create your first component in `src/components/`
2. **Add pages**: Create pages in `src/pages/` (file-based routing)
3. **Set up i18n**: Use messages from `messages/` directory in components
4. **Write tests**: Add tests in `tests/` directory (Vitest + Playwright)
5. **Deploy**: Build and deploy `dist/` directory to your hosting

## Links & Documentation

- [Astro Documentation](https://docs.astro.build)
- [Astro Components](https://docs.astro.build/en/concepts/astro-components/)
- [File-based Routing](https://docs.astro.build/en/basics/astro-pages/)
- [Integrations](https://astro.build/integrations/)

## Support & Issues

For questions or issues:
1. Check [Astro Docs](https://docs.astro.build)
2. Search [GitHub Issues](https://github.com/withastro/astro/issues)
3. Ask in team Slack/Discord

---

**Happy building with Astro!** 🚀
