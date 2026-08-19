import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'

export default function Header() {
  const { user, logout, isAdmin } = useAuth()
  const { count } = useCart()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="header">
      <Link to="/" className="logo">
        VOLT<span className="logo-mark">⚡</span>
      </Link>

      <nav className="nav">
        <NavLink to="/" end>Каталог</NavLink>
        {user && <NavLink to="/orders">Замовлення</NavLink>}
        {isAdmin && <NavLink to="/admin">Адмінка</NavLink>}
      </nav>

      <div className="header-actions">
        <Link to="/cart" className="cart-link mono">
          Кошик<span className="cart-count">{count}</span>
        </Link>
        {user ? (
          <div className="user-box">
            <span className="mono user-name">{user.firstName}</span>
            <button className="btn-ghost" onClick={handleLogout}>Вийти</button>
          </div>
        ) : (
          <Link to="/login" className="btn-primary">Увійти</Link>
        )}
      </div>
    </header>
  )
}
