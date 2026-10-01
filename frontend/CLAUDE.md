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
- `AuthContext` — JWT token + `AuthUser` + login/logout. Token persisted in `localStorage`. On mount, calls `api.auth.me()` to restore session. It does **not** navigate: the layout guards (`useAuthRedirect`) react to `status` and redirect with `replace` (role-based: `ADMIN` → `/dashboard/admin`, `STUDENT` → `/dashboard/student`, `INSTRUCTOR` → `/dashboard/instructor`; logged out → `/login`).
- Session expiry: `lib/http.ts` calls the handler registered via `setUnauthorizedHandler` when a request that carried a token gets a 401. `AuthProvider` registers it to clear the session; the dashboard guard then redirects. Every `RemoteRes` carries the HTTP `status` (0 = no response).
- Both wrapped in `context/Providers.tsx`, rendered by `pages/RootLayout.tsx` *inside* the router (AuthProvider uses `useNavigate`, which throws outside a router).

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

`hooks/useAuthRedirect.tsx` guards dashboard routes — redirects unauthenticated users to `/login`.

### Component conventions

- `components/ui/base/` — shadcn base components (button, card, input, table, etc.)
- `components/ui/` — composite UI pieces (e.g. `StatCard`)
- `components/dashboard/` — sidebar/nav components
- `components/admin/`, `components/registration/`, `components/auth/` — feature components
- `config/navigation/sidebar.tsx` — sidebar nav items config (role-based)
- Toast feedback via `sonner` (`toast.success` / `toast.error`)
- `lib/cn.ts` — `clsx` + `tailwind-merge` utility for class names
