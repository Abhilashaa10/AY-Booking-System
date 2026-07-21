function formatEventDate(dateStr) {
  const [datePart, timePart] = dateStr.split(' ')
  const [year, month, day] = datePart.split('-')
  const date = new Date(year, month - 1, day, ...timePart.split(':'))
  return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

export default formatEventDate
