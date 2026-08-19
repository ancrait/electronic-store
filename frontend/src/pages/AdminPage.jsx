import { useEffect, useState } from 'react'
import { api } from '../api/client.js'
import { formatDate, formatPrice, STATUS_LABELS } from '../api/format.js'

const EMPTY_PRODUCT = {
  name: '', brand: '', description: '', price: '', stockQuantity: '', imageUrl: '', categoryId: ''
}

export default function AdminPage() {
  const [categories, setCategories] = useState([])
  const [orders, setOrders] = useState([])
  const [form, setForm] = useState(EMPTY_PRODUCT)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  const loadOrders = () => {
    api.allOrders().then((data) => setOrders(data.content)).catch((err) => setError(err.message))
  }

  useEffect(() => {
    api.categories().then(setCategories).catch(() => setCategories([]))
    loadOrders()
  }, [])

  const update = (field) => (event) => setForm({ ...form, [field]: event.target.value })

  const handleCreate = async (event) => {
    event.preventDefault()
    setError(null)
    setMessage(null)
    try {
      const product = await api.createProduct({
        ...form,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
        categoryId: Number(form.categoryId)
      })
      setMessage(`Товар «${product.name}» додано до каталогу`)
      setForm(EMPTY_PRODUCT)
    } catch (err) {
      setError(err.message)
    }
  }

  const handleStatus = async (orderId, status) => {
    try {
      await api.updateOrderStatus(orderId, status)
      loadOrders()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="admin">
      <p className="eyebrow mono">Адміністрування</p>
      <h1>Каталог і замовлення</h1>

      <section className="panel">
        <h2>Новий товар</h2>
        <form className="form" onSubmit={handleCreate}>
          <div className="field-row">
            <label className="field">
              <span className="mono">Назва</span>
              <input className="input" required value={form.name} onChange={update('name')} />
            </label>
            <label className="field">
              <span className="mono">Бренд</span>
              <input className="input" required value={form.brand} onChange={update('brand')} />
            </label>
          </div>
          <label className="field">
            <span className="mono">Опис</span>
            <textarea className="input" rows="2" value={form.description} onChange={update('description')} />
          </label>
          <div className="field-row">
            <label className="field">
              <span className="mono">Ціна, грн</span>
              <input className="input mono" type="number" step="0.01" required value={form.price} onChange={update('price')} />
            </label>
            <label className="field">
              <span className="mono">Залишок</span>
              <input className="input mono" type="number" required value={form.stockQuantity} onChange={update('stockQuantity')} />
            </label>
            <label className="field">
              <span className="mono">Категорія</span>
              <select className="input" required value={form.categoryId} onChange={update('categoryId')}>
                <option value="">—</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>{category.name}</option>
                ))}
              </select>
            </label>
          </div>

          {message && <p className="notice">{message}</p>}
          {error && <p className="alert">{error}</p>}

          <button className="btn-primary">Додати товар</button>
        </form>
      </section>

      <section className="panel">
        <h2>Усі замовлення</h2>
        {orders.length === 0 && <p className="mono muted">Замовлень ще немає.</p>}
        {orders.map((order) => (
          <article key={order.id} className="order">
            <div className="order-head mono">
              <span>№{order.id}</span>
              <span>{order.email}</span>
              <span>{formatDate(order.createdAt)}</span>
              <span className="price">{formatPrice(order.totalPrice)}</span>
            </div>
            <div className="order-foot">
              <span className="mono muted">{order.customerName} · {order.deliveryAddress}</span>
              <select
                className="input input-status"
                value={order.status}
                onChange={(event) => handleStatus(order.id, event.target.value)}
              >
                {Object.entries(STATUS_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
            </div>
          </article>
        ))}
      </section>
    </div>
  )
}
