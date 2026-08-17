import axios from 'axios'

function createApiClient(baseURL) {
  const client = axios.create({
    baseURL,
    headers: { 'Content-Type': 'application/json' },
  })

  client.interceptors.request.use((config) => {
    const token = localStorage.getItem('campusride_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  return client
}

const api = createApiClient(import.meta.env.VITE_AUTH_API_URL || 'http://localhost:8081')

export const userApi = createApiClient(import.meta.env.VITE_USER_API_URL || 'http://localhost:8082')

export default api
