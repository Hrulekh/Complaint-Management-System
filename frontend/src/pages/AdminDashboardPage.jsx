import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import { formatDate } from '../utils/helpers'
import { StatusBadge } from '../components/UI'
import styles from '../styles/admin.module.css'

const TABS = ['Overview', 'Complaints', 'Users', 'Categories']

export default function AdminDashboardPage() {
  const navigate = useNavigate()
  const [tab, setTab] = useState('Overview')

  // Overview
  const [report, setReport] = useState(null)

  // Complaints
  const [complaints, setComplaints] = useState([])
  const [compLoading, setCompLoading] = useState(false)
  const [compPage, setCompPage] = useState(0)
  const [compTotalPages, setCompTotalPages] = useState(0)
  const [staff, setStaff] = useState([])
  const [assigningId, setAssigningId] = useState(null)
  const [assignStaffId, setAssignStaffId] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  // Users
  const [users, setUsers] = useState([])
  const [userPage, setUserPage] = useState(0)
  const [userTotalPages, setUserTotalPages] = useState(0)

  // Categories
  const [categories, setCategories] = useState([])
  const [priorities, setPriorities] = useState([])
  const [newCat, setNewCat] = useState({ name: '', description: '' })
  const [newPri, setNewPri] = useState({ name: '', level: '', slaHours: '' })

  const [toast, setToast] = useState('')
  const showToast = msg => { setToast(msg); setTimeout(() => setToast(''), 3000) }

  // Load overview
  useEffect(() => {
    if (tab === 'Overview') {
      api.get('/admin/reports/summary').then(r => setReport(r.data)).catch(console.error)
    }
    if (tab === 'Complaints') {
      loadComplaints(0)
      api.get('/admin/staff').then(r => setStaff(r.data)).catch(console.error)
    }
    if (tab === 'Users') loadUsers(0)
    if (tab === 'Categories') {
      api.get('/admin/categories').then(r => setCategories(r.data)).catch(console.error)
      api.get('/admin/priorities').then(r => setPriorities(r.data)).catch(console.error)
    }
  }, [tab])

  const loadComplaints = async (pg = 0, st = statusFilter) => {
    setCompLoading(true)
    try {
      const params = new URLSearchParams({ page: pg, size: 10, sort: 'createdAt,desc' })
      if (st) params.append('status', st)
      const res = await api.get(`/complaints?${params}`)
      setComplaints(res.data.content || [])
      setCompTotalPages(res.data.totalPages || 0)
      setCompPage(pg)
    } catch (err) { console.error(err) }
    finally { setCompLoading(false) }
  }

  const loadUsers = async (pg = 0) => {
    try {
      const res = await api.get(`/admin/users?page=${pg}&size=10`)
      setUsers(res.data.content || [])
      setUserTotalPages(res.data.totalPages || 0)
      setUserPage(pg)
    } catch (err) { console.error(err) }
  }

  const handleAssign = async (complaintId) => {
    if (!assignStaffId) return
    try {
      await api.patch(`/complaints/${complaintId}/assign`, { staffId: Number(assignStaffId) })
      showToast('Complaint assigned!')
      setAssigningId(null)
      setAssignStaffId('')
      loadComplaints(compPage)
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
  }

  const handleRoleChange = async (userId, role) => {
    try {
      await api.patch(`/admin/users/${userId}/role?role=${role}`)
      showToast('Role updated!')
      loadUsers(userPage)
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
  }

  const handleToggleActive = async (userId, active) => {
    try {
      await api.patch(`/admin/users/${userId}/active?active=${!active}`)
      showToast(`User ${!active ? 'activated' : 'deactivated'}!`)
      loadUsers(userPage)
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
  }

  const handleCreateCategory = async e => {
    e.preventDefault()
    try {
      await api.post('/admin/categories', newCat)
      showToast('Category created!')
      setNewCat({ name: '', description: '' })
      api.get('/admin/categories').then(r => setCategories(r.data))
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
  }

  const handleCreatePriority = async e => {
    e.preventDefault()
    try {
      await api.post('/admin/priorities', { ...newPri, level: Number(newPri.level), slaHours: Number(newPri.slaHours) })
      showToast('Priority created!')
      setNewPri({ name: '', level: '', slaHours: '' })
      api.get('/admin/priorities').then(r => setPriorities(r.data))
    } catch (err) { showToast('Error: ' + (err.response?.data?.message || 'Failed')) }
  }

  return (
    <div className={styles.page}>
      {toast && <div className={styles.toast}>{toast}</div>}

      <div className={styles.pageHeader}>
        <h1>Admin Dashboard</h1>
        <p>Manage complaints, users, categories and reports.</p>
      </div>

      {/* Tabs */}
      <div className={styles.tabs}>
        {TABS.map(t => (
          <button
            key={t}
            className={`${styles.tab} ${tab === t ? styles.tabActive : ''}`}
            onClick={() => setTab(t)}
          >{t}</button>
        ))}
      </div>

      {/* -- OVERVIEW -- */}
      {tab === 'Overview' && report && (
        <div className={styles.overview}>
          <div className={styles.statsGrid}>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.totalComplaints}</div>
              <div className={styles.statLabel}>Total Complaints</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.openComplaints}</div>
              <div className={styles.statLabel}>Open</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.submittedCount}</div>
              <div className={styles.statLabel}>Submitted</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.assignedCount}</div>
              <div className={styles.statLabel}>Assigned</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.inProgressCount}</div>
              <div className={styles.statLabel}>In Progress</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.resolvedCount}</div>
              <div className={styles.statLabel}>Resolved</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.closedCount}</div>
              <div className={styles.statLabel}>Closed</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.overdueCount}</div>
              <div className={styles.statLabel}>Overdue</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNum}>{report.averageResolutionHours != null ? `${report.averageResolutionHours.toFixed(1)}h` : 'N/A'}</div>
              <div className={styles.statLabel}>Avg Resolution Time</div>
            </div>
            {report.averageFeedbackRating > 0 && (
              <div className={styles.statCard}>
                <div className={styles.statNum}>{report.averageFeedbackRating.toFixed(1)} / 5</div>
                <div className={styles.statLabel}>Avg Feedback</div>
              </div>
            )}
          </div>

          {report.byCategory?.length > 0 && (
            <div className={styles.section}>
              <h3>By Category</h3>
              <div className={styles.barGroup}>
                {report.byCategory.map(c => (
                  <div key={c.name} className={styles.barRow}>
                    <span className={styles.barLabel}>{c.name}</span>
                    <div className={styles.barWrap}>
                      <div className={styles.bar} style={{ width: `${Math.min(100, (c.count / report.totalComplaints) * 100)}%` }} />
                    </div>
                    <span className={styles.barCount}>{c.count}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* ── COMPLAINTS ── */}
      {tab === 'Complaints' && (
        <div>
          <div className={styles.filterRow}>
            {['', 'SUBMITTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map(s => (
              <button
                key={s}
                className={`${styles.pill} ${statusFilter === s ? styles.pillActive : ''}`}
                onClick={() => { setStatusFilter(s); loadComplaints(0, s) }}
              >
                {s || 'All'}
              </button>
            ))}
          </div>

          {compLoading ? <div className={styles.loading}>Loading…</div> : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Ticket</th><th>Title</th><th>Category</th>
                  <th>Priority</th><th>Status</th><th>Created</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {complaints.map(c => (
                  <tr key={c.id}>
                    <td><code>{c.ticketId}</code></td>
                    <td
                      className={styles.clickable}
                      onClick={() => navigate(`/complaint/${c.id}`)}
                    >{c.title}</td>
                    <td>{c.category?.name}</td>
                    <td>{c.priority?.name}</td>
                    <td><StatusBadge status={c.status} /></td>
                    <td>{formatDate(c.createdAt)}</td>
                    <td>
                      {assigningId === c.id ? (
                        <div className={styles.assignRow}>
                          <select value={assignStaffId} onChange={e => setAssignStaffId(e.target.value)}>
                            <option value="">Select staff…</option>
                            {staff.map(s => <option key={s.id} value={s.id}>{s.fullName}</option>)}
                          </select>
                          <button className={styles.smallBtn} onClick={() => handleAssign(c.id)}>Assign</button>
                          <button className={styles.cancelSmall} onClick={() => setAssigningId(null)}>✕</button>
                        </div>
                      ) : (
                        <button className={styles.smallBtn} onClick={() => setAssigningId(c.id)}>
                          {c.assignedTo ? 'Reassign' : 'Assign'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {compTotalPages > 1 && (
            <div className={styles.pagination}>
              <button disabled={compPage === 0} onClick={() => loadComplaints(compPage - 1)}>← Prev</button>
              <span>Page {compPage + 1} of {compTotalPages}</span>
              <button disabled={compPage >= compTotalPages - 1} onClick={() => loadComplaints(compPage + 1)}>Next →</button>
            </div>
          )}
        </div>
      )}

      {/* ── USERS ── */}
      {tab === 'Users' && (
        <div>
          <table className={styles.table}>
            <thead>
              <tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id}>
                  <td>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td>
                    <select
                      value={u.role}
                      onChange={e => handleRoleChange(u.id, e.target.value)}
                    >
                      {['COMPLAINANT', 'STAFF', 'ADMIN'].map(r => (
                        <option key={r} value={r}>{r}</option>
                      ))}
                    </select>
                  </td>
                  <td>
                    <span className={u.active ? styles.activeBadge : styles.inactiveBadge}>
                      {u.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <button
                      className={u.active ? styles.cancelSmall : styles.smallBtn}
                      onClick={() => handleToggleActive(u.id, u.active)}
                    >
                      {u.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {userTotalPages > 1 && (
            <div className={styles.pagination}>
              <button disabled={userPage === 0} onClick={() => loadUsers(userPage - 1)}>← Prev</button>
              <span>Page {userPage + 1} of {userTotalPages}</span>
              <button disabled={userPage >= userTotalPages - 1} onClick={() => loadUsers(userPage + 1)}>Next →</button>
            </div>
          )}
        </div>
      )}

      {/* ── CATEGORIES & PRIORITIES ── */}
      {tab === 'Categories' && (
        <div className={styles.twoCol}>
          {/* Categories */}
          <div className={styles.section}>
            <h3>Categories</h3>
            <table className={styles.table}>
              <thead><tr><th>Name</th><th>Description</th></tr></thead>
              <tbody>
                {categories.map(c => <tr key={c.id}><td>{c.name}</td><td>{c.description}</td></tr>)}
              </tbody>
            </table>
            <form onSubmit={handleCreateCategory} className={styles.inlineForm}>
              <input placeholder="Name" value={newCat.name} onChange={e => setNewCat(f => ({ ...f, name: e.target.value }))} required />
              <input placeholder="Description" value={newCat.description} onChange={e => setNewCat(f => ({ ...f, description: e.target.value }))} />
              <button type="submit" className={styles.smallBtn}>+ Add</button>
            </form>
          </div>

          {/* Priorities */}
          <div className={styles.section}>
            <h3>Priorities</h3>
            <table className={styles.table}>
              <thead><tr><th>Name</th><th>Level</th><th>SLA (hrs)</th></tr></thead>
              <tbody>
                {priorities.map(p => <tr key={p.id}><td>{p.name}</td><td>{p.level}</td><td>{p.slaHours}</td></tr>)}
              </tbody>
            </table>
            <form onSubmit={handleCreatePriority} className={styles.inlineForm}>
              <input placeholder="Name" value={newPri.name} onChange={e => setNewPri(f => ({ ...f, name: e.target.value }))} required />
              <input type="number" placeholder="Level" value={newPri.level} onChange={e => setNewPri(f => ({ ...f, level: e.target.value }))} required />
              <input type="number" placeholder="SLA Hours" value={newPri.slaHours} onChange={e => setNewPri(f => ({ ...f, slaHours: e.target.value }))} required />
              <button type="submit" className={styles.smallBtn}>+ Add</button>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
