import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { formatPrice } from '../api/format.js'

export default function CartPage() {
  const { items, setQuantity, removeItem, total } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  if (items.length === 0) {
    return (
      <div className="empty">
        <h2>Кошик порожній</h2>
        <p>Оберіть щось у <Link to="/">каталозі</Link> — товари збережуться навіть після перезавантаження.</p>
      </div>
    )
  }

  return (
    <div className="cart">
      <p className="eyebrow mono">Кошик</p>
      <h1>{items.length} позиц{items.length === 1 ? 'ія' : 'ій'}</h1>

      <ul className="cart-list">
        {items.map((item) => (
          <li key={item.productId} className="cart-row">
            <div>
              <p className="cart-name">{item.name}</p>
              <p className="mono muted">{item.brand} · залишок {item.stockQuantity} шт.</p>
            </div>
            <input
              className="input input-qty mono"
              type="number"
              min="1"
              max={item.stockQuantity}
              value={item.quantity}
              onChange={(event) => setQuantity(item.productId, Number(event.target.value))}
            />
            <span className="price mono">{formatPrice(item.price * item.quantity)}</span>
            <button className="btn-ghost" onClick={() => removeItem(item.productId)}>Прибрати</button>
          </li>
        ))}
      </ul>

      <div className="cart-total">
        <span className="mono">Разом</span>
        <span className="price price-lg mono">{formatPrice(total)}</span>
      </div>

      <button className="btn-primary btn-block" onClick={() => navigate(user ? '/checkout' : '/login')}>
        {user ? 'Оформити замовлення' : 'Увійти й оформити'}
      </button>
    </div>
  )
}
