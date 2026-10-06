import { useState, useEffect, useContext } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'
import api from '../api/client'
import { formatDate, getStatusColor } from '../utils/helpers'
import { StatusBadge, Skeleton, Toast, EmptyState } from '../components/UI'
import styles from '../styles/dashboard.module.css'

export default function ComplainantDashboard() {
  const { user } = useContext(AuthContext)
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)
  const [unreadCount, setUnreadCount] = useState(0)
  const [summary, setSummary] = useState({ total: 0, resolved: 0, pending: 0 })

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [complaintsRes, notificationsRes] = await Promise.all([
        api.get('/complaints/my?page=0&size=5&sort=createdAt,desc'),
        api.get('/notifications/unread-count')
      ])

      setComplaints(complaintsRes.data.content || [])
      setUnreadCount(notificationsRes.data.unreadCount || 0)

      const total = complaintsRes.data.totalElements || 0
      const resolved = complaintsRes.data.content?.filter(c => c.status === 'RESOLVED').length || 0
      setSummary({ total, resolved, pending: total - resolved })
    } catch (err) {
      console.error('Failed to fetch dashboard data', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>My Complaints</h1>
        <p>Welcome back, {user?.fullName}</p>
      </div>

      <div className={styles.summaryCards}>
        <div className={styles.card}>
          <div className={styles.cardValue}>{summary.total}</div>
          <div className={styles.cardLabel}>Total Complaints</div>
        </div>
        <div className={styles.card}>
          <div className={styles.cardValue}>{summary.pending}</div>
          <div className={styles.cardLabel}>Pending</div>
        </div>
        <div className={styles.card}>
          <div className={styles.cardValue}>{summary.resolved}</div>
          <div className={styles.cardLabel}>Resolved</div>
        </div>
        {unreadCount > 0 && (
          <div className={styles.card}>
            <div className={styles.cardValue}>{unreadCount}</div>
            <div className={styles.cardLabel}>Unread Notifications</div>
          </div>
        )}
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Recent Complaints</h2>
          <a href="/new-complaint" className={styles.link}>File new complaint →</a>
        </div>

        {loading ? (
          <div className={styles.list}>
            {[1, 2, 3].map(i => (
              <div key={i} className={styles.listItem}>
                <Skeleton width="60%" height="1rem" />
              </div>
            ))}
          </div>
        ) : complaints.length === 0 ? (
          <EmptyState title="No complaints yet" message="File your first complaint to get started" />
        ) : (
          <div className={styles.list}>
            {complaints.map(complaint => (
              <a key={complaint.id} href={`/complaint/${complaint.id}`} className={styles.listItem}>
                <div className={styles.itemContent}>
                  <div className={styles.itemTitle}>{complaint.ticketId}</div>
                  <div className={styles.itemText}>{complaint.title}</div>
                  <div className={styles.itemMeta}>{formatDate(complaint.createdAt)}</div>
                </div>
                <StatusBadge status={complaint.status} />
              </a>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
