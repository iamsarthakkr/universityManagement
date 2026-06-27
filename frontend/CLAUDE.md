# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev      # Start dev server (Next.js, port 3000)
npm run build    # Production build
npm run lint     # ESLint
```

There are no tests in the frontend. The backend lives in `../server/` (Spring Boot, runs on port 8080).

## Architecture

**Next.js 15 App Router** with TypeScript, Tailwind CSS v4, shadcn/ui components (Radix UI primitives).

### API layer

All backend calls go through `lib/http.ts` (`http.get/post/put/patch/delete`), which:
- Reads `process.env.API_BASE_URL` (defaults to `http://localhost:8080`)
- Attaches JWT from `localStorage.accessToken` as `Authorization: Bearer`
- Returns a typed `RemoteRes<T>` (`{ isSuccess, body, message, errors, timestamp }`)

The API is structured as a typed interface (`types/IApi.ts`) with implementations in `lib/api/`. Add new API domains by implementing the interface and registering in `lib/api/api.ts`.

### Context / state

- `ApiContext` — singleton `IApi` instance, no state, just the API object
- `AuthContext` — JWT token + `AuthUser` + login/logout. Token persisted in `localStorage`. On mount, calls `api.auth.me()` to restore session. Role-based redirect on login (`ADMIN` → `/dashboard/admin`, `STUDENT` → `/dashboard/student`, `INSTRUCTOR` → `/dashboard/instructor`).
- Both wrapped in `context/Providers.tsx` at the root layout.

### Route structure

```
app/
  (auth)/          # Login + registration pages (no sidebar)
    login/
    registration/student/
    registration/instructor/
  dashboard/       # Protected area with sidebar layout
    admin/         # Admin pages + sub-routes for registrations
    student/       # Student pages (courses, enrollments)
    instructor/    # Instructor pages (courses)
```

`hooks/useAuthRedirect.tsx` guards dashboard routes — redirects unauthenticated users to `/login`.

### Component conventions

- `components/ui/base/` — shadcn base components (button, card, input, table, etc.)
- `components/ui/` — composite UI pieces (e.g. `StatCard`)
- `components/dashboard/` — sidebar/nav components
- `components/admin/`, `components/registration/`, `components/auth/` — feature components
- `config/navigation/sidebar.tsx` — sidebar nav items config (role-based)
- Toast feedback via `sonner` (`toast.success` / `toast.error`)
- `lib/cn.ts` — `clsx` + `tailwind-merge` utility for class names
