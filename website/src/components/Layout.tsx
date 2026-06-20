import { ReactNode } from 'react'
import { useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'

export default function Layout({ children }: { children: ReactNode }) {
  const location = useLocation()

  return (
    <div className="layout">
      <Sidebar />
      <main className="content" key={location.pathname}>
        <div className="page">{children}</div>
      </main>
    </div>
  )
}
