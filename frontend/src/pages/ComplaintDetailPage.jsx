import { useState, useEffect, useContext } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'
import api from '../api/client'
import { formatDate } from '../utils/helpers'
import { StatusBadge } from '../components/UI'
import styles from '../styles/detail.module.css'

const STATUS_FLOW = {
  SUBMITTED: ['ASSIGNED', 'IN_PROGRESS'],
  ASSIGNED: ['IN_PROGRESS'],
  IN_PROGRESS: ['RESOLVED'],
  RESOLVED: ['CLOSED'],
  REOPENED: ['IN_PROGRESS'],
}

export default function ComplaintDetailPage() {
  const { id } = useParams()
  const { user } = useContext(AuthContext)
  const navigate = useNavigate()

  const [complaint, setComplaint] = useState(null)
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Action panels
  const [remarkText, setRemarkText] = useState('')
  const [newStatus, setNewStatus] = useState('')
  const [resolution, setResolution] = useState('')
  const [feedbackRating, setFeedbackRating] = useState(5)
  const [feedbackComment, setFeedbackComment] = useState('')
  const [reopenReason, setReopenReason] = useState('')
  const [actionLoading, setActionLoading] = useState(false)
  const [toast, setToast] = useState('')

  const isAdmin = user?.role === 'ADMIN'
  const isStaff = user?.role === 'STAFF' || isAdmin

  const load = async () => {
    setLoading(true)
    try {
      const [cRes, hRes] = await Promise.all([
        api.get(`/complaints/${id}`),
        api.get(`/complaints/${id}/history`)
      ])
      setComplaint(cRes.data)
      setHistory(hRes.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Complaint not found')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [id])

  const showToast = msg => { setToast(msg); setTimeout(() => setToast(''), 3000) }

  const handleStatusUpdate = async () => {
    if (!newStatus) return
    setActionLoading(true)
    try {
      await api.patch(`/complaints/${id}/status`, { status: newStatus, remark: remarkText })
      showToast('Status updated successfully.')
      setNewStatus(''); setRemarkText('')
      load()
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
    finally { setActionLoading(false) }
  }

  const handleResolve = async () => {
    if (!resolution.trim()) { showToast('Please enter resolution summary'); return }
    setActionLoading(true)
    try {
      await api.patch(`/complaints/${id}/resolve`, { resolutionSummary: resolution })
      showToast('Complaint resolved.')
      setResolution('')
      load()
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
    finally { setActionLoading(false) }
  }

  const handleClose = async () => {
    setActionLoading(true)
    try {
      await api.patch(`/complaints/${id}/close`)
      showToast('Complaint closed.')
      load()
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
    finally { setActionLoading(false) }
  }

  const handleFeedback = async () => {
    setActionLoading(true)
    try {
      await api.post(`/complaints/${id}/feedback`, { rating: feedbackRating, comment: feedbackComment })
      showToast('Feedback submitted. Thank you.')
      setFeedbackComment('')
      load()
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
    finally { setActionLoading(false) }
  }

  const handleReopen = async () => {
    if (!reopenReason.trim()) { showToast('Please provide a reason to reopen'); return }
    setActionLoading(true)
    try {
      await api.patch(`/complaints/${id}/reopen?reason=${encodeURIComponent(reopenReason)}`)
      showToast('Complaint reopened.')
      setReopenReason('')
      load()
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
    finally { setActionLoading(false) }
  }

  if (loading) return <div className={styles.center}>Loading complaint...</div>
  if (error) return <div className={styles.center}><div className={styles.errorBox}>{error}</div></div>
  if (!complaint) return null

  const canUpdateStatus = isStaff && STATUS_FLOW[complaint.status]
  const canResolve = isStaff && complaint.status === 'IN_PROGRESS'
  const canClose = isStaff && complaint.status === 'RESOLVED'
  const canFeedback = user?.role === 'COMPLAINANT' && complaint.status === 'RESOLVED'
  const canReopen = (user?.role === 'COMPLAINANT' || isAdmin) && (complaint.status === 'RESOLVED' || complaint.status === 'CLOSED')

  return (
    <div className={styles.page}>
      {toast && <div className={styles.toast}>{toast}</div>}

      {/* Back */}
      <button className={styles.backBtn} onClick={() => navigate(-1)}>Back</button>

      {/* Header */}
      <div className={styles.header}>
        <div>
          <div className={styles.ticketId}>{complaint.ticketId}</div>
          <h1 className={styles.title}>{complaint.title}</h1>
          <div className={styles.meta}>
            <span>Category: {complaint.category?.name}</span>
            <span>Priority: {complaint.priority?.name}</span>
            <span>Filed: {formatDate(complaint.createdAt)}</span>
            {complaint.assignedTo && <span>Assigned to: {complaint.assignedTo.fullName}</span>}
          </div>
        </div>
        <StatusBadge status={complaint.status} />
      </div>

      <div className={styles.body}>
        {/* Left: Description + History + Actions */}
        <div className={styles.main}>
          {/* Description */}
          <div className={styles.card}>
            <h3>Description</h3>
            <p className={styles.description}>{complaint.description}</p>
          </div>

          {/* Resolution */}
          {complaint.resolutionSummary && (
            <div className={`${styles.card} ${styles.resolvedCard}`}>
              <h3>Resolution</h3>
              <p>{complaint.resolutionSummary}</p>
            </div>
          )}

          {/* Staff: Update Status */}
          {canUpdateStatus && (
            <div className={styles.card}>
              <h3>Update Status</h3>
              <div className={styles.actionRow}>
                <select value={newStatus} onChange={e => setNewStatus(e.target.value)}>
                  <option value="">Select new status...</option>
                  {(STATUS_FLOW[complaint.status] || []).map(s => (
                    <option key={s} value={s}>{s.replace('_', ' ')}</option>
                  ))}
                </select>
                <input
                  type="text" placeholder="Add a remark (optional)"
                  value={remarkText} onChange={e => setRemarkText(e.target.value)}
                />
                <button className={styles.actionBtn} onClick={handleStatusUpdate} disabled={actionLoading || !newStatus}>
                  Update
                </button>
              </div>
            </div>
          )}

          {/* Staff: Resolve */}
          {canResolve && (
            <div className={styles.card}>
              <h3>Resolve Complaint</h3>
              <textarea
                rows={3} placeholder="Describe the resolution..."
                value={resolution} onChange={e => setResolution(e.target.value)}
              />
              <button className={styles.resolveBtn} onClick={handleResolve} disabled={actionLoading}>
                Mark as Resolved
              </button>
            </div>
          )}

          {/* Staff: Close */}
          {canClose && (
            <div className={styles.card}>
              <h3>Close Complaint</h3>
              <p>The complaint has been resolved. You can now close it.</p>
              <button className={styles.closeBtn} onClick={handleClose} disabled={actionLoading}>
                Close Complaint
              </button>
            </div>
          )}

          {/* Complainant: Feedback */}
          {canFeedback && (
            <div className={styles.card}>
              <h3>Leave Feedback</h3>
              <div className={styles.ratingRow}>
                <label>Rating:</label>
                <select value={feedbackRating} onChange={e => setFeedbackRating(Number(e.target.value))}>
                  {[5,4,3,2,1].map(r => <option key={r} value={r}>{r} Star{r !== 1 ? 's' : ''}</option>)}
                </select>
              </div>
              <textarea
                rows={3} placeholder="Share your experience (optional)..."
                value={feedbackComment} onChange={e => setFeedbackComment(e.target.value)}
              />
              <button className={styles.actionBtn} onClick={handleFeedback} disabled={actionLoading}>
                Submit Feedback
              </button>
            </div>
          )}

          {/* Complainant / Admin: Reopen */}
          {canReopen && (
            <div className={styles.card}>
              <h3>Reopen Complaint</h3>
              <input
                type="text" placeholder="Reason for reopening..."
                value={reopenReason} onChange={e => setReopenReason(e.target.value)}
              />
              <button className={styles.reopenBtn} onClick={handleReopen} disabled={actionLoading}>
                Reopen
              </button>
            </div>
          )}

          {/* History */}
          <div className={styles.card}>
            <h3>Complaint History</h3>
            {history.length === 0 ? (
              <p className={styles.noHistory}>No history recorded yet.</p>
            ) : (
              <div className={styles.timeline}>
                {history.map((h, i) => (
                  <div key={i} className={styles.timelineItem}>
                    <div className={styles.timelineDot} />
                    <div className={styles.timelineContent}>
                      <div className={styles.timelineHeader}>
                        <span className={styles.timelineStatus}>{h.toStatus?.replace('_', ' ')}</span>
                        <span className={styles.timelineDate}>{formatDate(h.changedAt)}</span>
                      </div>
                      {h.remark && <p className={styles.timelineRemark}>{h.remark}</p>}
                      {h.changedBy && <p className={styles.timelineBy}>by {h.changedBy.fullName}</p>}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right: Sidebar Info */}
        <div className={styles.sidebar}>
          <div className={styles.card}>
            <h4>Details</h4>
            <div className={styles.infoRow}><span>Status</span><StatusBadge status={complaint.status} /></div>
            <div className={styles.infoRow}><span>Category</span><strong>{complaint.category?.name}</strong></div>
            <div className={styles.infoRow}><span>Priority</span><strong>{complaint.priority?.name}</strong></div>
            <div className={styles.infoRow}><span>Created</span><strong>{formatDate(complaint.createdAt)}</strong></div>
            {complaint.dueAt && <div className={styles.infoRow}><span>Due</span><strong>{formatDate(complaint.dueAt)}</strong></div>}
            {complaint.assignedTo && <div className={styles.infoRow}><span>Assigned To</span><strong>{complaint.assignedTo.fullName}</strong></div>}
            {complaint.resolvedAt && <div className={styles.infoRow}><span>Resolved</span><strong>{formatDate(complaint.resolvedAt)}</strong></div>}
            {complaint.feedbackRating && (
              <div className={styles.infoRow}>
                <span>Feedback</span>
                <strong>{complaint.feedbackRating} / 5</strong>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
