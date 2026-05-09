# Contributing to E-Recadero

Thank you for your interest in contributing to E-Recadero! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Style](#code-style)
- [Commit Messages](#commit-messages)
- [Pull Requests](#pull-requests)
- [Testing](#testing)
- [Documentation](#documentation)
- [Release Process](#release-process)

---

## Code of Conduct

We are committed to providing a welcoming and inclusive environment. Please be respectful and constructive in all interactions.

**Expected behavior**:
- Be respectful and inclusive
- Use welcoming language
- Be patient with others
- Focus on constructive criticism

**Unacceptable behavior**:
- Harassment, discrimination, or abuse
- Trolling, personal attacks
- Unwelcome sexual attention
- Spam or self-promotion

---

## Getting Started

### Prerequisites

- Node.js 18+ ([Download](https://nodejs.org))
- npm 9+ (included with Node.js)
- Git ([Download](https://git-scm.com))
- Text editor (VS Code recommended)

### Setup Local Development

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-org/e-recadero.git
   cd e-recadero
   ```

2. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Install dependencies**:
   ```bash
   cd admin && npm install
   cd ../front && npm install
   cd ..
   ```

4. **Start development servers**:
   ```bash
   ./scripts/dev.sh
   ```

   - Admin: http://localhost:3000
   - Front: http://localhost:3001

---

## Development Workflow

### Project Structure

```
e-recadero/
├── admin/              # Admin dashboard (Astro)
│   ├── src/
│   │   ├── components/
│   │   ├── layouts/
│   │   ├── lib/
│   │   ├── pages/
│   │   └── public/
│   └── package.json
├── front/              # Public website (Astro)
│   ├── src/
│   │   ├── components/
│   │   ├── layouts/
│   │   ├── lib/
│   │   ├── pages/
│   │   └── public/
│   └── package.json
├── back/               # Backend (Java/Spring)
├── messages/           # i18n translations
├── scripts/            # Helper scripts
└── specs/              # Specification documents
```

### Working on Features

1. **Create a feature branch from `main`**:
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/my-feature
   ```

2. **Make your changes** in the appropriate directory:
   - Admin dashboard features → `admin/src/`
   - Public website features → `front/src/`
   - Shared utilities → `admin/src/lib/` or `front/src/lib/`

3. **Run tests**:
   ```bash
   cd admin && npm run check
   cd ../front && npm run check
   ```

4. **Check for lint errors**:
   ```bash
   cd admin && npm run lint || true
   cd ../front && npm run lint || true
   ```

5. **Test in browser**:
   - Admin: http://localhost:3000
   - Front: http://localhost:3001

6. **Build for production**:
   ```bash
   ./scripts/build.sh
   ```

---

## Code Style

### Astro Components

```astro
---
// Imports first
import MainLayout from '@/layouts/MainLayout.astro';
import { utility } from '@/lib/helpers';

// Interface definitions
interface Props {
  title: string;
  variant?: 'primary' | 'secondary';
}

// Component logic
const { title, variant = 'primary' } = Astro.props;
const processedData = await utility(title);
---

<!-- Template -->
<MainLayout title={title}>
  <h1>{title}</h1>
  <p>Content here</p>
</MainLayout>

<!-- Styles -->
<style define:vars={{ variant }}>
  h1 {
    font-size: 2rem;
    color: var(--color-primary);
  }
</style>
```

**Rules**:
- Use TypeScript for all new code
- Define `Props` interface for each component
- Use descriptive variable names
- Order imports: framework → local → types
- Use `define:vars` for CSS variables from props

### TypeScript

```typescript
// Use strict typing
const getValue = (key: string): string => {
  // Implementation
  return '';
};

// Use interfaces for complex types
interface User {
  id: string;
  name: string;
  email: string;
  role: 'admin' | 'user';
}

// Use enums for constants
enum Theme {
  Light = 'light',
  Dark = 'dark',
}

// Use generics for reusability
function identity<T>(value: T): T {
  return value;
}
```

**Rules**:
- Enable `strict: true` in `tsconfig.json`
- No `any` types without justification
- Use `interface` for object shapes
- Use `type` for unions/aliases
- Document complex functions

### CSS

```css
/* Use modern CSS features */
.component {
  display: grid;
  gap: 1rem;
  margin-block: 1rem;
}

/* Mobile-first approach */
@media (min-width: 768px) {
  .component {
    grid-template-columns: 2fr 1fr;
  }
}

/* Use CSS variables for theming */
:root {
  --color-primary: #2c3e50;
  --color-accent: #3498db;
  --spacing-unit: 0.5rem;
}
```

**Rules**:
- Follow mobile-first design
- Use CSS Grid/Flexbox instead of floats
- Scope styles to components
- Use CSS variables for theming
- Avoid `!important`

---

## Commit Messages

Use clear, descriptive commit messages following the format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style (formatting, semicolons)
- **refactor**: Code refactoring without feature changes
- **perf**: Performance improvements
- **test**: Test additions/modifications
- **chore**: Build, dependencies, tooling

### Examples

```bash
git commit -m "feat(admin): add dashboard analytics component"
git commit -m "fix(front): resolve mobile nav overlay z-index"
git commit -m "docs: update migration notes for Astro"
git commit -m "refactor(shared): simplify i18n helper functions"
```

---

## Pull Requests

### Before Submitting

1. **Ensure code quality**:
   ```bash
   npm run check      # TypeScript
   npm run lint       # ESLint (if configured)
   npm run test       # Unit tests (if available)
   npm run test:e2e   # E2E tests (if configured)
   ```

2. **Update documentation**:
   - Update README.md if adding new features
   - Update MIGRATION_NOTES.md for architecture changes
   - Add comments for complex logic

3. **Clean commit history**:
   ```bash
   git rebase -i main    # Squash related commits
   git push origin feature/my-feature --force-with-lease
   ```

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Documentation update
- [ ] Performance improvement

## Related Issues
Closes #123

## Testing Done
- [ ] Unit tests added/updated
- [ ] Manual testing on localhost
- [ ] Tested on multiple browsers

## Checklist
- [ ] Code follows style guide
- [ ] Documentation updated
- [ ] No breaking changes
- [ ] Tested in admin and front
```

---

## Testing

### Unit Tests

Create test files alongside components:

```typescript
// lib/helper.ts
export function sum(a: number, b: number): number {
  return a + b;
}

// lib/helper.test.ts
import { describe, it, expect } from 'vitest';
import { sum } from './helper';

describe('sum', () => {
  it('adds two numbers correctly', () => {
    expect(sum(2, 3)).toBe(5);
  });
});
```

Run tests:
```bash
npm test
```

### E2E Tests

For user interactions, use Playwright:

```typescript
// e2e/navigation.spec.ts
import { test, expect } from '@playwright/test';

test('navigate to about page', async ({ page }) => {
  await page.goto('http://localhost:3001');
  await page.click('a[href="/about"]');
  await expect(page).toHaveTitle(/About/);
});
```

Run E2E tests:
```bash
npm run test:e2e
```

---

## Documentation

### Code Comments

```typescript
/**
 * Fetch user data from the API
 * @param userId - The user's unique identifier
 * @returns User object or null if not found
 */
export async function getUser(userId: string): Promise<User | null> {
  // Implementation
}
```

### README Updates

Update relevant README.md files when:
- Adding new features
- Changing configuration
- Updating dependencies
- Modifying file structure

Example:
```markdown
## New Feature: Language Switching

Users can now switch between English and Spanish. Preference is saved in localStorage.

**Usage**:
```

### MIGRATION_NOTES.md

Update this file for architecture changes:
- New patterns or conventions
- Performance improvements
- Breaking changes
- Deprecations

---

## Release Process

### Version Numbering

Uses [Semantic Versioning](https://semver.org/):
- **MAJOR**: Breaking changes (1.0.0 → 2.0.0)
- **MINOR**: New features (1.0.0 → 1.1.0)
- **PATCH**: Bug fixes (1.0.0 → 1.0.1)

### Release Steps

1. **Update version** in `package.json`:
   ```json
   "version": "1.1.0"
   ```

2. **Update CHANGELOG**:
   ```markdown
   ## [1.1.0] - 2026-05-10
   ### Added
   - Feature X
   ### Fixed
   - Bug Y
   ```

3. **Create release commit**:
   ```bash
   git commit -m "chore: release v1.1.0"
   ```

4. **Create git tag**:
   ```bash
   git tag -a v1.1.0 -m "Release v1.1.0"
   git push origin v1.1.0
   ```

5. **Build and deploy**:
   ```bash
   ./scripts/build.sh
   # Deploy dist/ to production
   ```

---

## Common Tasks

### Adding a New Page (Admin)

1. Create file: `admin/src/pages/new-page.astro`
2. Import layout: `import MainLayout from '@/layouts/MainLayout.astro'`
3. Add to navigation: Update `admin/src/layouts/MainLayout.astro`
4. Test: http://localhost:3000/new-page

### Adding i18n Strings

1. Add to `messages/en.json`:
   ```json
   {
     "feature": {
       "newString": "New String"
     }
   }
   ```

2. Add to `messages/es.json`:
   ```json
   {
     "feature": {
       "newString": "Nueva Cadena"
     }
   }
   ```

3. Use in component:
   ```astro
   ---
   import { getMessage } from '@/lib/i18n';
   const t = (key) => getMessage(key, locale);
   ---
   <p>{t('feature.newString')}</p>
   ```

### Debugging

**Environment variables**:
```bash
# Check what's loaded
node -e "console.log(process.env.VITE_API_URL)"
```

**Browser debugging**:
```javascript
// In browser console
console.log(import.meta.env)
```

**VS Code debugging**:
Set breakpoints and use "Run and Debug" (Ctrl+Shift+D)

---

## Getting Help

- **Documentation**: Read [Astro Docs](https://docs.astro.build/)
- **Issues**: Search existing [GitHub Issues](https://github.com/your-org/e-recadero/issues)
- **Discussions**: Join team chat/Slack
- **PRs**: Our team will review and provide feedback

---

## Recognition

Contributors are recognized in:
- Repository README.md (Contributors section)
- Release notes
- Team announcements

Thank you for contributing to E-Recadero! 🚀
