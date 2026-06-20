import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import GettingStarted from './pages/GettingStarted'
import Architecture from './pages/Architecture'
import Planners from './pages/Planners'
import Api from './pages/Api'
import Guides from './pages/Guides'
import Samples from './pages/Samples'
import FAQ from './pages/FAQ'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/getting-started" element={<GettingStarted />} />
        <Route path="/architecture" element={<Architecture />} />
        <Route path="/planners" element={<Planners />} />
        <Route path="/api" element={<Api />} />
        <Route path="/guides" element={<Guides />} />
        <Route path="/samples" element={<Samples />} />
        <Route path="/faq" element={<FAQ />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  )
}
