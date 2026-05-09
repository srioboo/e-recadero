# Data Model: Migrate Admin & Front from Svelte to Astro

**Date**: 2026-05-09  
**Feature**: Migrate Admin & Front from Svelte to Astro  
**Scope**: Design entities, relationships, and data flows for both Astro projects

## Entities

### Admin Project Entities

#### 1. **DashboardLayout**
- **Purpose**: Main shell/container for admin dashboard pages
- **Attributes**: 
  - `title: string` - Page title
  - `sidebar: SidebarConfig` - Navigation structure
  - `breadcrumbs: Breadcrumb[]` - Navigation trail
- **Relationships**: Contains multiple admin pages and components
- **Validation**: Title required, non-empty

#### 2. **AdminUser (from backend token)**
- **Purpose**: Represents authenticated admin user (from backend)
- **Attributes**:
  - `id: string` - User ID from backend
  - `name: string` - Display name
  - `email: string` - Email address
  - `role: 'admin' | 'editor' | 'viewer'` - Permission level
  - `permissions: string[]` - Feature permissions
- **Relationships**: Associated with admin session; used for authorization checks
- **Validation**: Role must be one of allowed values

#### 3. **NavMenu**
- **Purpose**: Sidebar/top navigation menu structure
- **Attributes**:
  - `items: MenuItem[]` - Menu items
  - `title: string` - Menu section title
  - `icon: string` - Optional icon identifier
- **Relationships**: Contains MenuItem entities
- **Validation**: At least one item required

#### 4. **FormComponents (generic)**
- **Purpose**: Reusable form field components (Input, Select, Checkbox, etc.)
- **Attributes**:
  - `name: string` - Field identifier
  - `label: string` - Display label
  - `type: string` - Field type
  - `required: boolean` - Is field mandatory
  - `validation: ValidationRule[]` - Input validation rules
- **Validation**: Label and name required; type must be valid

### Front Project Entities

#### 1. **PublicPage**
- **Purpose**: Main page structure for public-facing site
- **Attributes**:
  - `slug: string` - URL slug
  - `title: string` - Page title
  - `description: string` - Meta description for SEO
  - `content: string` - Main content
  - `metadata: PageMetadata` - Open Graph, Twitter cards, etc.
- **Relationships**: Can contain multiple sections and components
- **Validation**: Slug unique, title non-empty, description <160 chars

#### 2. **I18nContent**
- **Purpose**: Internationalized content container
- **Attributes**:
  - `locale: 'en' | 'es'` - Current language
  - `content: Record<string, string>` - Translated strings
  - `fallback: 'en'` - Fallback language if translation missing
- **Relationships**: Used by all pages and components
- **Validation**: Locale must be supported (en or es)

#### 3. **Navigation**
- **Purpose**: Top navigation / header menu
- **Attributes**:
  - `links: NavLink[]` - Navigation links
  - `logo: string` - Logo URL/path
  - `cta: CTAButton` - Call-to-action button
- **Relationships**: Global component shared across all pages
- **Validation**: At least one link required

#### 4. **ContentSection**
- **Purpose**: Reusable page section (hero, features, testimonials, etc.)
- **Attributes**:
  - `type: 'hero' | 'features' | 'testimonials' | 'cta' | 'custom'` - Section type
  - `title: string` - Section heading
  - `content: string` - Section body
  - `backgroundColor: string` - Optional styling
  - `ctas: Button[]` - Call-to-action buttons
- **Relationships**: Composed into PublicPage
- **Validation**: Type must be valid; title non-empty

### Shared Entities (Both Projects)

#### 1. **LocaleContext**
- **Purpose**: Represents current user locale/language preference
- **Attributes**:
  - `current: 'en' | 'es'` - Active language
  - `available: ['en', 'es']` - Supported languages
  - `default: 'en'` - Default language
- **Relationships**: Global context/provider used by all components
- **Validation**: Current must be one of available

#### 2. **UITheme**
- **Purpose**: Application theme configuration (colors, spacing, fonts)
- **Attributes**:
  - `primary: string` - Primary color hex
  - `secondary: string` - Secondary color hex
  - `spacing: Record<string, string>` - Spacing scale
  - `typography: TypographyConfig` - Font configuration
- **Relationships**: Applied globally via CSS custom properties or Tailwind config
- **Validation**: All colors valid hex format

## State Management Strategy

### Admin Project
- **Page State**: Per-page local state (React hooks via Astro components)
- **Global State**: Minimal (just auth token, user info) - store in localStorage/sessionStorage
- **API Communication**: Fetch via server-side functions (Astro server endpoints)

### Front Project
- **Page State**: Per-page local state
- **Global State**: Locale preference (stored in localStorage)
- **API Communication**: Fetch from client-side or via SSR when needed for SEO

## Data Flow Diagrams

### Admin Auth Flow
```
User Login Form → POST /api/login → Backend JWT Token
  ↓
Store in sessionStorage → Set AdminUser context
  ↓
Redirect to Dashboard → Dashboard loads public + admin data
```

### i18n Flow (Both Projects)
```
User selects language → Update LocaleContext
  ↓
Load messages/{locale}.json → Provide to components
  ↓
Components render with LocaleContext.messages[key]
```

## Database / Server Calls (Frontend → Backend)

### Admin Project Expected Endpoints
- `GET /api/auth/me` - Get current user info
- `POST /api/logout` - Logout current user
- `GET /api/admin/resources` - Fetch admin resources
- `PATCH /api/admin/resources/:id` - Update resource

### Front Project Expected Endpoints
- `GET /api/public/content` - Fetch public content
- `GET /api/public/pages/:slug` - Fetch page by slug

## Validation & Integrity Rules

### Admin Project
- User must be authenticated (have valid JWT token)
- Role-based access control enforced server-side before content is exposed
- Form validators must prevent invalid data submission

### Front Project
- i18n content must always have fallback (never show missing keys)
- SEO metadata must be present for all public pages
- Mobile responsive design required for all sections

## Version & Change History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-05-09 | Initial data model for Astro migration |
