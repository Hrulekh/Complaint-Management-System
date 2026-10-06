import styles from '../styles/components.module.css'

export function Skeleton({ width = '100%', height = '1rem', count = 1 }) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className={styles.skeleton} style={{ width, height }} />
      ))}
    </>
  )
}

export function Toast({ message, type = 'success', onClose }) {
  return (
    <div className={`${styles.toast} ${styles[`toast-${type}`]}`}>
      <p>{message}</p>
      <button onClick={onClose} className={styles.toastClose}>×</button>
    </div>
  )
}

export function EmptyState({ icon = '📋', title, message }) {
  return (
    <div className={styles.emptyState}>
      <div className={styles.emptyIcon}>{icon}</div>
      <h3>{title}</h3>
      <p>{message}</p>
    </div>
  )
}

export function StatusBadge({ status }) {
  const statusLabels = {
    SUBMITTED: 'Submitted',
    ASSIGNED: 'Assigned',
    IN_PROGRESS: 'In Progress',
    RESOLVED: 'Resolved',
    REOPENED: 'Reopened',
    CLOSED: 'Closed'
  }

  return (
    <span className={`${styles.badge} ${styles[`badge-${status}`]}`}>
      {statusLabels[status] || status}
    </span>
  )
}
