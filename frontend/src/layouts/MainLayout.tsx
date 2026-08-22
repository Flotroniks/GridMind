import { NavLink, Outlet } from 'react-router'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `btn btn-ghost ${isActive ? 'btn-active' : ''}`

export function MainLayout() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-base-300 to-base-100">
      <header className="navbar bg-base-200/80 px-4 shadow-sm sm:px-8">
        <div className="flex-1">
          <span className="text-lg font-bold text-primary">GridMind</span>
        </div>
        <nav className="flex gap-2">
          <NavLink to="/" className={navLinkClass} end>
            Inventaire
          </NavLink>
          <NavLink to="/storage" className={navLinkClass}>
            Stockage
          </NavLink>
        </nav>
      </header>
      <main className="p-4 sm:p-8">
        <Outlet />
      </main>
    </div>
  )
}
