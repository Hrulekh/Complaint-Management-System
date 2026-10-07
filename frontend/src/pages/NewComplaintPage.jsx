import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import styles from '../styles/forms.module.css'

export default function NewComplaintPage() {
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [priorities, setPriorities] = useState([])
  const [form, setForm] = useState({ title: '', description: '', categoryId: '', priorityId: '' })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [submitError, setSubmitError] = useState('')

  useEffect(() => {
    const load = async () => {
      try {
        // Use /lookup/* endpoints — accessible to all authenticated roles
        const [catRes, priRes] = await Promise.all([
          api.get('/lookup/categories'),
          api.get('/lookup/priorities')
        ])
        setCategories(catRes.data)
        setPriorities(priRes.data)
      } catch {
        setSubmitError('Failed to load categories/priorities. Make sure the backend is running.')
      }
    }
    load()
  }, [])

  const validate = () => {
    const e = {}
    if (!form.title.trim()) e.title = 'Title is required'
    if (!form.description.trim()) e.description = 'Description is required'
    if (!form.categoryId) e.categoryId = 'Category is required'
    if (!form.priorityId) e.priorityId = 'Priority is required'
    return e
  }

  const handleChange = e => {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
    setErrors(ev => ({ ...ev, [e.target.name]: '' }))
  }

  const handleSubmit = async e => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setLoading(true)
    setSubmitError('')
    try {
      const res = await api.post('/complaints', {
        title: form.title,
        description: form.description,
        categoryId: Number(form.categoryId),
        priorityId: Number(form.priorityId)
      })
      navigate(`/complaint/${res.data.id}`)
    } catch (err) {
      setSubmitError(err.response?.data?.message || 'Failed to submit complaint')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.pageWrap}>
      <div className={styles.formCard}>
        <div className={styles.cardHeader}>
          <h1 className={styles.cardTitle}>File a New Complaint</h1>
          <p className={styles.cardSub}>Describe your issue and we will get back to you promptly.</p>
        </div>

        {submitError && <div className={styles.apiError}>{submitError}</div>}

        <form onSubmit={handleSubmit} className={styles.form} noValidate>
          {/* Title */}
          <div className={styles.field}>
            <label htmlFor="title">Complaint Title <span className={styles.req}>*</span></label>
            <input
              id="title" name="title" type="text"
              value={form.title} onChange={handleChange}
              placeholder="Briefly describe the issue"
              className={errors.title ? styles.inputErr : ''}
              disabled={loading}
            />
            {errors.title && <span className={styles.fieldErr}>{errors.title}</span>}
          </div>

          {/* Description */}
          <div className={styles.field}>
            <label htmlFor="description">Description <span className={styles.req}>*</span></label>
            <textarea
              id="description" name="description" rows={5}
              value={form.description} onChange={handleChange}
              placeholder="Provide a detailed description of your complaint..."
              className={errors.description ? styles.inputErr : ''}
              disabled={loading}
            />
            {errors.description && <span className={styles.fieldErr}>{errors.description}</span>}
          </div>

          {/* Row: Category + Priority */}
          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="categoryId">Category <span className={styles.req}>*</span></label>
              <select
                id="categoryId" name="categoryId"
                value={form.categoryId} onChange={handleChange}
                className={errors.categoryId ? styles.inputErr : ''}
                disabled={loading}
              >
                <option value="">Select category...</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              {errors.categoryId && <span className={styles.fieldErr}>{errors.categoryId}</span>}
            </div>

            <div className={styles.field}>
              <label htmlFor="priorityId">Priority <span className={styles.req}>*</span></label>
              <select
                id="priorityId" name="priorityId"
                value={form.priorityId} onChange={handleChange}
                className={errors.priorityId ? styles.inputErr : ''}
                disabled={loading}
              >
                <option value="">Select priority...</option>
                {priorities.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
              {errors.priorityId && <span className={styles.fieldErr}>{errors.priorityId}</span>}
            </div>
          </div>

          {/* Actions */}
          <div className={styles.actions}>
            <button type="button" className={styles.cancelBtn} onClick={() => navigate('/dashboard')} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className={styles.submitBtn} disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Complaint'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
