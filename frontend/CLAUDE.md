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
- Attaches the JWT (via `lib/session.ts`, the only code that touches the `accessToken` key) as `Authorization: Bearer`, unless the call passes `{ skipAuth: true }` (used by `/auth/login`)
- Returns a typed `RemoteRes<T>` (`{ isSuccess, body, message, errors, timestamp, status }`; `status` 0 = no response). Never throws.
- Calls the handler registered with `setUnauthorizedHandler` when a request that carried a token gets a 401

The API is structured as a typed interface (`types/IApi.ts`) with implementations in `lib/api/`. Add new API domains by implementing the interface and registering in `lib/api/api.ts`.

### State (Zustand)

No React Context is used for app state; stores live in `stores/`.

- `stores/apiStore.ts` — holds the singleton `IApi`. Components use `useApi()`; store actions use `useApiStore.getState().api`.
- `stores/appStore.ts` — `useAppStore` holds **data** (`appState`, `error`, `user`, `staticData`) and a separate, stable `actions` object (`init`, `login`, `logout`, `expireSession`).
  - Read data with a selector: `useAppStore((state) => state.user)`. Select the narrowest value you need; never select a new object/array literal (Zustand v5 will re-render in a loop).
  - Call actions via `useAppActions()` — it never triggers re-renders.
  - `user === null` means logged out. Actions don't navigate: `AuthLayout`/`DashboardLayout` redirect with `<Navigate replace>` (authenticated → role home from `DASHBOARD_HOME`; logged out → `/login`).
- Startup: `components/common/AppGate.tsx` (in `pages/RootLayout.tsx`) registers `expireSession` as the 401 handler and calls `init()` on mount. `init()` loads static data and restores the session (`/auth/me` if a token exists) in parallel: success → `READY`; a 401 just logs out; any other failure → `FAILED`.
- `AppGate` also renders a loading overlay while `LOADING`, an error overlay whose retry re-runs the whole `init()` while `FAILED`, and the app only when `READY`. So `staticData` is always loaded for components — never add loading flags for it. Add app-wide reference data to `StaticData` + `init()` only if it's needed without login, since it blocks the whole app.

### Theming

- Light and dark values for every color live in `styles/globals.css` (`:root` and `.dark`); `@theme inline` maps them to Tailwind utilities. Use the semantic classes — `bg-background`/`bg-bg`, `bg-surface`/`bg-card`, `bg-surface-muted`/`bg-muted`, `text-text`/`text-foreground`, `text-text-muted`/`text-muted-foreground`, `border-border`, `text-brand`, `bg-brand-soft`, `bg-primary`, `shadow-soft` — never raw palette colors like `bg-white` or `text-slate-600`. If a palette color is unavoidable (e.g. status badges), add a `dark:` variant.
- `stores/themeStore.ts` (Zustand + `persist`, localStorage key `theme`) holds `light` / `dark` / `system` (default `system`) and toggles the `.dark` class on `<html>`, following OS changes while on `system`. An inline script in `index.html` applies the saved theme before first paint to avoid a flash — keep it in sync with the store's storage key/format.
- `components/common/ThemeToggle.tsx` is in the dashboard header and on the auth pages. The toaster reads the same store.

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

`AuthLayout` sends authenticated users to their role home; `DashboardLayout` sends unauthenticated users to `/login`. Both use `<Navigate replace>` directly — auth is never `loading` inside the app because `AppProvider` waits for it.

Role access is declared in `router.tsx` with `<RoleGuard roles={[...]} />` (`components/auth/RoleGuard.tsx`), which redirects other roles to their own home. `/dashboard` redirects to the role home. Role home paths live in `config/navigation/dashboardHome.ts`. The backend enforces roles too; the frontend guards are for UX.

### Component conventions

- `components/ui/base/` — shadcn base components (button, card, input, table, etc.)
- `components/ui/` — composite UI pieces (e.g. `StatCard`)
- `components/dashboard/` — sidebar/nav components
- `components/admin/`, `components/registration/`, `components/auth/` — feature components
- `config/navigation/sidebar.tsx` — sidebar entries: top-level links (`url`, e.g. the per-role Dashboard) or collapsible groups (`items`), each with `roles`; group links can narrow `roles` further. Use `getSidebarNav(role)`. Only link to routes that exist in `router.tsx`, and keep link roles in sync with the route's `RoleGuard`.
- Toast feedback via `sonner` (`toast.success` / `toast.error`)
- `lib/cn.ts` — `clsx` + `tailwind-merge` utility for class names
