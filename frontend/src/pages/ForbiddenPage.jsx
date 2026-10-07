import { useNavigate } from 'react-router-dom'

export default function ForbiddenPage() {
  const navigate = useNavigate()

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      padding: '2rem',
      textAlign: 'center',
      fontFamily: "'DM Sans', sans-serif",
      background: 'var(--bg-primary)'
    }}>
      <h1 style={{
        fontFamily: "'Fraunces', serif",
        fontSize: '6rem',
        color: 'var(--accent)',
        marginBottom: '0',
        lineHeight: 1
      }}>
        403
      </h1>
      <h2 style={{
        fontFamily: "'Fraunces', serif",
        fontSize: '1.5rem',
        color: 'var(--text-primary)',
        fontWeight: 600,
        marginBottom: '0.75rem',
        marginTop: '0.5rem'
      }}>
        Access Denied
      </h2>
      <p style={{ color: '#888', marginBottom: '2rem', maxWidth: 400, fontSize: '0.95rem' }}>
        You do not have permission to access this page. Contact an administrator if you believe this is a mistake.
      </p>
      <button
        onClick={() => navigate('/dashboard')}
        style={{
          padding: '0.7rem 1.75rem',
          borderRadius: '999px',
          background: 'var(--accent)',
          color: 'white',
          border: 'none',
          cursor: 'pointer',
          fontSize: '0.95rem',
          fontWeight: 700,
          fontFamily: 'inherit',
          transition: 'background 150ms'
        }}
      >
        Back to Dashboard
      </button>
    </div>
  )
}
