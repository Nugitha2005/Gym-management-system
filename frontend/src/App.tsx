import { lazy, Suspense, useEffect } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router";
import { RedirectIfLoggedIn, RequireRole } from "./auth/RequireRole";
import { MEMBER_ROLES } from "./auth/roles";
import { FullPageLoader, Spinner } from "./components/ui/Spinner";
import { AppShell } from "./layouts/AppShell";
import Landing from "./pages/public/Landing";
import Login from "./pages/public/Login";
import Register from "./pages/public/Register";

// App screens load on demand, so the public landing page stays light
const MemberHome = lazy(() => import("./pages/member/Home"));
const CheckIn = lazy(() => import("./pages/member/CheckIn"));
const Sessions = lazy(() => import("./pages/member/Sessions"));
const Bookings = lazy(() => import("./pages/member/Bookings"));
const Equipment = lazy(() => import("./pages/member/Equipment"));
const Medical = lazy(() => import("./pages/member/Medical"));
const Fitness = lazy(() => import("./pages/member/Fitness"));
const Incidents = lazy(() => import("./pages/member/Incidents"));

/** Scroll to the top on every page change (but let #hash links on the landing page work). */
function ScrollToTop() {
  const { pathname, hash } = useLocation();
  useEffect(() => {
    if (!hash) window.scrollTo(0, 0);
  }, [pathname, hash]);
  return null;
}

function PageLoader() {
  return (
    <div role="status" className="flex min-h-[40vh] items-center justify-center text-muted">
      <Spinner size={26} />
      <span className="sr-only">Loading…</span>
    </div>
  );
}

export default function App() {
  return (
    <>
      <ScrollToTop />
      <Suspense fallback={<FullPageLoader />}>
        <Routes>
          <Route path="/" element={<RedirectIfLoggedIn><Landing /></RedirectIfLoggedIn>} />
          <Route path="/login" element={<RedirectIfLoggedIn><Login /></RedirectIfLoggedIn>} />
          <Route path="/register" element={<RedirectIfLoggedIn><Register /></RedirectIfLoggedIn>} />

          {/* Members: students and university staff */}
          <Route element={<RequireRole roles={MEMBER_ROLES} />}>
            <Route element={<AppShell />}>
              <Route path="/member" element={<Page><MemberHome /></Page>} />
              <Route path="/member/check-in" element={<Page><CheckIn /></Page>} />
              <Route path="/member/sessions" element={<Page><Sessions /></Page>} />
              <Route path="/member/bookings" element={<Page><Bookings /></Page>} />
              <Route path="/member/equipment" element={<Page><Equipment /></Page>} />
              <Route path="/member/medical" element={<Page><Medical /></Page>} />
              <Route path="/member/fitness" element={<Page><Fitness /></Page>} />
              <Route path="/member/incidents" element={<Page><Incidents /></Page>} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </>
  );
}

/** Keeps the app shell on screen while a lazily loaded page arrives. */
function Page({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<PageLoader />}>{children}</Suspense>;
}
