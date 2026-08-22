import axios from 'axios'

const SESSION_KEY = 'campusride_session'
const TOKEN_KEY = 'campusride_token'

function getStoredToken() {
  try {
    const session = JSON.parse(localStorage.getItem(SESSION_KEY) || 'null')
    if (session?.token) {
      return session.token
    }
  } catch {
    // Fall back to the legacy token key below.
  }

  return localStorage.getItem(TOKEN_KEY)
}

function createApiClient(baseURL) {
  const client = axios.create({
    baseURL,
    headers: { 'Content-Type': 'application/json' },
  })

  client.interceptors.request.use((config) => {
    const token = getStoredToken()
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
