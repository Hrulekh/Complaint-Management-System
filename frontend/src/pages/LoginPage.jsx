import { useState, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'
import api from '../api/client'
import styles from '../styles/auth.module.css'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useContext(AuthContext)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState('')

  const validateForm = () => {
    const newErrors = {}

    if (!email) newErrors.email = 'Email is required'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = 'Invalid email format'
    }

    if (!password) newErrors.password = 'Password is required'

    if (password && password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters'
    }

    return newErrors
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const newErrors = validateForm()

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    setLoading(true)
    setApiError('')

    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setApiError(err.response?.data?.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.container}>

      {/* RIGHT SIDE */}
      <div className={styles.rightSection}>
        <div className={styles.formWrapper}>

          <div className={styles.header}>
            <h2 className={styles.loginTitle}>Login</h2>
            <p className={styles.loginSubtitle}>
              Welcome back. Please enter your details.
            </p>
          </div>

          {apiError && (
            <div className={styles.error}>
              {apiError}
            </div>
          )}

          <form onSubmit={handleSubmit} className={styles.form}>

            <div className={styles.formGroup}>
              <label htmlFor="email">Email</label>

              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value)
                  setErrors({ ...errors, email: '' })
                }}
                className={errors.email ? styles.inputError : ''}
                disabled={loading}
              />

              {errors.email && (
                <span className={styles.fieldError}>
                  {errors.email}
                </span>
              )}
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="password">Password</label>

              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value)
                  setErrors({ ...errors, password: '' })
                }}
                className={errors.password ? styles.inputError : ''}
                disabled={loading}
              />

              {errors.password && (
                <span className={styles.fieldError}>
                  {errors.password}
                </span>
              )}
            </div>

            <button
              type="submit"
              className={styles.submitButton}
              disabled={loading}
            >
              {loading ? 'Logging in...' : 'Login'}
            </button>

          </form>

          <p className={styles.footer}>
            Don't have an account?{' '}
            <a href="/register">Register here</a>
          </p>

        </div>
      </div>

    </div>
  )
}