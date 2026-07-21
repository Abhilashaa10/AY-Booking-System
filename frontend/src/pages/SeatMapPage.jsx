import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { eventService } from '../services/eventService'
import { bookingService } from '../services/bookingService'
import { useUser } from '../context/UserContext'
import SeatMap from '../components/SeatMap'

export default function SeatMapPage() {
  const { eventId } = useParams()
  const navigate = useNavigate()
  const { user } = useUser()
  const [selectedSeat, setSelectedSeat] = useState(null)

  const { data: seats, isLoading, refetch } = useQuery({
    queryKey: ['seats', eventId],
    queryFn: () => eventService.getAvailableSeats(eventId),
  })

  const { data: event } = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => eventService.getEventById(eventId),
  })

  const bookMutation = useMutation({
    mutationFn: () => bookingService.createBooking(user.id, selectedSeat.id, eventId),
    onSuccess: (booking) => {
      const saved = JSON.parse(localStorage.getItem('ay-my-bookings') || '[]')
      localStorage.setItem('ay-my-bookings', JSON.stringify([...saved, booking.id]))
      toast.success('Seat held! Complete payment before it expires.')
      navigate(`/bookings/${booking.id}/status`)
    },
    onError: (err) => {
      const msg = err.response?.data?.message || 'Could not book this seat — it may have just been taken.'
      toast.error(msg)
      refetch()
    },
  })

  if (isLoading) return <div className="p-8 text-center text-gray-500">Loading seats...</div>

  return (
    <div className="max-w-4xl mx-auto p-6">
      <button onClick={() => navigate('/')} className="text-sm text-indigo-600 mb-4">&larr; Back to events</button>
      <h1 className="text-2xl font-bold text-gray-900 mb-1">{event?.name}</h1>
      <p className="text-gray-500 mb-6">{event?.venue}</p>

      <div className="flex gap-4 mb-4 text-xs">
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-purple-200 border border-purple-300"></span>VIP</span>
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-blue-200 border border-blue-300"></span>Premium</span>
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-green-200 border border-green-300"></span>General</span>
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-gray-100 border border-gray-200"></span>Booked</span>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl p-6 mb-6 overflow-x-auto">
        <SeatMap seats={seats || []} selectedSeatId={selectedSeat?.id} onSelect={setSelectedSeat} />
      </div>

      {selectedSeat && (
        <div className="sticky bottom-4 bg-white border border-gray-200 rounded-xl p-4 shadow-lg flex items-center justify-between">
          <div>
            <div className="font-semibold text-gray-900">Seat {selectedSeat.seatNumber} · {selectedSeat.section}</div>
            <div className="text-sm text-gray-500">₹{selectedSeat.price}</div>
          </div>
          <button
            onClick={() => bookMutation.mutate()}
            disabled={bookMutation.isPending}
            className="bg-indigo-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50"
          >
            {bookMutation.isPending ? 'Booking...' : 'Book This Seat'}
          </button>
        </div>
      )}
    </div>
  )
}
