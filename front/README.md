# E-Recadero Front

Public-facing web application for e-recadero built with [Astro](https://astro.build).

## Features

- 🚀 Ultra-fast with Astro's static generation + partial hydration
- 📱 Fully responsive mobile-first design
- 🌍 Internationalization ready (English & Spanish)
- 🎨 Modern, clean UI components
- ♿ Accessibility-first approach
- 📊 SEO optimized with meta tags
- 🧪 Testing support (Vitest + Playwright)

## Quick Start

### Prerequisites

- Node.js 18.x or higher
- npm 9.x or higher

### Installation

```bash
npm install
```

### Development

Start dev server on port 3001:

```bash
npm run dev
```

Visit [http://localhost:3001](http://localhost:3001)

### Production Build

```bash
npm run build
```

Preview:

```bash
npm run preview
```

## Project Structure

```
front/
├── src/
│   ├── components/       # Reusable Astro components
│   ├── layouts/          # Page layouts
│   ├── lib/              # Utilities and types
│   ├── pages/            # File-based routes
│   └── styles/           # Global CSS (optional)
├── public/               # Static files
├── messages/             # i18n translations (en.json, es.json)
├── astro.config.mjs      # Astro configuration
├── tsconfig.json         # TypeScript config
├── package.json
└── README.md

```

## Available Scripts

| Command | Purpose |
|---------|---------|
| `npm run dev` | Start dev server (port 3001) |
| `npm run build` | Production build to `dist/` |
| `npm run preview` | Preview production build |
| `npm run check` | TypeScript type checking |
| `npm test` | Unit tests (Vitest) |
| `npm run test:e2e` | E2E tests (Playwright) |

## Configuration

### Environment

Create `.env.local`:

```env
VITE_API_URL=http://localhost:8000
VITE_ANALYTICS_ID=GA-XXXXX
```

### Port

Port is configured in `astro.config.mjs`:

```javascript
server: {
  port: 3001,
}
```

## Components

### MainLayout

Main page wrapper with header and footer:

```astro
---
import MainLayout from '@/layouts/MainLayout.astro';
---

<MainLayout title="Page Title">
  <!-- Your content -->
</MainLayout>
```

### Navigation

Top navigation component.

### Footer

Footer with links and social media.

## Internationalization

Translations in `messages/`:

- `messages/en.json` - English
- `messages/es.json` - Spanish

User language preference stored in localStorage (`user_locale`).

## Pages

- `/` - Home page with hero and features
- `/about` - About page (to be created)
- `/contact` - Contact page (to be created)

Add new pages by creating `.astro` files in `src/pages/`.

## Performance

Target metrics:

- First Contentful Paint: < 1.5s
- Largest Contentful Paint: < 2.5s
- Cumulative Layout Shift: < 0.05
- Lighthouse Score: > 90

## Deployment

1. Build:
   ```bash
   npm run build
   ```

2. Upload `dist/` directory to host

3. Set environment variables in production

## Testing

### Unit Tests

```bash
npm test
```

### E2E Tests

```bash
npm run test:e2e
```

## Contributing

1. Create feature branch
2. Make changes
3. Run tests
4. Submit PR

## Resources

- [Astro Docs](https://docs.astro.build)
- [Astro Discord](https://astro.build/chat)

## License

MIT

## Support

For issues, create a GitHub issue or contact the team.
