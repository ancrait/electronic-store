import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client.js'
import { useCart } from '../context/CartContext.jsx'
import { formatPrice } from '../api/format.js'

export default function ProductPage() {
  const { productId } = useParams()
  const [product, setProduct] = useState(null)
  const [error, setError] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const { addItem } = useCart()
  const navigate = useNavigate()

  useEffect(() => {
    api.product(productId).then(setProduct).catch((err) => setError(err.message))
  }, [productId])

  if (error) return <p className="alert">{error}</p>
  if (!product) return <p className="mono muted">Завантаження…</p>

  const handleAdd = () => {
    addItem(product, quantity)
    navigate('/cart')
  }

  return (
    <article className="detail">
      <Link to="/" className="mono muted back-link">← до каталогу</Link>

      <div className="detail-head mono">
        <span>{product.brand}</span>
        <span>{product.categoryName}</span>
        <span className={product.stockQuantity === 0 ? 'stock stock-out' : 'stock'}>
          {product.stockQuantity === 0 ? 'немає в наявності' : `${product.stockQuantity} шт. на складі`}
        </span>
      </div>

      <h1 className="detail-title">{product.name}</h1>
      <p className="detail-text">{product.description}</p>

      <div className="detail-buy">
        <span className="price price-lg mono">{formatPrice(product.price)}</span>
        <input
          className="input input-qty mono"
          type="number"
          min="1"
          max={product.stockQuantity}
          value={quantity}
          onChange={(event) => setQuantity(Number(event.target.value))}
        />
        <button className="btn-primary" disabled={product.stockQuantity === 0} onClick={handleAdd}>
          Додати в кошик
        </button>
      </div>
    </article>
  )
}
