import { createContext, useContext, useState, useEffect } from 'react'

export const SEEDED_USERS = [
  { id: 'a0000000-0000-0000-0000-000000000001', name: 'Ava Sharma', email: 'ava@ticketing.com' },
  { id: 'a0000000-0000-0000-0000-000000000002', name: 'Rahul Verma', email: 'rahul@ticketing.com' },
  { id: 'a0000000-0000-0000-0000-000000000003', name: 'Priya Singh', email: 'priya@ticketing.com' },
]

const UserContext = createContext(null)

export function UserProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('ay-user')
    return saved ? JSON.parse(saved) : null
  })

  useEffect(() => {
    if (user) localStorage.setItem('ay-user', JSON.stringify(user))
    else localStorage.removeItem('ay-user')
  }, [user])

  return (
    <UserContext.Provider value={{ user, setUser }}>
      {children}
    </UserContext.Provider>
  )
}

export function useUser() {
  return useContext(UserContext)
}
