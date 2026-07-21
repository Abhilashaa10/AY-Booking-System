import { useState, useEffect } from 'react'

export default function Countdown({ expiresAt, onExpire }) {
  const [remaining, setRemaining] = useState(() => Math.max(0, new Date(expiresAt) - new Date()))

  useEffect(() => {
    const interval = setInterval(() => {
      const diff = Math.max(0, new Date(expiresAt) - new Date())
      setRemaining(diff)
      if (diff <= 0) {
        clearInterval(interval)
        onExpire?.()
      }
    }, 1000)
    return () => clearInterval(interval)
  }, [expiresAt, onExpire])

  const minutes = Math.floor(remaining / 60000)
  const seconds = Math.floor((remaining % 60000) / 1000)

  return (
    <span className={`font-mono font-semibold ${remaining < 60000 ? 'text-red-500' : 'text-amber-600'}`}>
      {minutes}:{seconds.toString().padStart(2, '0')}
    </span>
  )
}
