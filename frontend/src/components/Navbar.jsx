import { useContext, useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'
import api from '../api/client'
import styles from '../styles/navbar.module.css'

export default function Navbar() {
  const { user, logout } = useContext(AuthContext)
  const navigate = useNavigate()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)

  // Fetch unread notification count on mount and every 60 s
  useEffect(() => {
    if (!user) return
    const load = () => {
      api.get('/notifications/unread-count')
        .then(r => setUnreadCount(r.data.unreadCount || 0))
        .catch(() => {})
    }
    load()
    const interval = setInterval(load, 60000)
    return () => clearInterval(interval)
  }, [user])

  const handleLogout = async () => {
    setMenuOpen(false)
    await logout()
    navigate('/login')
  }

  // Build nav links based on role — per UML use case diagram
  const navLinks = []
  if (user) {
    if (user.role === 'COMPLAINANT') {
      navLinks.push({ label: 'Dashboard', path: '/dashboard' })
      navLinks.push({ label: 'My Complaints', path: '/my-complaints' })
      navLinks.push({ label: 'New Complaint', path: '/new-complaint' })
    } else if (user.role === 'STAFF') {
      navLinks.push({ label: 'Dashboard', path: '/dashboard' })
      navLinks.push({ label: 'Assigned Complaints', path: '/my-complaints' })
    } else if (user.role === 'ADMIN') {
      navLinks.push({ label: 'Admin Panel', path: '/admin' })
    }
  }

  return (
    <nav className={styles.navbar}>
      <div className={styles.inner}>
        {/* Logo */}
        <button
          className={styles.logo}
          onClick={() => user?.role === 'ADMIN' ? navigate('/admin') : navigate('/dashboard')}
        >
          <span className={styles.logoText}>CMS</span>
        </button>

        {/* Desktop nav links */}
        <div className={styles.links}>
          {navLinks.map(link => (
            <button
              key={link.path}
              className={`${styles.navBtn} ${location.pathname === link.path ? styles.active : ''}`}
              onClick={() => navigate(link.path)}
            >
              {link.label}
            </button>
          ))}
        </div>

        {/* Right side */}
        <div className={styles.right}>
          {/* Notification bell */}
          {user && (
            <button
              className={`${styles.bellBtn} ${location.pathname === '/notifications' ? styles.bellActive : ''}`}
              onClick={() => navigate('/notifications')}
              title="Notifications"
              aria-label={`Notifications${unreadCount > 0 ? ` (${unreadCount} unread)` : ''}`}
            >
              <span className={styles.bellIcon}>Alerts</span>
              {unreadCount > 0 && (
                <span className={styles.badge}>{unreadCount > 9 ? '9+' : unreadCount}</span>
              )}
            </button>
          )}

          {user && (
            <div className={styles.userMenu}>
              <button
                className={styles.avatarBtn}
                onClick={() => setMenuOpen(prev => !prev)}
                title={user.fullName}
              >
                <span className={styles.avatarInitial}>
                  {user.fullName?.charAt(0).toUpperCase()}
                </span>
                <span className={styles.userName}>{user.fullName}</span>
                <span className={styles.chevron}>{menuOpen ? '\u25B2' : '\u25BC'}</span>
              </button>

              {menuOpen && (
                <>
                  {/* Click-away overlay */}
                  <div className={styles.overlay} onClick={() => setMenuOpen(false)} />
                  <div className={styles.dropdown}>
                    <div className={styles.dropdownHeader}>
                      <div className={styles.dropdownName}>{user.fullName}</div>
                      <div className={styles.dropdownEmail}>{user.email}</div>
                      <div className={styles.dropdownRole}>{user.role}</div>
                    </div>
                    <hr className={styles.divider} />
                    <button
                      className={styles.dropdownItem}
                      onClick={() => { setMenuOpen(false); navigate('/notifications') }}
                    >
                      Notifications
                      {unreadCount > 0 && <span className={styles.inlineBadge}>{unreadCount}</span>}
                    </button>
                    <hr className={styles.divider} />
                    <button className={styles.logoutBtn} onClick={handleLogout}>
                      Logout
                    </button>
                  </div>
                </>
              )}
            </div>
          )}

          {!user && (
            <button
              className={styles.loginBtn}
              onClick={() => navigate('/login')}
            >
              Login
            </button>
          )}
        </div>
      </div>
    </nav>
  )
}
