import api from './api'

export const cancellationService = {
  cancelBooking: (bookingId, userId, seatId) => api.post(`/cancellations/${bookingId}`, null, {
    headers: { 'X-User-Id': userId, 'X-Seat-Id': seatId },
  }).then(res => res.data),
}
