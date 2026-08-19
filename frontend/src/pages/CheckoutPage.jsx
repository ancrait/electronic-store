import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'
import { formatPrice } from '../api/format.js'

export default function CheckoutPage() {
  const { user } = useAuth()
  const { items, total, clear } = useCart()
  const [form, setForm] = useState({
    customerName: `${user.firstName} ${user.lastName}`,
    phone: user.phone || '',
    deliveryAddress: '',
    comment: ''
  })
  const [error, setError] = useState(null)
  const [pending, setPending] = useState(false)
  const [createdOrder, setCreatedOrder] = useState(null)

  const update = (field) => (event) => setForm({ ...form, [field]: event.target.value })

  const handleSubmit = async (event) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      const order = await api.createOrder({
        ...form,
        items: items.map((item) => ({ productId: item.productId, quantity: item.quantity }))
      })
      setCreatedOrder(order)
      clear()
    } catch (err) {
      setError(err.message)
    } finally {
      setPending(false)
    }
  }

  if (createdOrder) {
    return (
      <div className="empty">
        <p className="eyebrow mono">Замовлення №{createdOrder.id}</p>
        <h2>Прийнято в роботу</h2>
        <p>Підтвердження вже летить на {createdOrder.email}. Статус можна відстежити в розділі{' '}
          <Link to="/orders">Замовлення</Link>.</p>
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <div className="empty">
        <h2>Немає що оформлювати</h2>
        <p>Спочатку додайте товари в <Link to="/">каталозі</Link>.</p>
      </div>
    )
  }

  return (
    <div className="form-page">
      <p className="eyebrow mono">Оформлення</p>
      <h1>Куди доставити</h1>

      <form className="form" onSubmit={handleSubmit}>
        <label className="field">
          <span className="mono">Отримувач</span>
          <input className="input" required value={form.customerName} onChange={update('customerName')} />
        </label>
        <label className="field">
          <span className="mono">Телефон</span>
          <input className="input" value={form.phone} onChange={update('phone')} />
        </label>
        <label className="field">
          <span className="mono">Адреса або відділення</span>
          <input className="input" required value={form.deliveryAddress} onChange={update('deliveryAddress')} />
        </label>
        <label className="field">
          <span className="mono">Коментар</span>
          <textarea className="input" rows="3" value={form.comment} onChange={update('comment')} />
        </label>

        <div className="cart-total">
          <span className="mono">До сплати</span>
          <span className="price price-lg mono">{formatPrice(total)}</span>
        </div>

        {error && <p className="alert">{error}</p>}

        <button className="btn-primary btn-block" disabled={pending}>
          {pending ? 'Надсилаємо…' : 'Підтвердити замовлення'}
        </button>
      </form>
    </div>
  )
}
