import { createContext, useContext, useState } from 'react'
import { api, tokenStorage } from '../api/client.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => tokenStorage.getUser())

  const login = async (credentials) => {
    const data = await api.login(credentials)
    tokenStorage.save(data)
    setUser(data.user)
    return data.user
  }

  const register = async (payload) => {
    const data = await api.register(payload)
    tokenStorage.save(data)
    setUser(data.user)
    return data.user
  }

  const logout = () => {
    tokenStorage.clear()
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAdmin: user?.role === 'ADMIN' }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
