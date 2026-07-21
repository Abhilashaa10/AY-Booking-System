import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { bookingService } from '../services/bookingService'
import { paymentService } from '../services/paymentService'
import Countdown from '../components/Countdown'

export default function BookingStatusPage() {
  const { bookingId } = useParams()
  const navigate = useNavigate()

  const { data: booking, refetch: refetchBooking } = useQuery({
    queryKey: ['booking', bookingId],
    queryFn: () => bookingService.getBooking(bookingId),
    refetchInterval: 3000,
  })

  const { data: payment } = useQuery({
    queryKey: ['payment', bookingId],
    queryFn: () => paymentService.getPaymentStatus(bookingId),
    enabled: !!booking,
    refetchInterval: (query) => (query.state.data?.status === 'PENDING' ? 2000 : false),
  })

  if (!booking) return <div className="p-8 text-center text-gray-500">Loading booking...</div>

  const statusColors = {
    PENDING: 'bg-amber-100 text-amber-700',
    CONFIRMED: 'bg-green-100 text-green-700',
    FAILED: 'bg-red-100 text-red-700',
    CANCELLED: 'bg-gray-100 text-gray-700',
    EXPIRED: 'bg-gray-100 text-gray-700',
  }

  return (
    <div className="max-w-lg mx-auto p-6">
      <div className="bg-white border border-gray-200 rounded-xl p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-xl font-bold text-gray-900">Booking Status</h1>
          <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusColors[booking.status]}`}>
            {booking.status}
          </span>
        </div>

        <div className="space-y-2 text-sm text-gray-600 mb-4">
          <div>Amount: <span className="font-medium text-gray-900">₹{booking.amount}</span></div>
          {booking.status === 'PENDING' && booking.expiresAt && (
            <div>Seat hold expires in: <Countdown expiresAt={booking.expiresAt} onExpire={refetchBooking} /></div>
          )}
        </div>

        {payment && (
          <div className="border-t border-gray-100 pt-4 mb-4">
            <div className="text-sm text-gray-500 mb-1">Payment</div>
            <div className={`text-sm font-medium ${payment.status === 'SUCCESS' ? 'text-green-600' : payment.status === 'FAILED' ? 'text-red-600' : 'text-amber-600'}`}>
              {payment.status}
              {payment.failureReason && ` — ${payment.failureReason}`}
            </div>
          </div>
        )}

        <button onClick={() => navigate('/')} className="w-full bg-gray-100 text-gray-700 py-2 rounded-lg font-medium hover:bg-gray-200">
          Back to Events
        </button>
      </div>
    </div>
  )
}
