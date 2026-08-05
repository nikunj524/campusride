import { useCallback, useMemo, useState } from 'react'
import api from '../api/client'
import AuthContext from './authContext'

const USER_KEY = 'campusride_user'
const TOKEN_KEY = 'campusride_token'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const saveSession = useCallback(({ token, user: authenticatedUser }) => {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(authenticatedUser))
    setUser(authenticatedUser)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  const refreshProfile = useCallback(async () => {
    const { data } = await api.get('/auth/profile')
    localStorage.setItem(USER_KEY, JSON.stringify(data))
    setUser(data)
    return data
  }, [])

  const value = useMemo(() => ({
    user,
    isAuthenticated: Boolean(user && localStorage.getItem(TOKEN_KEY)),
    saveSession,
    logout,
    refreshProfile,
  }), [user, saveSession, logout, refreshProfile])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
