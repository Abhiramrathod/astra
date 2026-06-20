import { ReactNode, useState, useEffect } from 'react'
import { useLocation, NavLink } from 'react-router-dom'
import ParticleBackground from './ParticleBackground'
import {
  HomeIcon, RocketIcon, BlocksIcon, GitBranchIcon,
  BookOpenIcon, CpuIcon, FileCodeIcon, HelpCircleIcon,
  GithubIcon, MenuIcon, XIcon,
} from './Icons'

const navLinks = [
  { to: '/', label: 'Home', icon: HomeIcon, end: true },
  { to: '/getting-started', label: 'Getting Started', icon: RocketIcon },
  { to: '/architecture', label: 'Architecture', icon: BlocksIcon },
  { to: '/planners', label: 'Planners', icon: GitBranchIcon },
  { to: '/api', label: 'API', icon: BookOpenIcon },
  { to: '/guides', label: 'Guides', icon: CpuIcon },
  { to: '/samples', label: 'Samples', icon: FileCodeIcon },
  { to: '/faq', label: 'FAQ', icon: HelpCircleIcon },
]

export default function Layout({ children }: { children: ReactNode }) {
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)

  useEffect(() => {
    setMenuOpen(false)
  }, [location.pathname])

  return (
    <>
      <div className="watermark" />
      <ParticleBackground />

      <header className="header">
        <div className="header-inner">
          <a href="/" className="header-logo">
            <img src="/astra/favicon.svg" alt="Astra" />
            <span>Astra</span>
          </a>

          <nav className="header-nav">
            {navLinks.map(l => (
              <NavLink key={l.to} to={l.to} end={l.end}>
                <l.icon /> {l.label}
              </NavLink>
            ))}
          </nav>

          <div className="header-actions">
            <a href="https://github.com/Abhiramrathod/astra" target="_blank" rel="noopener" aria-label="GitHub">
              <GithubIcon />
            </a>
            <button className="mobile-menu-btn" onClick={() => setMenuOpen(!menuOpen)} aria-label="Toggle menu">
              {menuOpen ? <XIcon /> : <MenuIcon />}
            </button>
          </div>
        </div>
      </header>

      <div className={`mobile-dropdown ${menuOpen ? 'open' : ''}`} onClick={() => setMenuOpen(false)}>
        {navLinks.map(l => (
          <NavLink key={l.to} to={l.to} end={l.end}>
            <l.icon /> {l.label}
          </NavLink>
        ))}
        <div className="mobile-dropdown-section">Links</div>
        <a href="https://github.com/Abhiramrathod/astra" target="_blank" rel="noopener">
          <GithubIcon /> GitHub
        </a>
      </div>

      <main className="content" key={location.pathname}>
        <div className="page">
          {children}
          <footer className="footer">
            <span>&copy; {new Date().getFullYear()} Astra — Apache 2.0 License</span>
            <div className="footer-links">
              <a href="https://github.com/Abhiramrathod/astra" target="_blank" rel="noopener">GitHub</a>
              <a href="https://github.com/Abhiramrathod/astra/releases" target="_blank" rel="noopener">Releases</a>
              <a href="https://github.com/Abhiramrathod/astra/issues" target="_blank" rel="noopener">Issues</a>
            </div>
          </footer>
        </div>
      </main>
    </>
  )
}
