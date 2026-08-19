export const formatPrice = (value) =>
  new Intl.NumberFormat('uk-UA', { style: 'currency', currency: 'UAH', maximumFractionDigits: 0 })
    .format(Number(value))

export const formatDate = (millis) =>
  new Intl.DateTimeFormat('uk-UA', { dateStyle: 'medium', timeStyle: 'short' })
    .format(new Date(millis))

export const STATUS_LABELS = {
  NEW: 'Новe',
  CONFIRMED: 'Підтверджено',
  SHIPPED: 'Відправлено',
  DELIVERED: 'Доставлено',
  CANCELLED: 'Скасовано'
}
