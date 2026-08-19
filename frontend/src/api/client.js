const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const TOKEN_KEY = 'volt.accessToken'
const REFRESH_KEY = 'volt.refreshToken'
const USER_KEY = 'volt.user'

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  getUser: () => {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  },
  save: ({ accessToken, refreshToken, user }) => {
    localStorage.setItem(TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_KEY, refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_KEY)
    localStorage.removeItem(USER_KEY)
  }
}

async function request(path, { method = 'GET', body, auth = false } = {}) {
  const headers = { 'Content-Type': 'application/json' }

  if (auth) {
    const token = tokenStorage.get()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  }

  const response = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })

  if (response.status === 401) {
    tokenStorage.clear()
    throw new Error('Сесія завершилася. Увійдіть знову.')
  }

  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text()

  if (!response.ok) {
    throw new Error(extractMessage(payload))
  }

  return payload
}

function extractMessage(payload) {
  if (typeof payload === 'string') return payload || 'Сервер повернув помилку'
  if (payload?.errors) return Object.values(payload.errors).join('. ')
  return payload?.message || 'Сервер повернув помилку'
}

export const api = {
  register: (data) => request('/api/auth/register', { method: 'POST', body: data }),
  login: (data) => request('/api/auth/login', { method: 'POST', body: data }),
  me: () => request('/api/auth/me', { auth: true }),

  categories: () => request('/api/categories'),
  products: (params) => request(`/api/products?${new URLSearchParams(params)}`),
  product: (id) => request(`/api/products/${id}`),
  createProduct: (data) => request('/api/products', { method: 'POST', body: data, auth: true }),

  createOrder: (data) => request('/api/orders', { method: 'POST', body: data, auth: true }),
  myOrders: () => request('/api/orders/my?size=50', { auth: true }),
  allOrders: () => request('/api/orders/all?size=50', { auth: true }),
  updateOrderStatus: (id, status) =>
    request(`/api/orders/${id}/status`, { method: 'PUT', body: { status }, auth: true })
}
