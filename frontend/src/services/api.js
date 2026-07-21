import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1',
})

api.interceptors.request.use((config) => {
  const saved = localStorage.getItem('ay-user')
  if (saved) {
    const user = JSON.parse(saved)
    config.headers['X-User-Id'] = user.id
  }
  return config
})

export default api
