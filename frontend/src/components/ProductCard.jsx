import { Link } from 'react-router-dom'
import { formatPrice } from '../api/format.js'

export default function ProductCard({ product, onAdd }) {
  const outOfStock = product.stockQuantity === 0

  return (
    <article className="card">
      <div className="card-strip mono">
        <span>{product.brand}</span>
        <span className={outOfStock ? 'stock stock-out' : 'stock'}>
          {outOfStock ? 'немає' : `${product.stockQuantity} шт.`}
        </span>
      </div>

      <Link to={`/products/${product.id}`} className="card-title">{product.name}</Link>
      <p className="card-text">{product.description}</p>

      <div className="card-footer">
        <span className="price mono">{formatPrice(product.price)}</span>
        <button className="btn-primary" disabled={outOfStock} onClick={() => onAdd(product)}>
          {outOfStock ? 'Немає' : 'У кошик'}
        </button>
      </div>
    </article>
  )
}
