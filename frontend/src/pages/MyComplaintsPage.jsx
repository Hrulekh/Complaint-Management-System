import { useState, useEffect, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'
import api from '../api/client'
import { formatDate } from '../utils/helpers'
import { StatusBadge, EmptyState } from '../components/UI'
import styles from '../styles/list.module.css'

const STATUSES = ['SUBMITTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'REOPENED', 'CLOSED']

export default function MyComplaintsPage() {
  const navigate = useNavigate()
  const { user } = useContext(AuthContext)
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [searchInput, setSearchInput] = useState('')

  const isStaff = user?.role === 'STAFF'

  const fetchComplaints = async (pg = 0, st = statusFilter, q = search) => {
    setLoading(true)
    try {
      const params = new URLSearchParams({ page: pg, size: 10, sort: 'createdAt,desc' })
      if (st) params.append('status', st)
      if (q) params.append('search', q)
      const res = await api.get(`/complaints/my?${params}`)
      setComplaints(res.data.content || [])
      setTotalPages(res.data.totalPages || 0)
      setPage(pg)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchComplaints() }, [])

  const handleSearch = e => {
    e.preventDefault()
    setSearch(searchInput)
    fetchComplaints(0, statusFilter, searchInput)
  }

  const handleStatus = val => {
    setStatusFilter(val)
    fetchComplaints(0, val, search)
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1>{isStaff ? 'Assigned Complaints' : 'My Complaints'}</h1>
          <p>{isStaff
            ? 'Complaints assigned to you for investigation and resolution.'
            : 'Track the status of all your submitted complaints.'
          }</p>
        </div>
        {!isStaff && (
          <button className={styles.primaryBtn} onClick={() => navigate('/new-complaint')}>
            + New Complaint
          </button>
        )}
      </div>

      {/* Filters */}
      <div className={styles.filters}>
        <form onSubmit={handleSearch} className={styles.searchBox}>
          <input
            type="text"
            placeholder="Search by ticket ID or title..."
            value={searchInput}
            onChange={e => setSearchInput(e.target.value)}
          />
          <button type="submit">Search</button>
        </form>

        <div className={styles.statusTabs}>
          <button
            className={`${styles.tab} ${statusFilter === '' ? styles.tabActive : ''}`}
            onClick={() => handleStatus('')}
          >All</button>
          {STATUSES.map(s => (
            <button
              key={s}
              className={`${styles.tab} ${statusFilter === s ? styles.tabActive : ''}`}
              onClick={() => handleStatus(s)}
            >
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* List */}
      {loading ? (
        <div className={styles.loadingWrap}>
          {[1, 2, 3].map(i => <div key={i} className={styles.skeletonRow} />)}
        </div>
      ) : complaints.length === 0 ? (
        <EmptyState
          title={isStaff ? 'No assigned complaints' : 'No complaints found'}
          message={isStaff
            ? 'No complaints have been assigned to you yet.'
            : 'Try adjusting your filters or file a new complaint.'
          }
        />
      ) : (
        <div className={styles.list}>
          {complaints.map(c => (
            <div key={c.id} className={styles.listItem} onClick={() => navigate(`/complaint/${c.id}`)}>
              <div className={styles.itemLeft}>
                <div className={styles.ticketId}>{c.ticketId}</div>
                <div className={styles.itemTitle}>{c.title}</div>
                <div className={styles.itemMeta}>
                  <span>{c.category?.name}</span>
                  <span className={styles.dot}>-</span>
                  <span>{c.priority?.name}</span>
                  <span className={styles.dot}>-</span>
                  <span>{formatDate(c.createdAt)}</span>
                  {isStaff && c.createdBy && (
                    <>
                      <span className={styles.dot}>-</span>
                      <span>Filed by: {c.createdBy.fullName}</span>
                    </>
                  )}
                </div>
              </div>
              <div className={styles.itemRight}>
                <StatusBadge status={c.status} />
                <span className={styles.arrow}>&rarr;</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className={styles.pagination}>
          <button disabled={page === 0} onClick={() => fetchComplaints(page - 1)}>Prev</button>
          <span>Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => fetchComplaints(page + 1)}>Next</button>
        </div>
      )}
    </div>
  )
}
