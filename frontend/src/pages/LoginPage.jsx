import { SEEDED_USERS, useUser } from '../context/UserContext'
import { useNavigate } from 'react-router-dom'

export default function LoginPage() {
  const { setUser } = useUser()
  const navigate = useNavigate()

  function handleSelect(u) {
    setUser(u)
    navigate('/')
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 to-white px-4">
      <div className="max-w-md w-full">
        <h1 className="text-3xl font-bold text-center text-gray-900 mb-2">AY Ticket Booking</h1>
        <p className="text-center text-gray-500 mb-8">Choose a demo user to continue</p>
        <div className="space-y-3">
          {SEEDED_USERS.map((u) => (
            <button
              key={u.id}
              onClick={() => handleSelect(u)}
              className="w-full text-left bg-white border border-gray-200 rounded-xl p-4 hover:border-indigo-400 hover:shadow-md transition-all"
            >
              <div className="font-semibold text-gray-900">{u.name}</div>
              <div className="text-sm text-gray-500">{u.email}</div>
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}
