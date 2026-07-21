import { Link, useNavigate } from 'react-router-dom'
import { useUser } from '../context/UserContext'

export default function Navbar() {
  const { user, setUser } = useUser()
  const navigate = useNavigate()

  function handleLogout() {
    setUser(null)
    navigate('/login')
  }

  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
      <Link to="/" className="text-xl font-bold text-indigo-600">AY Booking</Link>
      <div className="flex items-center gap-4">
        <Link to="/my-bookings" className="text-sm text-gray-600 hover:text-indigo-600">My Bookings</Link>
        <span className="text-sm text-gray-500">{user?.name}</span>
        <button onClick={handleLogout} className="text-sm text-red-500 hover:text-red-700">Logout</button>
      </div>
    </nav>
  )
}
