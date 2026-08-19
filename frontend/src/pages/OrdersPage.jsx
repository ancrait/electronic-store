import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client.js'
import { formatDate, formatPrice, STATUS_LABELS } from '../api/format.js'

export default function OrdersPage() {
  const [orders, setOrders] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.myOrders()
      .then((data) => setOrders(data.content))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="mono muted">Завантаження…</p>
  if (error) return <p className="alert">{error}</p>

  if (orders.length === 0) {
    return (
      <div className="empty">
        <h2>Замовлень поки немає</h2>
        <p>Перше можна оформити з <Link to="/">каталогу</Link>.</p>
      </div>
    )
  }

  return (
    <div className="orders">
      <p className="eyebrow mono">Історія</p>
      <h1>Мої замовлення</h1>

      {orders.map((order) => (
        <article key={order.id} className="order">
          <div className="order-head mono">
            <span>№{order.id}</span>
            <span>{formatDate(order.createdAt)}</span>
            <span className={`badge badge-${order.status.toLowerCase()}`}>{STATUS_LABELS[order.status]}</span>
          </div>
          <ul className="order-items">
            {order.items.map((item) => (
              <li key={item.productId}>
                <span>{item.productName}</span>
                <span className="mono muted">{item.quantity} × {formatPrice(item.price)}</span>
              </li>
            ))}
          </ul>
          <div className="order-foot">
            <span className="mono muted">{order.deliveryAddress}</span>
            <span className="price mono">{formatPrice(order.totalPrice)}</span>
          </div>
        </article>
      ))}
    </div>
  )
}
