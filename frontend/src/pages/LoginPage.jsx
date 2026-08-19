import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState(null)
  const [pending, setPending] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleSubmit = async (event) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      await login(form)
      navigate(location.state?.from || '/')
    } catch (err) {
      setError(err.message)
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="form-page">
      <p className="eyebrow mono">Вхід</p>
      <h1>З поверненням</h1>

      <form className="form" onSubmit={handleSubmit}>
        <label className="field">
          <span className="mono">Пошта</span>
          <input className="input" type="email" required value={form.email}
                 onChange={(event) => setForm({ ...form, email: event.target.value })} />
        </label>
        <label className="field">
          <span className="mono">Пароль</span>
          <input className="input" type="password" required value={form.password}
                 onChange={(event) => setForm({ ...form, password: event.target.value })} />
        </label>

        {error && <p className="alert">{error}</p>}

        <button className="btn-primary btn-block" disabled={pending}>
          {pending ? 'Входимо…' : 'Увійти'}
        </button>
      </form>

      <p className="muted">Ще немає акаунта? <Link to="/register">Зареєструватися</Link></p>
    </div>
  )
}
