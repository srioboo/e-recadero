# Implementation Plan: Migrate Admin & Front from Svelte to Astro

**Branch**: `001-migrate-svelte-astro` | **Date**: 2026-05-09 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/001-migrate-svelte-astro/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Replace existing Svelte-based admin and front projects with new Astro-based implementations. Both projects are currently empty scaffolding without production code, enabling a clean migration. The new projects will maintain i18n support (English/Spanish), development server capabilities, and production build pipelines using Astro's modern build tooling.

## Technical Context

**Language/Version**: Node.js 18+ with TypeScript 5.x  
**Primary Dependencies**: Astro 4.x, @astrojs/react (or framework adapter), Vite (build engine)  
**Storage**: N/A (frontend projects, data handled by backend API)  
**Testing**: Vitest for unit tests, Playwright for E2E tests  
**Target Platform**: Web browsers (desktop + mobile), WASM-capable via Astro  
**Project Type**: Web application (dual frontend: admin dashboard + public-facing app)  
**Performance Goals**: First Contentful Paint < 2s, Lighthouse score > 90, build time < 30s  
**Constraints**: Must maintain existing i18n structure (messages/en.json, messages/es.json), reuse existing authentication from backend  
**Scale/Scope**: 2 independent Astro projects, ~50+ components total across both, SSR/SSG hybrid rendering

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Alignment with Project Principles

✅ **Modularidad y Composabilidad**: Astro enforces component-based architecture; each feature will be built as independent, reusable components
✅ **Documentation Driven**: Both projects will include README.md, component documentation, and Storybook-style component catalog
✅ **Backwards Compatibility**: Since existing Svelte projects have no production code, migration is clean cut; no breaking changes to preserve
✅ **Developer Experience**: Astro provides excellent DX with fast dev server, HMR, TypeScript support, and clear file-based routing
✅ **Semantic Versioning**: Project will follow sem-ver starting from 1.0.0 (initial Astro implementation)
✅ **Multi-language Support**: Node.js/npm ensures cross-platform support; TypeScript code is language-agnostic
✅ **Security by Design**: Astro's static/hybrid rendering reduces attack surface; CSP headers will be configured in deployment

**Gate Status**: ✅ PASS - No principle violations detected

## Project Structure

### Documentation (this feature)

```text
specs/001-migrate-svelte-astro/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command) - TBD
├── data-model.md        # Phase 1 output (/speckit.plan command) - TBD
├── quickstart.md        # Phase 1 output (/speckit.plan command) - TBD
├── contracts/           # Phase 1 output (/speckit.plan command) - TBD
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root - Two Independent Astro Projects)

**Option Selected**: Dual Frontend Web Applications (admin + front)

```text
admin/                           # Admin dashboard (replaces old Svelte project)
├── src/
│   ├── components/             # Reusable Astro components
│   ├── pages/                  # File-based routing
│   ├── layouts/                # Layout components
│   └── lib/                    # Utilities, i18n helpers
├── public/                     # Static assets
├── messages/                   # i18n translations (en.json, es.json)
├── package.json
├── astro.config.mjs
├── tsconfig.json
├── vite.config.ts
└── tests/
    ├── unit/                   # Unit tests (Vitest)
    └── e2e/                    # E2E tests (Playwright)

front/                           # Public-facing app (replaces old Svelte project)
├── src/
│   ├── components/             # Reusable Astro components
│   ├── pages/                  # File-based routing
│   ├── layouts/                # Layout components
│   └── lib/                    # Utilities, i18n helpers
├── public/                     # Static assets
├── messages/                   # i18n translations (en.json, es.json)
├── package.json
├── astro.config.mjs
├── tsconfig.json
├── vite.config.ts
└── tests/
    ├── unit/                   # Unit tests (Vitest)
    └── e2e/                    # E2E tests (Playwright)
```

**Structure Decision**: Both projects follow identical Astro conventions to ensure consistency across the codebase. Each project is independently deployable and runnable. The `messages/` directory reuses existing i18n files from the previous Svelte setup. Build outputs will be in `dist/` directories for each project.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
