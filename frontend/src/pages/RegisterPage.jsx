import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function RegisterPage() {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', phone: '' })
  const [error, setError] = useState(null)
  const [pending, setPending] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const update = (field) => (event) => setForm({ ...form, [field]: event.target.value })

  const handleSubmit = async (event) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      await register(form)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="form-page">
      <p className="eyebrow mono">Реєстрація</p>
      <h1>Створити акаунт</h1>
      <p className="muted">Лист із підтвердженням прийде на вказану пошту.</p>

      <form className="form" onSubmit={handleSubmit}>
        <div className="field-row">
          <label className="field">
            <span className="mono">Імʼя</span>
            <input className="input" required value={form.firstName} onChange={update('firstName')} />
          </label>
          <label className="field">
            <span className="mono">Прізвище</span>
            <input className="input" required value={form.lastName} onChange={update('lastName')} />
          </label>
        </div>
        <label className="field">
          <span className="mono">Пошта</span>
          <input className="input" type="email" required value={form.email} onChange={update('email')} />
        </label>
        <label className="field">
          <span className="mono">Телефон</span>
          <input className="input" value={form.phone} onChange={update('phone')} />
        </label>
        <label className="field">
          <span className="mono">Пароль (від 6 символів)</span>
          <input className="input" type="password" required minLength={6} value={form.password} onChange={update('password')} />
        </label>

        {error && <p className="alert">{error}</p>}

        <button className="btn-primary btn-block" disabled={pending}>
          {pending ? 'Створюємо…' : 'Зареєструватися'}
        </button>
      </form>

      <p className="muted">Вже є акаунт? <Link to="/login">Увійти</Link></p>
    </div>
  )
}
