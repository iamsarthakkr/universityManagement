# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev      # Start Vite dev server (port 3000 - backend CORS only allows this origin)
npm run build    # tsc type-check + Vite build into dist/
npm run preview  # Serve the production build
npm run lint     # ESLint
```

There are no tests in the frontend. The backend lives in `../server/` (Spring Boot, runs on port 8080).

## Architecture

**Vite + React Router (SPA, `createBrowserRouter`)** with TypeScript, Tailwind CSS v4, shadcn/ui components (Radix UI primitives).

### API layer

All backend calls go through `lib/http.ts` (`http.get/post/put/patch/delete`), which:
- Reads `import.meta.env.VITE_API_BASE_URL` (defaults to `http://localhost:8080`; inlined at build time)
- Attaches JWT from `localStorage.accessToken` as `Authorization: Bearer`
- Returns a typed `RemoteRes<T>` (`{ isSuccess, body, message, errors, timestamp }`)

The API is structured as a typed interface (`types/IApi.ts`) with implementations in `lib/api/`. Add new API domains by implementing the interface and registering in `lib/api/api.ts`.

### Context / state

- `ApiContext` — singleton `IApi` instance, no state, just the API object
- `AuthContext` — JWT token + `AuthUser` + login/logout. Token persisted in `localStorage`. On mount, calls `api.auth.me()` to restore session. It does **not** navigate: the layout guards (`useAuthRedirect`) react to `status` and redirect with `replace` (authenticated → role home from `DASHBOARD_HOME`; logged out → `/login`).
- Session expiry: `lib/http.ts` calls the handler registered via `setUnauthorizedHandler` when a request that carried a token gets a 401. `AuthProvider` registers it to clear the session; the dashboard guard then redirects. Every `RemoteRes` carries the HTTP `status` (0 = no response).
- `AppContext` — app bootstrap. Loads static reference data (`staticData.departments`) and waits for auth session restore. App state (`AppState`: `LOADING` / `FAILED` / `READY`) is internal: it renders a full-screen loading overlay, or an error overlay with retry, and only renders the app once `READY`. Consumers use `useAppContext()` and read `staticData` (e.g. `staticData.departments`), which is always loaded — never add loading flags for static data. Add new app-wide reference data here (only if it's needed before/without login, since it blocks the whole app).
- All wrapped in `context/Providers.tsx` (`Api` → `Auth` → `App`), rendered by `pages/RootLayout.tsx` inside the router.

### Route structure

Entry: `index.html` -> `main.tsx` -> `router.tsx`. All routes are declared explicitly in `router.tsx`;
adding a page means creating the component under `pages/` **and** registering it there.

```
pages/
  RootLayout.tsx   # Providers + <Outlet/>
  auth/            # AuthLayout.tsx - login + registration pages (no sidebar)
    login/
    registration/student/
    registration/instructor/
  dashboard/       # DashboardLayout.tsx - protected area with sidebar layout
    admin/         # Admin pages + sub-routes for registrations
    courses/       # Course catalogue + create course
    student/       # Student pages (courses, enrollments)
    instructor/    # Instructor pages (courses)
```

Layouts render children via `<Outlet />`. Use `Link`/`useNavigate` from `react-router` (never plain `<a href>` for
internal links - it reloads the whole app). `/` and unknown paths redirect to `/login`.

`hooks/useAuthRedirect.tsx` guards the layouts — unauthenticated users go to `/login`, authenticated users on auth pages go to their role home.

Role access is declared in `router.tsx` with `<RoleGuard roles={[...]} />` (`components/auth/RoleGuard.tsx`), which redirects other roles to their own home. `/dashboard` redirects to the role home. Role home paths live in `config/navigation/dashboardHome.ts`. The backend enforces roles too; the frontend guards are for UX.

### Component conventions

- `components/ui/base/` — shadcn base components (button, card, input, table, etc.)
- `components/ui/` — composite UI pieces (e.g. `StatCard`)
- `components/dashboard/` — sidebar/nav components
- `components/admin/`, `components/registration/`, `components/auth/` — feature components
- `config/navigation/sidebar.tsx` — sidebar entries: top-level links (`url`, e.g. the per-role Dashboard) or collapsible groups (`items`), each with `roles`; group links can narrow `roles` further. Use `getSidebarNav(role)`. Only link to routes that exist in `router.tsx`, and keep link roles in sync with the route's `RoleGuard`.
- Toast feedback via `sonner` (`toast.success` / `toast.error`)
- `lib/cn.ts` — `clsx` + `tailwind-merge` utility for class names
