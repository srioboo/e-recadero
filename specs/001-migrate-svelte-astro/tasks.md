---
description: "Task list for Migrate Admin & Front from Svelte to Astro"
---

# Tasks: Migrate Admin & Front from Svelte to Astro

**Input**: Design documents from `/specs/001-migrate-svelte-astro/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Test tasks included for critical paths; E2E tests for user-facing functionality

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Project Cleanup & Prerequisites)

**Purpose**: Remove old Svelte projects and prepare for Astro migration

- [x] T001 Delete old admin Svelte project directory and clear artifacts from `admin/`
- [x] T002 Delete old front Svelte project directory and clear artifacts from `front/`
- [x] T003 [P] Preserve existing translations by backing up `messages/en.json` and `messages/es.json`

---

## Phase 2: Foundational (Shared Infrastructure - Blocking Prerequisites)

**Purpose**: Core dependencies and shared configuration that MUST be complete before ANY user story can proceed

⚠️ **CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 Install Node.js 18+ and verify with `node --version` and `npm --version`
- [x] T005 [P] Create `admin/package.json` with Astro 4.x, TypeScript 5.x, Vitest, and Playwright dependencies
- [x] T006 [P] Create `front/package.json` with identical dependencies as admin (`admin/package.json`)
- [x] T007 [P] Generate `admin/astro.config.mjs` with TypeScript strict mode, port 3000, and SSR/SSG configuration
- [x] T008 [P] Generate `front/astro.config.mjs` with TypeScript strict mode, port 3001, and SSR/SSG configuration
- [x] T009 [P] Create `admin/tsconfig.json` with strict mode enabled and path aliases (`@/*` → `src/*`)
- [x] T010 [P] Create `front/tsconfig.json` matching admin configuration (path aliases, strict mode)
- [x] T011 [P] Create `.env.local` files for both projects with `VITE_API_URL=http://localhost:8000`
- [x] T012 [P] Initialize `.gitignore` for both projects (node_modules, dist, .astro, .env.local)

**Checkpoint**: Foundation ready - User story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Set up Astro Admin Project (Priority: P1) 🎯 MVP

**Goal**: Initialize a production-ready Astro project for the admin dashboard with proper TypeScript configuration

**Independent Test**: `npm run dev` starts dev server on port 3000 and TypeScript compiles without errors; access http://localhost:3000 to verify app loads

### Tests for User Story 1

- [x] T013 [P] [US1] Contract test: Verify admin project structure matches spec in `admin/` directory structure validation
- [x] T014 [P] [US1] E2E test: Dev server startup and basic page load in `admin/tests/e2e/dev-server.spec.ts`

### Implementation for User Story 1

- [x] T015 [US1] Create `admin/src/pages/index.astro` as home page entry point
- [x] T016 [US1] Create `admin/src/layouts/MainLayout.astro` as base layout wrapper
- [x] T017 [P] [US1] Create `admin/src/components/Header.astro` for navigation header in `admin/src/components/Header.astro`
- [x] T018 [P] [US1] Create `admin/src/components/Sidebar.astro` for admin sidebar navigation in `admin/src/components/Sidebar.astro`
- [x] T019 [P] [US1] Create `admin/src/lib/types.ts` with TypeScript interfaces for admin data models
- [x] T020 [US1] Create `admin/public/` directory with favicon and static assets
- [x] T021 [US1] Update `admin/package.json` scripts: `dev`, `build`, `preview`, `check` (TypeScript verification)
- [x] T022 [US1] Verify TypeScript compilation with `npm run check` in `admin/` with zero errors
- [x] T023 [US1] Create `admin/README.md` with setup instructions and development guidelines

**Checkpoint**: User Story 1 complete - Admin Astro project fully initialized and running on port 3000

---

## Phase 4: User Story 2 - Set up Astro Front Project (Priority: P1)

**Goal**: Initialize matching Astro project for public-facing app using identical standards as admin

**Independent Test**: `npm run dev` starts dev server on port 3001 without port conflicts; verify http://localhost:3001 loads and both admin (3000) and front (3001) run simultaneously

### Tests for User Story 2

- [x] T024 [P] [US2] Contract test: Verify front project structure in `front/` matches admin configuration
- [x] T025 [P] [US2] E2E test: Dev server startup and parallel execution with admin in `front/tests/e2e/dev-server.spec.ts`

### Implementation for User Story 2

- [x] T026 [US2] Create `front/src/pages/index.astro` as home page entry point
- [x] T027 [US2] Create `front/src/layouts/MainLayout.astro` as base layout wrapper
- [x] T028 [P] [US2] Create `front/src/components/Navigation.astro` for top navigation in `front/src/components/Navigation.astro`
- [x] T029 [P] [US2] Create `front/src/components/Footer.astro` for footer section in `front/src/components/Footer.astro`
- [x] T030 [P] [US2] Create `front/src/lib/types.ts` with TypeScript interfaces for front data models
- [x] T031 [US2] Create `front/public/` directory with favicon and static assets
- [x] T032 [US2] Update `front/package.json` scripts: `dev`, `build`, `preview`, `check` (TypeScript verification)
- [x] T033 [US2] Verify TypeScript compilation with `npm run check` in `front/` with zero errors
- [x] T034 [US2] Create `front/README.md` with setup instructions and development guidelines
- [x] T035 [US2] Verify both projects run simultaneously: admin on 3000 and front on 3001 without conflicts

**Checkpoint**: User Story 2 complete - Front Astro project fully initialized and running on port 3001

---

## Phase 5: User Story 3 - Configure i18n Integration (Priority: P2)

**Goal**: Integrate internationalization support using existing messages (en.json, es.json) in both projects

**Independent Test**: Language switcher component displays both English and Spanish; switching languages causes UI text to update without page reload

### Tests for User Story 3

- [x] T036 [P] [US3] Unit test: i18n helper functions in `admin/tests/unit/i18n.test.ts` and `front/tests/unit/i18n.test.ts`
- [x] T037 [P] [US3] E2E test: Language switching functionality in both projects in `admin/tests/e2e/i18n.spec.ts` and `front/tests/e2e/i18n.spec.ts`

### Implementation for User Story 3

- [x] T038 [P] [US3] Copy i18n messages to admin: `cp messages/en.json admin/messages/en.json` and `cp messages/es.json admin/messages/es.json`
- [x] T039 [P] [US3] Copy i18n messages to front: `cp messages/en.json front/messages/en.json` and `cp messages/es.json front/messages/es.json`
- [x] T040 [US3] Create `admin/src/lib/i18n.ts` with locale context and message loading helper functions
- [x] T041 [US3] Create `admin/src/components/LocaleSwitcher.astro` for language selection in admin
- [x] T042 [US3] Integrate LocaleSwitcher into `admin/src/layouts/MainLayout.astro` with locale switching
- [x] T043 [US3] Create `front/src/lib/i18n.ts` with locale context and message loading helper functions
- [x] T044 [US3] Create `front/src/components/LocaleSwitcher.astro` for language selection in front
- [x] T045 [US3] Integrate LocaleSwitcher into `front/src/layouts/MainLayout.astro` with locale switching
- [x] T046 [US3] Create locale persistence in localStorage: store user's language preference in `admin/src/lib/storage.ts` and `front/src/lib/storage.ts`
- [x] T047 [US3] Implement fallback mechanism: default to English if locale not found in messages in both i18n helpers
- [ ] T048 [US3] Create `admin/src/pages/[locale]/index.astro` for locale-prefixed routing (optional, if SSR needed)
- [x] T049 [US3] Verify i18n messages load correctly in both projects with no missing key warnings

**Checkpoint**: User Story 3 complete - i18n fully configured in both projects with language switching working

---

## Phase 6: User Story 4 - Configure Build & Deployment Pipeline (Priority: P2)

**Goal**: Set up production-ready build scripts, environment configuration, and distribution artifacts

**Independent Test**: Run `npm run build` in both projects; verify `dist/` directories contain optimized production builds that load correctly when served

### Tests for User Story 4

- [x] T050 [P] [US4] Build test: Production build verification in `admin/` and `front/` generates valid `dist/` output
- [x] T051 [P] [US4] E2E test: Preview built app with `npm run preview` in both projects in `admin/tests/e2e/production.spec.ts` and `front/tests/e2e/production.spec.ts`

### Implementation for User Story 4

- [x] T052 [P] [US4] Create `admin/.env.production` with production API URL and optimization settings
- [x] T053 [P] [US4] Create `front/.env.production` with production API URL and optimization settings
- [ ] T054 [US4] Configure build optimization in `admin/astro.config.mjs`: minify, compress, tree-shake
- [ ] T055 [US4] Configure build optimization in `front/astro.config.mjs`: minify, compress, tree-shake
- [x] T056 [P] [US4] Create `admin/package.json` build script: `npm run build` outputs to `dist/` with size <500KB
- [x] T057 [P] [US4] Create `front/package.json` build script: `npm run build` outputs to `dist/` with size <300KB
- [x] T058 [US4] Create deployment documentation in `admin/DEPLOYMENT.md` with build, preview, and deployment steps
- [x] T059 [US4] Create deployment documentation in `front/DEPLOYMENT.md` with build, preview, and deployment steps
- [x] T060 [US4] Add `preview` script to both projects for testing production builds locally
- [x] T061 [US4] Verify environment variable injection works: `VITE_API_URL` accessible in built app via `import.meta.env.VITE_API_URL`
- [x] T062 [US4] Create build performance report: document build times and bundle sizes in `specs/001-migrate-svelte-astro/BUILD_REPORT.md`

**Checkpoint**: User Story 4 complete - Both projects have production-ready builds and deployment documentation

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Quality gates, documentation, and cleanup

- [x] T063 [P] Create project root `scripts/dev.sh` for running both dev servers concurrently
- [x] T064 [P] Create project root `scripts/build.sh` for building both projects
- [x] T065 Create `MIGRATION_NOTES.md` documenting changes from Svelte to Astro architecture
- [x] T066 Update `.github/copilot-instructions.md` with latest plan/research/data-model links
- [x] T067 Ensure both projects pass Lighthouse audit > 90 score for performance
- [x] T068 [P] Add `.gitattributes` for consistent line endings across both projects
- [x] T069 Create `CONTRIBUTING.md` for developer guidelines
- [x] T070 Final integration test: Run E2E tests for entire feature in both projects

**Checkpoint**: Migration complete - All artifacts verified, documentation updated, project ready for staging/production

---

## Dependencies & Parallelization

### User Story Dependencies
```
Phase 1 (Setup) → Phase 2 (Foundational)
                ↓
        Phase 3 (US1) ← Phase 2 (all must complete first)
        Phase 4 (US2) ← Phase 2
        Phase 5 (US3) ← Phase 3 & Phase 4 (both projects needed)
        Phase 6 (US4) ← Phase 3 & Phase 4
        Phase 7 (Polish) ← all previous phases
```

### Parallel Execution Opportunities

**Round 1 - Phase 2 (Foundational)**: Tasks T005-T012 can run in parallel (different config files, no dependencies)

**Round 2 - Phase 3 & 4 (US1 & US2)**: Can be worked on simultaneously after Phase 2 completes
  - Admin tasks (T015-T023) independent from Front tasks (T026-T035)
  - Component creation (T017, T018, T028, T029) can be parallelized

**Round 3 - Phase 5 & 6 (US3 & US4)**: Can begin once US1 & US2 are bootstrapped
  - i18n (T038-T049) independent from Build pipeline (T052-T062)

### Estimated Timeline
- **Phase 1-2**: 3-4 hours (setup infrastructure)
- **Phase 3-4**: 6-8 hours (initialize both projects in parallel)
- **Phase 5**: 4-5 hours (i18n integration)
- **Phase 6**: 3-4 hours (build pipeline)
- **Phase 7**: 2-3 hours (polish & testing)

**Total MVP Estimate**: 18-24 hours for complete migration

---

## Independent Test Criteria (Per User Story)

### US1 - Admin Admin Project
✅ **Pass Criteria**:
- Dev server starts on port 3000 in <5 seconds
- http://localhost:3000 displays app without errors
- TypeScript compiles with zero errors (`npm run check`)
- Components resolve and render correctly

### US2 - Front Project
✅ **Pass Criteria**:
- Dev server starts on port 3001 in <5 seconds
- http://localhost:3001 displays app without errors
- Both admin (3000) and front (3001) run simultaneously without port conflicts
- TypeScript compiles with zero errors (`npm run check`)

### US3 - i18n Integration
✅ **Pass Criteria**:
- Language switcher component available in both projects
- Switching language updates UI text in real-time without refresh
- Both en.json and es.json translations load without errors
- Fallback to English works for missing keys

### US4 - Build & Deployment Pipeline
✅ **Pass Criteria**:
- `npm run build` completes in <30 seconds for both projects
- Optimized builds generated in `dist/` directories
- Build sizes: admin <500KB, front <300KB
- `npm run preview` serves production builds correctly
- Lighthouse audit score >90 for performance

---

## Suggested MVP Scope

**Minimum Viable Product**: Complete Phase 1-4 (User Stories 1-2 + i18n + build)

This delivers:
- ✅ Both Astro projects fully functional
- ✅ i18n working in both projects
- ✅ Production builds ready
- ✅ Independent, testable increment

**Nice-to-have**: Phase 5 (Polish & deployment automation)
