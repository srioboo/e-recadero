# Contract: Admin Astro Project

**Date**: 2026-05-09  
**Project**: admin/  
**Type**: Frontend Application Contract  
**Status**: Design

## Public Exports

### Components

#### `<DashboardLayout>`
**Purpose**: Main layout wrapper for admin pages  
**Props**:
```typescript
interface DashboardLayoutProps {
  title: string
  activeNav?: string
  children: ReactNode
}
```

**Exports**:
- `DashboardLayout` - Main component

#### `<AdminForm>`
**Purpose**: Reusable form component with validation  
**Props**:
```typescript
interface AdminFormProps {
  fields: FormField[]
  onSubmit: (data: Record<string, any>) => Promise<void>
  isLoading?: boolean
  submitLabel?: string
}
```

### API Routes (Server Endpoints)

#### `GET /api/auth/me`
**Purpose**: Get current admin session info  
**Response** (200):
```json
{
  "id": "user-123",
  "name": "Admin User",
  "email": "admin@example.com",
  "role": "admin"
}
```

#### `POST /api-proxy/*`
**Purpose**: Proxy authenticated requests to backend  
**Headers**: Requires `Authorization: Bearer <token>`  
**Behavior**: Forwards request to backend with authentication

### Page Routes

- `/` - Dashboard home
- `/settings` - Admin settings
- `/[resource]` - Dynamic resource pages

### Context Providers

#### `<AdminAuthProvider>`
Provides current user and auth functions to component tree

**Exports**:
```typescript
type AdminAuthContext = {
  user: AdminUser | null
  isLoading: boolean
  logout: () => Promise<void>
}
```

## Consumed External Contracts

### Backend API Contract
Requires authentication headers on all requests.

**Base URL**: `${import.meta.env.VITE_API_URL}`

Required endpoints:
- `POST /auth/login` - User authentication
- `GET /auth/me` - Get current user
- `POST /auth/logout` - Logout user
- `GET /admin/dashboard` - Dashboard data
- `GET /admin/resources` - Resource listing
- `PATCH /admin/resources/:id` - Update resource

## Build Artifacts

### Development
- **Command**: `npm run dev`
- **Output**: Dev server on `http://localhost:3000`
- **HMR**: Enabled via Astro

### Production
- **Command**: `npm run build`
- **Output**: `dist/` directory with static/hybrid files
- **Size Target**: < 500KB (main bundle + CSS)

## Environment Variables

| Variable | Required | Example |
|----------|----------|---------|
| `VITE_API_URL` | Yes | `https://api.example.com` |

## Dependencies Exposed to Calling Code

None - this is a standalone frontend application.

## Testing Contract

### Unit Test Coverage
- Components: 80%+
- Utils: 90%+

### E2E Test Scenarios
- Admin login flow
- Dashboard view
- Form submission with validation

**Test Framework**: Playwright  
**Entry Point**: `tests/e2e/admin.spec.ts`
