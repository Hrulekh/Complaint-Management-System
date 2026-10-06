import { useNavigate } from 'react-router-dom'
import styles from '../styles/auth.module.css'

export default function NotFoundPage() {
  const navigate = useNavigate()

  return (
    <div className={styles.container}>
      <div className={styles.formWrapper} style={{ textAlign: 'center', maxWidth: '500px' }}>
        <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>404</div>
        <h1 className={styles.title}>Page Not Found</h1>
        <p className={styles.subtitle}>The page you're looking for doesn't exist or has been moved.</p>
        <button
          onClick={() => navigate('/')}
          className={styles.submitButton}
          style={{ marginTop: '2rem' }}
        >
          Go to Dashboard
        </button>
      </div>
    </div>
  )
}
