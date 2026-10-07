import { useState, useEffect, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'
import api from '../api/client'
import { formatDate } from '../utils/helpers'
import { StatusBadge, Skeleton, EmptyState } from '../components/UI'
import styles from '../styles/dashboard.module.css'

export default function ComplainantDashboard() {
  const { user } = useContext(AuthContext)
  const navigate = useNavigate()
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)
  const [unreadCount, setUnreadCount] = useState(0)
  const [summary, setSummary] = useState({ total: 0, resolved: 0, pending: 0 })

  const isStaff = user?.role === 'STAFF'

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

  const headingText = isStaff ? 'Assigned Complaints' : 'My Complaints'
  const subText = isStaff
    ? `Welcome back, ${user?.fullName}. Showing your assigned complaints.`
    : `Welcome back, ${user?.fullName}`

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>{headingText}</h1>
        <p>{subText}</p>
      </div>

      <div className={styles.summaryCards}>
        <div className={styles.card}>
          <div className={styles.cardValue}>{summary.total}</div>
          <div className={styles.cardLabel}>Total</div>
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
          <div
            className={styles.card}
            style={{ cursor: 'pointer' }}
            onClick={() => navigate('/notifications')}
          >
            <div className={styles.cardValue}>{unreadCount}</div>
            <div className={styles.cardLabel}>Unread Alerts</div>
          </div>
        )}
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Recent Complaints</h2>
          {/* Only COMPLAINANT can file new complaints */}
          {!isStaff && (
            <a href="/new-complaint" className={styles.link}>File new complaint</a>
          )}
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
          <EmptyState
            title={isStaff ? 'No assigned complaints' : 'No complaints yet'}
            message={isStaff ? 'No complaints have been assigned to you.' : 'File your first complaint to get started.'}
          />
        ) : (
          <div className={styles.list}>
            {complaints.map(complaint => (
              <a
                key={complaint.id}
                href={`/complaint/${complaint.id}`}
                className={styles.listItem}
              >
              <div className={styles.itemContent}>
                  <div className={styles.itemTitle}>{complaint.ticketId}</div>
                  <div className={styles.itemText}>{complaint.title}</div>
                  <div className={styles.itemMeta}>
                    {formatDate(complaint.createdAt)}
                    {isStaff && complaint.createdBy && ` - Filed by: ${complaint.createdBy.fullName}`}
                  </div>
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
