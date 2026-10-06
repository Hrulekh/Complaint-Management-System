export function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function getStatusColor(status) {
  const colors = {
    SUBMITTED: '#c28a1b',
    ASSIGNED: '#6c757d',
    IN_PROGRESS: '#0d6efd',
    RESOLVED: '#2f5d46',
    REOPENED: '#ff6b6b',
    CLOSED: '#1d1b18'
  }
  return colors[status] || '#666'
}

export function getPriorityColor(level) {
  if (level >= 3) return '#b5441f'
  if (level >= 2) return '#c28a1b'
  return '#2f5d46'
}
