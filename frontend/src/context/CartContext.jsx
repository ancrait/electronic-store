import { createContext, useContext, useEffect, useState } from 'react'

const CartContext = createContext(null)
const CART_KEY = 'volt.cart'

export function CartProvider({ children }) {
  const [items, setItems] = useState(() => {
    const raw = localStorage.getItem(CART_KEY)
    return raw ? JSON.parse(raw) : []
  })

  useEffect(() => {
    localStorage.setItem(CART_KEY, JSON.stringify(items))
  }, [items])

  const addItem = (product, quantity = 1) => {
    setItems((current) => {
      const existing = current.find((item) => item.productId === product.id)
      if (existing) {
        return current.map((item) =>
          item.productId === product.id
            ? { ...item, quantity: Math.min(item.quantity + quantity, product.stockQuantity) }
            : item
        )
      }
      return [...current, {
        productId: product.id,
        name: product.name,
        brand: product.brand,
        price: product.price,
        stockQuantity: product.stockQuantity,
        quantity
      }]
    })
  }

  const setQuantity = (productId, quantity) => {
    setItems((current) =>
      current.map((item) => (item.productId === productId ? { ...item, quantity } : item))
    )
  }

  const removeItem = (productId) => {
    setItems((current) => current.filter((item) => item.productId !== productId))
  }

  const clear = () => setItems([])

  const total = items.reduce((sum, item) => sum + item.price * item.quantity, 0)
  const count = items.reduce((sum, item) => sum + item.quantity, 0)

  return (
    <CartContext.Provider value={{ items, addItem, setQuantity, removeItem, clear, total, count }}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)
