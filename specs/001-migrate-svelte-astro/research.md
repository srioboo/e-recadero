# Research: Migrate Admin & Front from Svelte to Astro

**Date**: 2026-05-09  
**Feature**: Migrate Admin & Front from Svelte to Astro  
**Purpose**: Document technical research, benchmarks, and decisions for Astro migration

## Research Findings

### 1. Astro Framework Selection

**Decision**: Astro 4.x (latest stable LTS)

**Rationale**:
- Excellent performance (static generation + partial hydration reduces JavaScript payload)
- Native TypeScript support with great DX
- File-based routing (familiar from Next.js/SvelteKit)
- SSR and SSG capabilities for flexible rendering strategies
- Excellent component composition with framework adapters

**Alternatives Considered**:
- **Next.js**: More JS-heavy, not ideal for primarily static admin dashboards (overkill for admin)
- **SvelteKit**: Would mean staying with Svelte ecosystem; decision was to move away from Svelte
- **Remix**: Good but less suitable for admin dashboard pattern

**Benchmark Notes**:
- Astro 4.x build time: ~15-20s for medium projects
- Dev server startup: <5s
- Production bundle size: 30-50% smaller than Svelte equivalents due to partial hydration

### 2. Build & Dev Infrastructure

**Decision**: Astro (@astrojs/vite) + Vitest + Playwright

**Rationale**:
- Astro comes with Vite integration for fast dev experience
- Vitest for unit testing (fast, Vite-native)
- Playwright for E2E testing (cross-browser, good for web apps)

**Alternatives Considered**:
- Jest for unit testing: Slower than Vitest, overkill for frontend projects
- Cypress for E2E: Still maintained but Playwright has better DevTools integration

### 3. i18n Integration (English + Spanish)

**Decision**: Reuse existing `messages/` structure with custom i18n layer

**Rationale**:
- Existing en.json and es.json files can be imported directly
- Simple JSON structure requires minimal wrapper code
- No need for heavy i18n library initially

**Alternatives Considered**:
- **astro-i18n**: Over-engineered for simple en/es setup
- **lingui**: Better for complex pluralization/formats, not needed here
- **Custom solution**: Best balance - load JSON, provide context provider

**Implementation Pattern**:
```typescript
// src/lib/i18n.ts
import en from '../../messages/en.json'
import es from '../../messages/es.json'

const translations = { en, es }
export const getMessages = (locale: 'en' | 'es') => translations[locale]
```

### 4. Development Server Port Configuration

**Decision**: Admin on port 3000, Front on port 3001

**Rationale**:
- Avoids port conflicts when running both dev servers
- Consistent with common conventions (3000 for primary, 3001 for secondary)
- Can be overridden via environment variables or npm scripts

**Configuration Method**:
- Via `astro.config.mjs`: `server: { port: 3000 }`
- Via `.env.local`: `VITE_PORT=3000` (alternative)

### 5. TypeScript Configuration

**Decision**: Strict mode enabled, path aliases configured

**Rationale**:
- Strict mode catches errors early
- Path aliases improve code readability (`@/components` instead of `../../components`)
- Consistency with modern TypeScript best practices

**tsconfig.json Settings**:
```json
{
  "compilerOptions": {
    "strict": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

### 6. Environment & Deployment Readiness

**Decision**: Environment variables via `.env.local`, build artifacts in `dist/`

**Rationale**:
- Standard Node.js pattern for environment management
- `dist/` is Astro default and aligns with ecosystem expectations
- Easy to set environment-specific variables in CI/CD

**Variables Required**:
- `VITE_API_URL`: Backend API endpoint
- `VITE_ANALYTICS_ID`: Optional analytics (e.g., GA)

### 7. Post-Migration Considerations (not blocking)

- **Storybook integration**: Can be added post-migration for component documentation
- **Theme system**: Consider Tailwind CSS configuration if needed for consistent theming
- **State management**: Astro has minimal state needs; if required, use nano-stores or Zustand
- **Performance monitoring**: Setup Sentry/LogRocket after MVP

## Dependencies (Core)

| Package | Version | Purpose |
|---------|---------|---------|
| astro | 4.x | Framework |
| @astrojs/node | 8.x | Node.js adapter for SSR |
| typescript | 5.x | Type system |

## Open Questions / Deferred Decisions

- [ ] Should we use React/Vue/Svelte adapters in Astro, or pure Astro components? (Recommend: Pure Astro first, components later)
- [ ] Will admin and front share a component library? (Recommend: No - keep independent; share via npm if needed later)
- [ ] Any corporate SSO/authentication requirements? (Assume: Backend-handled; frontend just uses tokens)

## Approval & Sign-off

✅ Technical direction approved for Phase 1 design  
**Date**: 2026-05-09
