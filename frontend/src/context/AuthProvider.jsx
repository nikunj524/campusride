import { useCallback, useEffect, useMemo, useState } from 'react'
import api from '../api/client'
import AuthContext from './authContext'

const SESSION_KEY = 'campusride_session'
const USER_KEY = 'campusride_user'
const TOKEN_KEY = 'campusride_token'

function getStoredSession() {
  try {
    const rawSession = localStorage.getItem(SESSION_KEY)
    if (rawSession) {
      const session = JSON.parse(rawSession)
      return {
        ...session,
        activeRole: session?.activeRole || (getWorkspaceModeFromToken(session?.token) === 'driver' ? 'DRIVER' : 'STUDENT'),
        driverEligible: Boolean(
          session?.driverEligible
          || getDriverEligibleFromToken(session?.token)
          || session?.user?.driverEligible
          || session?.user?.role === 'DRIVER'
        ),
      }
    }
  } catch {
    // Fall through to the legacy keys below.
  }

  const token = localStorage.getItem(TOKEN_KEY)
  const user = getStoredUser()
  if (token || user) {
    return {
      token,
      user,
      driverEligible: Boolean(getDriverEligibleFromToken(token) || user?.driverEligible || user?.role === 'DRIVER'),
    }
  }

  return null
}

function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

function getDriverEligibleFromToken(token) {
  if (!token) return null

  try {
    const payload = token.split('.')[1]
    if (!payload) return null

    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/')
    const paddedPayload = normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, '=')
    const decoded = atob(paddedPayload)
    const parsed = JSON.parse(decoded)
    return typeof parsed.driverEligible === 'boolean' ? parsed.driverEligible : null
  } catch {
    return null
  }
}

function getWorkspaceModeFromToken(token) {
  if (!token) return 'student'

  try {
    const payload = token.split('.')[1]
    if (!payload) return 'student'

    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/')
    const paddedPayload = normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, '=')
    const decoded = atob(paddedPayload)
    const parsed = JSON.parse(decoded)
    return parsed.activeRole === 'DRIVER' ? 'driver' : 'student'
  } catch {
    return 'student'
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => getStoredSession())

  const token = session?.token || null
  const user = session?.user || null
  const driverEligible = Boolean(session?.driverEligible || getDriverEligibleFromToken(token) || user?.driverEligible || user?.role === 'DRIVER')
  const workspaceMode = useMemo(() => session?.activeRole === 'DRIVER'
    ? 'driver'
    : session?.activeRole === 'STUDENT' ? 'student' : getWorkspaceModeFromToken(token), [session?.activeRole, token])

  const saveSession = useCallback(({
    token: nextToken,
    user: authenticatedUser,
    activeRole: activeRoleFromResponse,
    driverEligible: eligibleFromResponse,
  }) => {
    const nextSession = {
      token: nextToken,
      user: authenticatedUser,
      activeRole: activeRoleFromResponse || (getWorkspaceModeFromToken(nextToken) === 'driver' ? 'DRIVER' : 'STUDENT'),
      driverEligible: Boolean(
        eligibleFromResponse
        || getDriverEligibleFromToken(nextToken)
        || authenticatedUser?.driverEligible
        || authenticatedUser?.role === 'DRIVER'
      ),
    }

    localStorage.setItem(TOKEN_KEY, nextToken)
    localStorage.setItem(USER_KEY, JSON.stringify(authenticatedUser))
    localStorage.setItem(SESSION_KEY, JSON.stringify(nextSession))
    setSession(nextSession)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(SESSION_KEY)
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setSession(null)
  }, [])

  const refreshProfile = useCallback(async () => {
    const { data } = await api.get('/auth/profile')
    setSession((currentSession) => {
      const currentToken = currentSession?.token || localStorage.getItem(TOKEN_KEY)
      const nextSession = {
        token: currentToken,
        user: data,
        activeRole: currentSession?.activeRole || (getWorkspaceModeFromToken(currentToken) === 'driver' ? 'DRIVER' : 'STUDENT'),
        driverEligible: Boolean(
          data?.driverEligible
          || data?.role === 'DRIVER'
        ),
      }
      localStorage.setItem(SESSION_KEY, JSON.stringify(nextSession))
      localStorage.setItem(USER_KEY, JSON.stringify(data))
      if (nextSession.token) {
        localStorage.setItem(TOKEN_KEY, nextSession.token)
      }
      return nextSession
    })
    return data
  }, [])

  useEffect(() => {
    if (!token) return

    void Promise.resolve().then(() => refreshProfile()).catch(() => {
      // Keep the existing session when the profile request is temporarily unavailable.
    })
  }, [refreshProfile, token])

  const switchWorkspace = useCallback(async (mode) => {
    const { data } = await api.post('/auth/workspace', { mode })
    saveSession(data)
  }, [saveSession])

  const switchToDriverWorkspace = useCallback(() => switchWorkspace('DRIVER'), [switchWorkspace])

  const switchToStudentWorkspace = useCallback(() => switchWorkspace('STUDENT'), [switchWorkspace])

  const value = useMemo(() => ({
    user,
    workspaceMode,
    isDriverWorkspace: workspaceMode === 'driver',
    canSwitchToDriver: Boolean(driverEligible),
    isAuthenticated: Boolean(user && token),
    saveSession,
    logout,
    refreshProfile,
    switchToDriverWorkspace,
    switchToStudentWorkspace,
  }), [user, token, workspaceMode, driverEligible, saveSession, logout, refreshProfile, switchToDriverWorkspace, switchToStudentWorkspace])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
