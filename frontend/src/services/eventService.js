import api from './api'

export const eventService = {
  getAllEvents: () => api.get('/events').then(res => res.data),
  getEventById: (eventId) => api.get(`/events/${eventId}`).then(res => res.data),
  getEventDetail: (eventId) => api.get(`/events/${eventId}/detail`).then(res => res.data),
  getAvailableSeats: (eventId) => api.get(`/events/${eventId}/seats`).then(res => res.data),
}
