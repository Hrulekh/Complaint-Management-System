import { useState, useEffect } from 'react'
import api from '../api/client'
import { formatDate } from '../utils/helpers'
import styles from '../styles/list.module.css'

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const load = async (pg = 0) => {
    setLoading(true)
    try {
      const res = await api.get(`/notifications?page=${pg}&size=15&sort=createdAt,desc`)
      setNotifications(res.data.content || [])
      setTotalPages(res.data.totalPages || 0)
      setPage(pg)
    } catch (err) { console.error(err) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const markRead = async id => {
    try {
      await api.patch(`/notifications/${id}/read`)
      setNotifications(ns => ns.map(n => n.id === id ? { ...n, readFlag: true } : n))
    } catch {}
  }

  const markAllRead = async () => {
    try {
      await api.patch('/notifications/read-all')
      setNotifications(ns => ns.map(n => ({ ...n, readFlag: true })))
    } catch {}
  }

  const unread = notifications.filter(n => !n.readFlag).length

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1>Notifications</h1>
          <p>{unread > 0 ? `${unread} unread` : 'All caught up!'}</p>
        </div>
        {unread > 0 && (
          <button className={styles.secondaryBtn} onClick={markAllRead}>
            Mark all as read
          </button>
        )}
      </div>

      {loading ? (
        <div className={styles.loadingWrap}>
          {[1,2,3,4].map(i => <div key={i} className={styles.skeletonRow} />)}
        </div>
      ) : notifications.length === 0 ? (
        <div className={styles.emptyCenter}>
          <div className={styles.emptyIcon}>—</div>
          <h3>No notifications yet</h3>
          <p>You'll be notified when something important happens to your complaints.</p>
        </div>
      ) : (
        <div className={styles.list}>
          {notifications.map(n => (
            <div
              key={n.id}
              className={`${styles.notifItem} ${!n.readFlag ? styles.notifUnread : ''}`}
              onClick={() => !n.readFlag && markRead(n.id)}
            >
              <div className={styles.notifDot} style={{ opacity: n.readFlag ? 0 : 1 }} />
              <div className={styles.notifBody}>
                <div className={styles.notifType}>{n.eventType?.replace(/_/g, ' ')}</div>
                <div className={styles.notifMsg}>{n.message}</div>
                <div className={styles.notifDate}>{formatDate(n.createdAt)}</div>
              </div>
              {!n.readFlag && (
                <button
                  className={styles.readBtn}
                  onClick={e => { e.stopPropagation(); markRead(n.id) }}
                >
                  Mark read
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className={styles.pagination}>
          <button disabled={page === 0} onClick={() => load(page - 1)}>← Prev</button>
          <span>Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => load(page + 1)}>Next →</button>
        </div>
      )}
    </div>
  )
}
