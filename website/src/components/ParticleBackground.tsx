import { useEffect, useRef } from 'react'

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  size: number
  alpha: number
  life: number
  maxLife: number
  color: string
}

export default function ParticleBackground() {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const cvs = canvasRef.current
    if (!cvs) return

    const ctx = cvs.getContext('2d')
    if (!ctx) return

    const $cvs: HTMLCanvasElement = cvs
    const $ctx: CanvasRenderingContext2D = ctx

    let animId: number
    let particles: Particle[] = []
    let lastTime = 0

    const resize = () => {
      $cvs.width = window.innerWidth
      $cvs.height = window.innerHeight
    }

    resize()
    window.addEventListener('resize', resize)

    const colors = [
      'rgba(220, 38, 38, {a})',
      'rgba(239, 68, 68, {a})',
      'rgba(201, 168, 76, {a})',
      'rgba(228, 199, 102, {a})',
    ]

    function spawnParticle() {
      const maxLife = 3000 + Math.random() * 3000
      const color = colors[Math.floor(Math.random() * colors.length)]
      particles.push({
        x: Math.random() * $cvs.width,
        y: $cvs.height + 10,
        vx: (Math.random() - 0.5) * 0.3,
        vy: -(0.2 + Math.random() * 0.4),
        size: 1 + Math.random() * 2.5,
        alpha: 0.2 + Math.random() * 0.4,
        life: 0,
        maxLife,
        color,
      })
    }

    function spawnBurst() {
      const cx = Math.random() * $cvs.width
      const cy = $cvs.height * (0.6 + Math.random() * 0.4)
      for (let i = 0; i < 6; i++) {
        const maxLife = 1500 + Math.random() * 2000
        const angle = Math.random() * Math.PI * 2
        const speed = 0.3 + Math.random() * 0.6
        const color = colors[Math.floor(Math.random() * colors.length)]
        particles.push({
          x: cx,
          y: cy,
          vx: Math.cos(angle) * speed,
          vy: Math.sin(angle) * speed - 0.3,
          size: 1 + Math.random() * 2,
          alpha: 0.3 + Math.random() * 0.4,
          life: 0,
          maxLife,
          color,
        })
      }
    }

    let burstTimer = 0

    function tick(time: number) {
      const dt = Math.min(time - lastTime, 50)
      lastTime = time

      $ctx.clearRect(0, 0, $cvs.width, $cvs.height)

      if (particles.length < 80 && Math.random() < 0.3) {
        spawnParticle()
      }

      burstTimer += dt
      if (burstTimer > 4000 + Math.random() * 4000) {
        spawnBurst()
        burstTimer = 0
      }

      particles = particles.filter(p => {
        p.life += dt
        if (p.life >= p.maxLife) return false

        const progress = p.life / p.maxLife
        p.x += p.vx * (dt / 16)
        p.y += p.vy * (dt / 16)
        p.alpha *= 0.998
        p.vy -= 0.0003 * (dt / 16)

        $ctx.beginPath()
        $ctx.arc(p.x, p.y, p.size * (1 - progress * 0.3), 0, Math.PI * 2)
        const colorStr = p.color.replace('{a}', String(p.alpha * (1 - progress * 0.6)))
        $ctx.fillStyle = colorStr
        $ctx.fill()

        if (p.size > 1.5) {
          $ctx.beginPath()
          $ctx.arc(p.x - p.vx * 1.5, p.y - p.vy * 1.5, p.size * 0.4, 0, Math.PI * 2)
          $ctx.fillStyle = colorStr.replace(/[\d.]+(?=\))/, String(p.alpha * 0.3 * (1 - progress * 0.6)))
          $ctx.fill()
        }

        return true
      })

      animId = requestAnimationFrame(tick)
    }

    animId = requestAnimationFrame(tick)

    return () => {
      cancelAnimationFrame(animId)
      window.removeEventListener('resize', resize)
    }
  }, [])

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: 'fixed',
        inset: 0,
        pointerEvents: 'none',
        zIndex: 0,
        opacity: 0.6,
      }}
    />
  )
}
