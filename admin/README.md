# E-Recadero Admin Dashboard

Admin dashboard for e-recadero built with [Astro](https://astro.build).

## Features

- ⚡ Lightning fast with Astro
- 🎨 Modern UI with responsive design
- 📱 Mobile-friendly interface
- 🔐 TypeScript for type safety
- 🧪 Testing support (Vitest + Playwright)
- 🌍 i18n ready (English & Spanish)

## Getting Started

### Prerequisites

- Node.js 18.x or higher
- npm 9.x or higher

### Installation

```bash
npm install
```

### Development

Start the development server on port 3000:

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build

Create a production-optimized build:

```bash
npm run build
```

Preview the production build locally:

```bash
npm run preview
```

## Project Structure

```
admin/
├── src/
│   ├── components/       # Reusable Astro components
│   ├── layouts/          # Layout components
│   ├── lib/              # Utilities and types
│   ├── pages/            # File-based routing
│   └── styles/           # Global styles (optional)
├── public/               # Static assets
├── messages/             # i18n translations
├── astro.config.mjs      # Astro configuration
├── tsconfig.json         # TypeScript configuration
├── package.json          # Dependencies
└── README.md             # This file
```

## Available Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start dev server (port 3000) |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run check` | Run TypeScript type checking |
| `npm test` | Run unit tests (Vitest) |
| `npm run test:e2e` | Run E2E tests (Playwright) |

## Configuration

### Environment Variables

Create a `.env.local` file:

```env
VITE_API_URL=http://localhost:8000
```

### TypeScript

TypeScript is configured in strict mode. Configuration is in `tsconfig.json`.

### Port Configuration

In `astro.config.mjs`:

```javascript
server: {
  port: 3000,
}
```

Change the port as needed.

## Components

### MainLayout

Base layout for all admin pages.

```astro
---
import MainLayout from '@/layouts/MainLayout.astro';
---

<MainLayout title="Page Title">
  <!-- Your content -->
</MainLayout>
```

### Header

Top navigation header component.

### Sidebar

Left sidebar navigation component.

## Testing

### Unit Tests

```bash
npm test
```

### E2E Tests

```bash
npm run test:e2e
```

## Deployment

1. Build the project:
   ```bash
   npm run build
   ```

2. Deploy the `dist/` directory to your hosting provider.

3. Ensure environment variables are set in your production environment.

## Internationalization (i18n)

Translations are located in the `messages/` directory:

- `messages/en.json` - English translations
- `messages/es.json` - Spanish translations

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests and type checking
4. Submit a pull request

## Resources

- [Astro Documentation](https://docs.astro.build)
- [Astro Community](https://astro.build/chat)

## License

MIT

## Support

For issues or questions, please create an issue on GitHub or contact the development team.
