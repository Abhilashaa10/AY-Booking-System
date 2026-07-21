import api from './api'
import { v4 as uuidv4 } from 'uuid'

export const bookingService = {
  createBooking: (userId, seatId, eventId) => {
    const idempotencyKey = uuidv4()
    return api.post('/bookings', null, {
      params: { seatId, eventId },
      headers: {
        'X-User-Id': userId,
        'X-Idempotency-Key': idempotencyKey,
      },
    }).then(res => res.data)
  },
  getBooking: (bookingId) => api.get(`/bookings/${bookingId}`).then(res => res.data),
  cancelBooking: (bookingId, userId) => api.delete(`/bookings/${bookingId}`, {
    headers: { 'X-User-Id': userId },
  }).then(res => res.data),
}
