import { useQueries } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { bookingService } from '../services/bookingService'

export default function MyBookingsPage() {
  const navigate = useNavigate()
  const bookingIds = JSON.parse(localStorage.getItem('ay-my-bookings') || '[]')

  const results = useQueries({
    queries: bookingIds.map((id) => ({
      queryKey: ['booking', id],
      queryFn: () => bookingService.getBooking(id),
    })),
  })

  const bookings = results.map((r) => r.data).filter(Boolean).reverse()

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My Bookings</h1>
      {bookings.length === 0 && <p className="text-gray-500">No bookings yet.</p>}
      <div className="space-y-3">
        {bookings.map((b) => (
          <div
            key={b.id}
            onClick={() => navigate(`/bookings/${b.id}/status`)}
            className="bg-white border border-gray-200 rounded-xl p-4 cursor-pointer hover:shadow-md flex items-center justify-between"
          >
            <div>
              <div className="font-medium text-gray-900">₹{b.amount}</div>
              <div className="text-xs text-gray-400">{new Date(b.createdAt).toLocaleString()}</div>
            </div>
            <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-gray-100 text-gray-600">{b.status}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
