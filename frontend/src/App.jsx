import { Routes, Route, Navigate } from 'react-router-dom'
import { useUser } from './context/UserContext'
import Navbar from './components/Navbar'
import LoginPage from './pages/LoginPage'
import EventListPage from './pages/EventListPage'
import SeatMapPage from './pages/SeatMapPage'
import MyBookingsPage from './pages/MyBookingsPage'
import BookingStatusPage from './pages/BookingStatusPage'

function ProtectedRoute({ children }) {
  const { user } = useUser()
  if (!user) return <Navigate to="/login" replace />
  return children
}

function App() {
  const { user } = useUser()

  return (
    <div className="min-h-screen bg-gray-50">
      {user && <Navbar />}
      <Routes>
        <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
        <Route path="/" element={<ProtectedRoute><EventListPage /></ProtectedRoute>} />
        <Route path="/events/:eventId/seats" element={<ProtectedRoute><SeatMapPage /></ProtectedRoute>} />
        <Route path="/bookings/:bookingId/status" element={<ProtectedRoute><BookingStatusPage /></ProtectedRoute>} />
        <Route path="/my-bookings" element={<ProtectedRoute><MyBookingsPage /></ProtectedRoute>} />
      </Routes>
    </div>
  )
}

export default App
