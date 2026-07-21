import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { eventService } from '../services/eventService'
import formatEventDate from '../utils'

export default function EventListPage() {
  const navigate = useNavigate()
  const { data: events, isLoading, error } = useQuery({
    queryKey: ['events'],
    queryFn: eventService.getAllEvents,
  })

  if (isLoading) return <div className="p-8 text-center text-gray-500">Loading events...</div>
  if (error) return <div className="p-8 text-center text-red-500">Failed to load events. Is the backend running?</div>

  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Upcoming Events</h1>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {events?.map((event) => (
          <div
            key={event.id}
            onClick={() => navigate(`/events/${event.id}/seats`)}
            className="bg-white border border-gray-200 rounded-xl p-5 cursor-pointer hover:shadow-lg hover:border-indigo-300 transition-all"
          >
            <h2 className="font-semibold text-lg text-gray-900">{event.name}</h2>
            <p className="text-sm text-gray-500 mt-1">{event.venue}</p>
            <p className="text-sm text-gray-400 mt-1">{formatEventDate(event.eventDate)}</p>
            <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100">
              <span className="text-sm font-medium text-indigo-600">{event.priceRange}</span>
              <span className="text-xs text-gray-500">{event.availableSeats} seats left</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
