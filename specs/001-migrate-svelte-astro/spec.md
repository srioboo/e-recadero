# Feature Specification: Migrate Admin & Front from Svelte to Astro

**Feature Branch**: `001-migrate-svelte-astro`  
**Created**: 2026-05-09  
**Status**: Draft  
**Input**: User description: "Cambiar admin y front que están basados en svelte por proyectos basados en astro. Actualmente están solo creados y no tienen contenido desarrollado, por tanto, se pueden borrar y sustituir"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Set up Astro Admin Project (Priority: P1)

Initialize a new Astro project for the admin dashboard with proper TypeScript configuration, component structure, and build setup. This establishes the foundation for all admin functionality.

**Why this priority**: Without a properly configured admin project, no admin features can be developed. This is the critical first deliverable.

**Independent Test**: Successfully run `npm run dev` and access the admin dashboard at `http://localhost:3000` with Astro development server running and TypeScript compilation working.

**Acceptance Scenarios**:

1. **Given** an empty admin directory (old Svelte project removed), **When** Astro project is initialized, **Then** project structure is created with TypeScript support enabled and builds without errors
2. **Given** Astro admin project is set up, **When** `npm run dev` is executed, **Then** development server starts on a specified port and serves the dashboard
3. **Given** Astro project exists, **When** components are created in `src/components/`, **Then** they are properly resolved and rendered by Astro

---

### User Story 2 - Set up Astro Front Project (Priority: P1)

Initialize a new Astro project for the front-end application with the same configuration standards as the admin project, ensuring consistency across the codebase.

**Why this priority**: Like the admin project, this is a foundational requirement. Both projects need to be set up before feature work can progress.

**Independent Test**: Successfully run `npm run dev` in the front project and access the app at `http://localhost:3001` with Astro development server running and full TypeScript support.

**Acceptance Scenarios**:

1. **Given** an empty front directory (old Svelte project removed), **When** Astro project is initialized, **Then** project structure matches admin configuration standards
2. **Given** Astro front project is set up, **When** `npm run dev` is executed, **Then** development server starts and serves the frontend
3. **Given** Astro front and admin projects exist, **When** both are running simultaneously, **Then** they run on different ports without conflicts

---

### User Story 3 - Configure i18n Integration (Priority: P2)

Integrate internationalization (i18n) support in both projects using existing `messages/` structure with translations for English and Spanish, migrating from the Svelte i18n configuration.

**Why this priority**: Both existing projects use i18n. P2 because it's important for usability but doesn't block core functionality.

**Independent Test**: Switch language in both admin and front apps and verify that UI text changes appropriately between English and Spanish without page reload.

**Acceptance Scenarios**:

1. **Given** i18n plugin is configured in both Astro projects, **When** default language is English, **Then** all UI text displays in English
2. **Given** user selects Spanish language, **When** language is changed, **Then** UI updates to display Spanish translations for all strings
3. **Given** missing translations exist, **When** page loads, **Then** fallback language (English) is used instead of showing missing translation keys

---

### User Story 4 - Configure Build & Deployment Pipeline (Priority: P2)

Set up build scripts, environment configuration, and deployment-ready configuration for both Astro projects to match the existing CI/CD expectations.

**Why this priority**: Required before deployment but can be done after core projects are functional. Enables testing and staging.

**Independent Test**: Run `npm run build` in both projects and verify production-ready output is generated in `dist/` directory without errors.

**Acceptance Scenarios**:

1. **Given** both projects are set up, **When** `npm run build` is executed, **Then** optimized production builds are created in `dist/` directories
2. **Given** environment variables are set (API_URL, etc.), **When** production build runs, **Then** environment values are correctly injected
3. **Given** built projects exist, **When** they are served, **Then** all assets load correctly and app functions as expected

---

### Edge Cases

- What happens if a user navigates while i18n is switching languages? (Should complete current request before switching)
- How does the build process handle missing translation keys? (Fallback to default language)
- What occurs if both dev servers try to use the same port? (Error should specify conflicting port and suggest alternative)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Admin project MUST be created using Astro with TypeScript support enabled
- **FR-002**: Front project MUST be created using Astro with TypeScript support enabled, matching admin configuration
- **FR-003**: Both projects MUST reuse existing `messages/` directory structure with en.json and es.json translations
- **FR-004**: Both projects MUST support development server with hot module replacement (HMR)
- **FR-005**: Both projects MUST generate production-ready builds via `npm run build`
- **FR-006**: Admin and Front projects MUST use different ports when running dev servers simultaneously (no port conflicts)
- **FR-007**: Both projects MUST preserve existing environment configuration patterns from original Svelte setup
- **FR-008**: TypeScript configuration MUST be consistent between both projects (same tsconfig.json patterns)

### Key Entities

- **Admin Project**: Astro-based dashboard application for e-recadero administration, replacing previous Svelte implementation
- **Front Project**: Astro-based public-facing application for e-recadero, replacing previous Svelte implementation
- **i18n Configuration**: Translation system supporting English (en) and Spanish (es) locales

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Both Astro projects initialize without errors and structure matches defined standards
- **SC-002**: Development servers for both projects start within 5 seconds and serve content without 404 errors
- **SC-003**: i18n configuration allows language switching with immediate UI updates in both projects
- **SC-004**: Production builds for both projects complete in under 30 seconds and produce optimized bundles
- **SC-005**: All existing translation keys (en.json, es.json) are accessible in both projects without fallback errors
- **SC-006**: TypeScript compilation succeeds with no errors in both projects
- **SC-007**: Both projects can run simultaneously without port or resource conflicts

## Assumptions

- Astro will be configured with compatible versions (latest stable or specified version matching project requirements)
- Existing `messages/` directory structure (en.json, es.json) remains unchanged and can be reused directly
- Both projects follow the same build/dev script naming conventions (`npm run dev`, `npm run build`)
- Node.js version is compatible with Astro requirements (18+ or as specified in package.json)
- Old Svelte projects (admin/ and front/) can be safely deleted as they contain no production code
- Development team has access to Astro documentation and community support
- No external APIs or backend changes are required for this migration
- Port assignments can be configured via environment or package.json scripts (e.g., 3000 for admin, 3001 for front)
