import { NavLink } from 'react-router-dom'

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <img src="/astra/favicon.svg" alt="Astra" />
        Astra
      </div>
      <nav>
        <NavLink to="/" end>Home</NavLink>
        <div className="sidebar-section">Docs</div>
        <NavLink to="/getting-started">Getting Started</NavLink>
        <NavLink to="/architecture">Architecture</NavLink>
        <NavLink to="/planners">Planners</NavLink>
        <NavLink to="/api">API Reference</NavLink>
        <div className="sidebar-section">Links</div>
        <a href="https://github.com/Abhiramrathod/astra" target="_blank">GitHub</a>
      </nav>
    </aside>
  )
}
