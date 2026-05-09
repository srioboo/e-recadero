# Contract: Front Astro Project

**Date**: 2026-05-09  
**Project**: front/  
**Type**: Frontend Application Contract  
**Status**: Design

## Public Exports

### Components

#### `<Header>`
**Purpose**: Top navigation bar  
**Props**:
```typescript
interface HeaderProps {
  currentLocale: 'en' | 'es'
  onLocaleChange: (locale: 'en' | 'es') => void
}
```

#### `<ContentSection>`
**Purpose**: Reusable section component for landing pages  
**Props**:
```typescript
interface ContentSectionProps {
  type: 'hero' | 'features' | 'testimonials' | 'cta'
  title: string
  content: string
  ctas?: CTAButton[]
}
```

#### `<LocaleSwitcher>`
**Purpose**: Language selector component  
**Props**:
```typescript
interface LocaleSwitcherProps {
  currentLocale: 'en' | 'es'
  onChange: (locale: 'en' | 'es') => void
}
```

### Utilities

#### `getLocalizedContent(key: string, locale: 'en' | 'es'): string`
**Purpose**: Retrieve localized content  
**Signature**:
```typescript
// Loads from messages/{locale}.json
getLocalizedContent('key.path', 'en') → 'Localized text'
```

### Page Routes

- `/` - Home page
- `/page/[slug]` - Dynamic page routing
- `/about` - About page (optional)
- `/contact` - Contact page (optional)

### Context Providers

#### `<LocaleProvider>`
Provides current locale and switching functionality

**Exports**:
```typescript
type LocaleContext = {
  current: 'en' | 'es'
  messages: Record<string, string>
  setLocale: (locale: 'en' | 'es') => void
}
```

## Consumed External Contracts

### Backend API Contract

**Base URL**: `${import.meta.env.VITE_API_URL}`

Required endpoints:
- `GET /content/pages` - List of public pages
- `GET /content/pages/:slug` - Specific page content
- `GET /content/sections` - Content sections for homepage

### Response Format Example (Page Endpoint)
```json
{
  "slug": "about",
  "title": "About Us",
  "description": "Learn about e-recadero",
  "content": "<h1>About</h1>...",
  "sections": [
    {
      "type": "hero",
      "title": "Welcome",
      "content": "..."
    }
  ]
}
```

## Build Artifacts

### Development
- **Command**: `npm run dev`
- **Output**: Dev server on `http://localhost:3001`
- **HMR**: Enabled via Astro

### Production
- **Command**: `npm run build`
- **Output**: `dist/` directory with static files optimized for CDN
- **Size Target**: < 300KB (main bundle + CSS)

## Environment Variables

| Variable | Required | Example |
|----------|----------|---------|
| `VITE_API_URL` | Yes | `https://api.example.com` |
| `VITE_ANALYTICS_ID` | No | `GA-XXXXX` |

## SEO & Performance

### Required Meta Tags
- `og:title`, `og:description`, `og:image`
- `twitter:card`, `twitter:title`
- `description` meta tag
- Canonical URL

### Performance Budget
- First Contentful Paint: < 1.5s (mobile)
- Largest Contentful Paint: < 2.5s (mobile)
- Cumulative Layout Shift: < 0.05

### Internationalization

- Content sourced from `messages/en.json` and `messages/es.json`
- Language preference stored in localStorage: `user_locale`
- Server-side rendering for SEO (SSR for SSG-compatible routes)

## Dependencies Exposed to Calling Code

None - this is a standalone frontend application.

## Testing Contract

### Unit Test Coverage
- Components: 75%+
- Utils: 85%+

### E2E Test Scenarios
- Homepage loads
- Page navigation
- Language switching
- Mobile responsiveness

**Test Framework**: Playwright  
**Entry Point**: `tests/e2e/front.spec.ts`
