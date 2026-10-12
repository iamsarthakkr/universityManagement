# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev      # Start Vite dev server on port 3000; proxies /api/* to the backend on localhost:8080
npm run build    # tsc type-check + Vite build into dist/
npm run preview  # Serve the production build
npm run lint     # ESLint
npm test         # Vitest in watch mode
npm run test:run # Vitest once (CI)
npm run test:coverage # Vitest once with V8 coverage; fails below the thresholds
```

The backend lives in `../server/` (Spring Boot, runs on port 8080).

## Testing

Vitest + jsdom + Testing Library, configured in the `test` block of `vite.config.ts` (same `@/` alias as the app).

- Unit/component tests sit next to the code as `*.test.ts(x)`. App-level tests (routing, guards, startup) live in `tests/`.
- `tests/setup.ts` stubs `matchMedia` (live `matches`; `setPrefersDark()` to switch), `ResizeObserver` and pointer APIs for Radix, and resets localStorage, the app store, the query cache and the 401 handler between tests.
- Mock the network, not the modules: `mockFetch({ 'GET /path': jsonOk(body), 'POST /path': jsonError(400, 'msg') })` from `tests/mockFetch.ts` stubs global `fetch` and returns the recorded requests (method, path, headers, parsed body). Unmocked routes fail like a network error. This keeps `http.ts`, `unwrap`, the stores and React Query in the path under test.
- `tests/fixtures.ts` has one builder per server response (`me(role)`, `semester()`, `offering()`, `enrollment()`, `studentRegistration(id, status)`, …). Each returns a valid default; override only the fields a test is about. Don't hand-write nested response objects in tests.
- `tests/render.tsx`: `renderWithProviders(ui, { path })` (query client + tooltip provider + memory router) for components; `renderApp(path)` mounts the real `routes` from `router.tsx` for end-to-end flows. Only `renderApp` includes the toaster, so use it (logged in via `setToken` + a `GET /me` mock) when asserting toast messages.
- Use 4xx errors when testing query error states — 5xx/network errors retry once with a delay.
- Coverage (V8) is configured in `vite.config.ts`: app code only (`components/ui/base` shadcn primitives, tests and type-only files are excluded). Thresholds are 90% statements/functions/lines and 80% branches — `npm run test:coverage` fails below them. Reports go to `coverage/` (HTML at `coverage/index.html`, plus `lcov` for CI tools). Raise the thresholds as coverage grows; don't lower them to make a change pass.
- `tests/routing.test.tsx` opens every sidebar link for every role through the real router, so a new sidebar link must have a working route (and a broken lazy `import()` fails the suite).

## Architecture

**Vite + React Router (SPA, `createBrowserRouter`)** with TypeScript, Tailwind CSS v4, shadcn/ui components (Radix UI primitives).

### API layer

All backend calls go through `lib/http.ts` (`http.get/post/put/patch/delete`), which:
- Calls the API on the same origin under `API_BASE_URL = '/api'`. In dev/preview, Vite proxies `/api/*` to `http://localhost:8080` (prefix stripped); in production the reverse proxy in front of the containers (Caddy on the VPS) routes `/api/*` to the backend with the prefix stripped. The frontend image's nginx only serves static files and returns 404 for `/api/*` — never make it proxy to the backend. There is no build-time API URL — never reintroduce one. `mockFetch` requires the `/api` prefix and matches routes on the backend path (`'GET /departments'`).
- Attaches the JWT (via `lib/session.ts`, the only code that touches the `accessToken` key) as `Authorization: Bearer`, unless the call passes `{ skipAuth: true }` (used by `/auth/login`)
- Returns a typed `RemoteRes<T>` (`{ isSuccess, body, message, errors, timestamp, status }`; `status` 0 = no response). Never throws.
- Calls the handler registered with `setUnauthorizedHandler` when a request that carried a token gets a 401

The API is structured as a typed interface (`types/IApi.ts`) with implementations in `lib/api/`. Add new API domains by implementing the interface and registering in `lib/api/api.ts`. The clients mirror the server's resources, not roles (there is no `/admin` prefix):
- Optional filters go through `withQuery(path, filter)` (`lib/queryString.ts`), which drops unset values. Never build `?a=${b}` by hand. Filter types (`CourseOfferingFilter`, `EnrollmentFilter`, …) match the server's `*Filter` records.
- State changes are one `performAction({ id, action })` per resource, posting to `/{resource}/{id}/{action}`.
- The caller's own data uses the `/me` endpoints (`getMyEnrollments`, `getMyOfferings`).

### Business rules stay on the server

The UI never decides a domain rule (status transitions, registration windows, who may act on what, capacity). Responses carry what the current viewer may do: `allowedActions` on semesters, offerings, enrollments and registrations, plus `isRegistrationOpen` / `acceptsOfferings` on semesters. Buttons and menus render exactly those, and refusals show the server's message. If a screen needs a rule the server doesn't expose, raise it as a backend change instead of computing it here. Grouping, sorting and text search for display are fine.

### State (Zustand)

No React Context is used for app state; stores live in `stores/`.

- `stores/apiStore.ts` — holds the singleton `IApi`. Components use `useApi()`; store actions use `useApiStore.getState().api`.
- `stores/appStore.ts` — `useAppStore` holds **data** (`appState`, `error`, `user`, `staticData`) and a separate, stable `actions` object (`init`, `login`, `logout`, `expireSession`).
  - Read data with a selector: `useAppStore((state) => state.user)`. Select the narrowest value you need; never select a new object/array literal (Zustand v5 will re-render in a loop).
  - Call actions via `useAppActions()` — it never triggers re-renders.
  - `user === null` means logged out. Actions don't navigate: `AuthLayout`/`DashboardLayout` redirect with `<Navigate replace>` (authenticated → role home from `DASHBOARD_HOME`; logged out → `/login`).
- Startup: `components/common/AppGate.tsx` (in `pages/RootLayout.tsx`) registers `expireSession` as the 401 handler and calls `init()` on mount. `init()` loads static data and restores the session (`/me` if a token exists) in parallel: success → `READY`; a 401 just logs out; any other failure → `FAILED`.
- `AppGate` also renders a loading overlay while `LOADING`, an error overlay whose retry re-runs the whole `init()` while `FAILED`, and the app only when `READY`. So `staticData` is always loaded for components — never add loading flags for it. Add app-wide reference data to `StaticData` + `init()` only if it's needed without login, since it blocks the whole app.

### Server state (TanStack Query)

Zustand holds **client** state (user, app state, theme, static data loaded at startup). Data fetched for a page and data changed by forms is **server** state and goes through React Query.

- `lib/query.ts` — the shared `queryClient` (provided in `pages/RootLayout.tsx`), `ApiError`, and `unwrap()`. `http` never throws, so every `queryFn`/`mutationFn` wraps its call: `queryFn: () => unwrap(api.courses.getCourses({}))`. `unwrap` throws `ApiError` (`message`, `status`) when `isSuccess` is false.
- Retries: 4xx never retry; network errors and 5xx retry once.
- `lib/queryKeys.ts` — every query key lives here; never write keys inline. Keys are hierarchical so a prefix invalidates a whole group (e.g. `queryKeys.registrations.byKind(kind)` refreshes all statuses after approve/reject).
- Mutations invalidate what they change in `onSuccess` and return the invalidation promise, so `isPending` stays true until fresh data is in (used to keep rows/buttons disabled).
- Shared action hooks own the mutation, toasts and invalidation for a resource: `useSemesterAction`, `useEnrollmentAction`. Reuse them rather than writing another mutation for the same action. What each change invalidates:

  | Change | Invalidates |
  | --- | --- |
  | Registration approve/reject | `registrations.byKind(kind)` and `students`/`instructors` |
  | Semester action | `semesters`, `offerings`, `enrollments` (summaries embed the semester status) |
  | Create offering | `offerings` |
  | Request enrollment / enrollment action | `offerings` (seat counts), `enrollments` |
- List filters live in the URL: `useFilterParams(SCHEMA)` (`hooks/useFilterParams.ts`) returns typed filters and a setter (`replace` navigation). Build the schema from `idParam`, `enumParam(values)` and `textParam` as a module-level constant; invalid values are dropped, never sent. Put the filter object in the query key (`queryKeys.offerings.list(filters)`).
- `logout()`/`expireSession()` call `queryClient.clear()` so cached data never leaks to the next user on the same browser.
- Login and app startup stay in the app store — don't move them to React Query.

### Theming

- Light and dark values for every color live in `styles/globals.css` (`:root` and `.dark`); `@theme inline` maps them to Tailwind utilities. Use the semantic classes — `bg-background`/`bg-bg`, `bg-surface`/`bg-card`, `bg-surface-muted`/`bg-muted`, `text-text`/`text-foreground`, `text-text-muted`/`text-muted-foreground`, `border-border`, `text-brand`, `bg-brand-soft`, `bg-primary`, `shadow-soft` — never raw palette colors like `bg-white` or `text-slate-600`. If a palette color is unavoidable (e.g. status badges), add a `dark:` variant.
- `stores/themeStore.ts` (Zustand + `persist`, localStorage key `theme`) holds `light` / `dark` / `system` (default `system`) and toggles the `.dark` class on `<html>`, following OS changes while on `system`. An inline script in `index.html` applies the saved theme before first paint to avoid a flash — keep it in sync with the store's storage key/format.
- `components/common/ThemeToggle.tsx` is in the dashboard header and on the auth pages. The toaster reads the same store.

### Route structure

Entry: `index.html` -> `main.tsx` -> `router.tsx`. All routes are declared explicitly in `router.tsx`;
adding a page means creating the component (default export) under `pages/` **and** registering it there with `lazy: lazyPage(() => import('@/pages/...'))` so each page is its own chunk. Layouts and guards are imported eagerly. The root route's `HydrateFallback` shows the loading overlay while the first page chunk loads.

```
pages/
  RootLayout.tsx   # Providers + <Outlet/>
  auth/            # AuthLayout.tsx - login + registration pages (no sidebar)
    login/
    registration/student/
    registration/instructor/
  dashboard/       # DashboardLayout.tsx - protected area with sidebar layout
    admin/         # registrations, semesters (+ detail), offerings (+ new), students, instructors
    offerings/     # offering detail + class list (admin and instructor)
    courses/       # course catalogue (all roles) + create course (admin)
    student/       # browse and request offerings, enrollments, my courses
    instructor/    # my offerings
    profile/       # account details (all roles)
```

Pages are thin: they read route params and render a feature component from `components/`. Detail pages parse ids with `idParam` and redirect on an invalid one.

Layouts render children via `<Outlet />`. Use `Link`/`useNavigate` from `react-router` (never plain `<a href>` for
internal links - it reloads the whole app). `/` and unknown paths redirect to `/login`.

`AuthLayout` sends authenticated users to their role home; `DashboardLayout` sends unauthenticated users to `/login`. Both use `<Navigate replace>` directly — auth is never `loading` inside the app because `AppProvider` waits for it.

Role access is declared in `router.tsx` with `<RoleGuard roles={[...]} />` (`components/auth/RoleGuard.tsx`), which redirects other roles to their own home. `/dashboard` redirects to the role home. Role home paths live in `config/navigation/dashboardHome.ts`. The backend enforces roles too; the frontend guards are for UX.

### Component conventions

- `components/ui/base/` — shadcn base components (button, card, input, table, etc.)
- `components/ui/` — composite UI pieces (e.g. `StatCard`)
- `components/dashboard/` — sidebar/nav components
- `components/common/` — shared building blocks:
  - `ActionMenu` renders a list of server `allowedActions` from a per-resource config (`SEMESTER_ACTIONS`, `ENROLLMENT_ACTIONS`, `REGISTRATION_ACTIONS`). Set `confirm` on an action to ask first (it uses `ConfirmDialog`). The menu button is named "Actions for {subject}".
  - `QueryState` handles loading (skeleton), error (server message + retry) and empty states around query content.
  - `filters/` (`FilterBar`, `FilterSelect`, `DepartmentFilter`, `SemesterFilter`, `InstructorFilter`) and `StatusTabs` for list filters.
- `components/semesters/SemesterSections` lays out cards grouped by semester (with `groupBySemester` from `lib/semester.ts`, which keeps the server's order).
- Feature components: `admin/`, `auth/`, `courses/`, `enrollments/`, `instructor/`, `offerings/`, `profile/`, `registration/`, `semesters/`, `student/`
- `config/navigation/sidebar.tsx` — sidebar entries: top-level links (`url`, e.g. the per-role Dashboard) or collapsible groups (`items`), each with `roles`; group links can narrow `roles` further. Use `getSidebarNav(role)`. Only link to routes that exist in `router.tsx`, and keep link roles in sync with the route's `RoleGuard`. Create pages are reached from their list page's button, not from the sidebar.
- Toast feedback via `sonner` (`toast.success` / `toast.error`)
- `lib/cn.ts` — `clsx` + `tailwind-merge` utility for class names
