import api from './api'

export const paymentService = {
  getPaymentStatus: (bookingId) => api.get(`/payments/booking/${bookingId}`).then(res => res.data),
}
