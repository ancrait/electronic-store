import { useEffect, useState } from 'react'
import { api } from '../api/client.js'
import { useCart } from '../context/CartContext.jsx'
import ProductCard from '../components/ProductCard.jsx'

export default function CatalogPage() {
  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState([])
  const [categoryId, setCategoryId] = useState('')
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState('id:asc')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const { addItem } = useCart()

  useEffect(() => {
    api.categories().then(setCategories).catch(() => setCategories([]))
  }, [])

  useEffect(() => {
    const [sortBy, direction] = sort.split(':')
    const params = { page, size: 9, sortBy, direction }
    if (categoryId) params.categoryId = categoryId
    if (search.trim()) params.search = search.trim()

    setLoading(true)
    api.products(params)
      .then((data) => {
        setProducts(data.content)
        setTotalPages(data.totalPages)
        setError(null)
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [categoryId, search, sort, page])

  return (
    <>
      <section className="hero">
        <p className="eyebrow mono">Каталог · оновлено щодня</p>
        <h1>Техніка, яку<br />перевіряють перед відправкою</h1>
        <p className="hero-text">
          Ноутбуки, смартфони й аудіо з реальним залишком на складі. Наявність оновлюється
          одразу після кожного замовлення.
        </p>
      </section>

      <section className="filters">
        <input
          className="input"
          placeholder="Пошук за назвою або брендом"
          value={search}
          onChange={(event) => { setSearch(event.target.value); setPage(0) }}
        />
        <select className="input" value={categoryId} onChange={(event) => { setCategoryId(event.target.value); setPage(0) }}>
          <option value="">Усі категорії</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>{category.name}</option>
          ))}
        </select>
        <select className="input" value={sort} onChange={(event) => setSort(event.target.value)}>
          <option value="id:asc">За замовчуванням</option>
          <option value="price:asc">Спочатку дешевші</option>
          <option value="price:desc">Спочатку дорожчі</option>
          <option value="name:asc">За назвою</option>
        </select>
      </section>

      {error && <p className="alert">{error}</p>}
      {loading && <p className="mono muted">Завантаження…</p>}

      {!loading && products.length === 0 && !error && (
        <div className="empty">
          <h2>Нічого не знайшли</h2>
          <p>Спробуйте інший запит або скиньте фільтри.</p>
        </div>
      )}

      <section className="grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} onAdd={addItem} />
        ))}
      </section>

      {totalPages > 1 && (
        <div className="pagination mono">
          <button className="btn-ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>← назад</button>
          <span>{page + 1} / {totalPages}</span>
          <button className="btn-ghost" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>далі →</button>
        </div>
      )}
    </>
  )
}
