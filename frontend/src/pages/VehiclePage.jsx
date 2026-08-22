import { useEffect, useState } from 'react'
import {
  AlertCircle,
  CheckCircle2,
  CarFront,
  CircleAlert,
  Edit3,
  LoaderCircle,
  Plus,
  Save,
  Trash2,
  X,
} from 'lucide-react'
import { userApi } from '../api/client'
import useAuth from '../hooks/useAuth'

const vehicleTypeOptions = ['BIKE', 'CAR']
const ownershipTypeOptions = ['OWN', 'FAMILY', 'OTHER']

function VehiclePage() {
  const { user } = useAuth()
  const [vehicle, setVehicle] = useState(null)
  const [form, setForm] = useState(createEmptyForm())
  const [mode, setMode] = useState('loading')
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false)

  useEffect(() => {
    let isActive = true

    async function loadVehicle() {
      setMode('loading')
      setError('')
      setSuccess('')
      try {
        const { data } = await userApi.get('/api/vehicles/my')
        if (!isActive) return
        setVehicle(data)
        setForm(toEditableForm(data))
        setMode('view')
      } catch (requestError) {
        if (!isActive) return
        const status = requestError.response?.status
        if (status === 404) {
          setVehicle(null)
          setForm(createEmptyForm())
          setMode('empty')
        } else if (status === 403) {
          setError('Only drivers can manage vehicles.')
          setMode('forbidden')
        } else {
          setError(requestError.response?.data?.message || 'We could not load your vehicle right now.')
          setMode('error')
        }
      }
    }

    if (user) loadVehicle()
    return () => { isActive = false }
  }, [user])

  const beginRegister = () => {
    setForm(createEmptyForm())
    setError('')
    setSuccess('')
    setMode('create')
  }

  const beginEdit = () => {
    if (!vehicle) return
    setForm(toEditableForm(vehicle))
    setError('')
    setSuccess('')
    setMode('edit')
  }

  const cancelEditing = () => {
    setForm(vehicle ? toEditableForm(vehicle) : createEmptyForm())
    setError('')
    setDeleteConfirmOpen(false)
    setMode(vehicle ? 'view' : 'empty')
  }

  const saveVehicle = async (event) => {
    event.preventDefault()
    setIsSaving(true)
    setError('')
    setSuccess('')

    const payload = {
      vehicleNumber: form.vehicleNumber.trim(),
      vehicleType: form.vehicleType,
      vehicleModel: form.vehicleModel.trim(),
      vehicleColor: form.vehicleColor.trim(),
      totalSeats: Number(form.totalSeats),
      ownershipType: form.ownershipType,
    }

    try {
      const { data } = mode === 'edit'
        ? await userApi.put('/api/vehicles/my', {
          vehicleModel: payload.vehicleModel,
          vehicleColor: payload.vehicleColor,
          totalSeats: payload.totalSeats,
          ownershipType: payload.ownershipType,
        })
        : await userApi.post('/api/vehicles', payload)

      setVehicle(data)
      setForm(toEditableForm(data))
      setMode('view')
      setSuccess(mode === 'edit' ? 'Your vehicle has been updated successfully.' : 'Your vehicle has been registered successfully.')
    } catch (requestError) {
      setError(extractErrorMessage(requestError, 'Your vehicle could not be saved. Please try again.'))
    } finally {
      setIsSaving(false)
    }
  }

  const deleteVehicle = async () => {
    setIsDeleting(true)
    setError('')
    setSuccess('')
    try {
      await userApi.delete('/api/vehicles/my')
      setVehicle(null)
      setForm(createEmptyForm())
      setMode('empty')
      setSuccess('Your vehicle has been removed.')
      setDeleteConfirmOpen(false)
    } catch (requestError) {
      setError(extractErrorMessage(requestError, 'Your vehicle could not be removed. Please try again.'))
    } finally {
      setIsDeleting(false)
    }
  }

  if (mode === 'loading') {
    return <VehicleState icon={LoaderCircle} message="Loading your vehicle..." spinning />
  }

  return (
    <section className="vehicle-page">
      <div className="vehicle-shell">
        <div className="vehicle-header">
          <div>
            <p className="section-kicker">Driver workspace</p>
            <h1>Vehicle registration</h1>
            <p>Register the vehicle you use for CampusRide and keep its details up to date.</p>
          </div>
          
          {/* {vehicle && mode === 'view' ? (
            <div className="vehicle-header-actions">
              <button className="button button-outline" onClick={beginEdit} type="button"><Edit3 size={16} />Edit</button>
              <button className="button button-danger" onClick={() => setDeleteConfirmOpen(true)} type="button"><Trash2 size={16} />Remove</button>
            </div>
          ) : null} */}
        </div>

        {success && <Feedback type="success">{success}</Feedback>}
        {error && <Feedback type="error">{error}</Feedback>}

        {mode === 'forbidden' ? (
          <AccessDeniedState />
        ) : mode === 'error' ? (
          <VehicleState icon={AlertCircle} message={error || 'We could not load your vehicle right now.'} />
        ) : mode === 'empty' ? (
          <EmptyVehicleState onRegister={beginRegister} />
        ) : vehicle && mode === 'view' ? (
          <VehicleCard vehicle={vehicle} onEdit={beginEdit} onRemove={() => setDeleteConfirmOpen(true)} />
        ) : mode === 'create' || mode === 'edit' ? (
          <VehicleFormCard
            form={form}
            isEditing={mode === 'edit'}
            isSaving={isSaving}
            onCancel={cancelEditing}
            onChange={setForm}
            onSubmit={saveVehicle}
            vehicleExists={Boolean(vehicle)}
          />
        ) : null}
      </div>

      {deleteConfirmOpen && (
        <ConfirmationModal
          isDeleting={isDeleting}
          onCancel={() => setDeleteConfirmOpen(false)}
          onConfirm={deleteVehicle}
        />
      )}
    </section>
  )
}

function VehicleCard({ vehicle, onEdit, onRemove }) {
  const statusLabel = formatLabel(vehicle.status)
  const statusTone = vehicle.status === 'ACTIVE' ? 'bg-[#eaf7ef] text-[#137b43]' : 'bg-[#fff4e5] text-[#b45309]'

  return (
    <article className="vehicle-panel vehicle-card">
      <div className="vehicle-card-header">
        <div className="vehicle-icon"><CarFront size={26} /></div>
        <div>
          <p className="vehicle-card-kicker">Registered vehicle</p>
          <h2>{vehicle.vehicleNumber}</h2>
        </div>
        <span className={`vehicle-status ${statusTone}`}>{statusLabel}</span>
      </div>

      <dl className="vehicle-details-grid">
        <VehicleDetail label="Vehicle number" value={vehicle.vehicleNumber} />
        <VehicleDetail label="Vehicle type" value={formatLabel(vehicle.vehicleType)} />
        <VehicleDetail label="Model" value={vehicle.vehicleModel} />
        <VehicleDetail label="Color" value={vehicle.vehicleColor} />
        <VehicleDetail label="Seats" value={String(vehicle.totalSeats)} />
        <VehicleDetail label="Ownership" value={formatLabel(vehicle.ownershipType)} />
      </dl>

      <div className="vehicle-footer">
        <button className="button button-outline" onClick={onEdit} type="button"><Edit3 size={16} />Edit</button>
        <button className="button button-danger" onClick={onRemove} type="button"><Trash2 size={16} />Remove</button>
      </div>
    </article>
  )
}

function VehicleFormCard({ form, isEditing, isSaving, onCancel, onChange, onSubmit, vehicleExists }) {
  return (
    <article className="vehicle-panel vehicle-form-panel">
      <div className="vehicle-card-header">
        <div className="vehicle-icon">{isEditing ? <Edit3 size={26} /> : <Plus size={26} />}</div>
        <div>
          <p className="vehicle-card-kicker">{isEditing ? 'Update vehicle' : 'Register vehicle'}</p>
          <h2>{isEditing ? 'Edit vehicle details' : 'Add your vehicle'}</h2>
        </div>
      </div>

      <form className="vehicle-form" onSubmit={onSubmit}>
        <div className="vehicle-form-grid">
          <Field label="Vehicle Number" readOnly={vehicleExists && isEditing}>
            <input
              disabled={vehicleExists && isEditing}
              maxLength="30"
              required
              value={form.vehicleNumber}
              onChange={(event) => onChange({ ...form, vehicleNumber: event.target.value })}
            />
          </Field>
          <Field label="Vehicle Type" readOnly={vehicleExists && isEditing}>
            <select
              disabled={vehicleExists && isEditing}
              value={form.vehicleType}
              onChange={(event) => onChange({ ...form, vehicleType: event.target.value })}
            >
              {vehicleTypeOptions.map((option) => <option key={option} value={option}>{formatLabel(option)}</option>)}
            </select>
          </Field>
          <Field label="Vehicle Model">
            <input
              maxLength="80"
              required
              value={form.vehicleModel}
              onChange={(event) => onChange({ ...form, vehicleModel: event.target.value })}
            />
          </Field>
          <Field label="Vehicle Color">
            <input
              maxLength="40"
              required
              value={form.vehicleColor}
              onChange={(event) => onChange({ ...form, vehicleColor: event.target.value })}
            />
          </Field>
          <Field label="Total Seats" fullWidth={false}>
            <input
              min="1"
              max="8"
              required
              type="number"
              value={form.totalSeats}
              onChange={(event) => onChange({ ...form, totalSeats: event.target.value })}
            />
          </Field>
          <Field label="Ownership Type" fullWidth={false}>
            <select
              value={form.ownershipType}
              onChange={(event) => onChange({ ...form, ownershipType: event.target.value })}
            >
              {ownershipTypeOptions.map((option) => <option key={option} value={option}>{formatLabel(option)}</option>)}
            </select>
          </Field>
        </div>

        <div className="vehicle-form-actions">
          <button className="button button-outline" disabled={isSaving} onClick={onCancel} type="button"><X size={16} />Cancel</button>
          <button className="button button-primary" disabled={isSaving} type="submit">
            {isSaving ? <LoaderCircle className="animate-spin" size={16} /> : <Save size={16} />}
            {isSaving ? 'Saving...' : isEditing ? 'Save changes' : 'Register vehicle'}
          </button>
        </div>
      </form>
    </article>
  )
}

function AccessDeniedState() {
  return (
    <article className="vehicle-panel vehicle-empty-state">
      <div className="vehicle-empty-icon vehicle-empty-icon-danger"><AlertCircle size={28} /></div>
      <h2>Vehicle access is restricted</h2>
      <p>Only drivers can register and manage a vehicle in CampusRide.</p>
    </article>
  )
}

function EmptyVehicleState({ onRegister }) {
  return (
    <article className="vehicle-panel vehicle-empty-state">
      <div className="vehicle-empty-icon"><CarFront size={28} /></div>
      <h2>No vehicle registered</h2>
      <p>Register your vehicle so students can ride with you on CampusRide.</p>
      <button className="button button-primary" onClick={onRegister} type="button"><Plus size={16} />Register Vehicle</button>
    </article>
  )
}

function ConfirmationModal({ isDeleting, onCancel, onConfirm }) {
  return (
    <div className="vehicle-modal-backdrop" role="presentation">
      <div className="vehicle-modal" role="dialog" aria-modal="true" aria-labelledby="vehicle-remove-title">
        <div className="vehicle-modal-icon"><CircleAlert size={24} /></div>
        <h2 id="vehicle-remove-title">Remove vehicle?</h2>
        <p>This will permanently delete your registered vehicle from CampusRide.</p>
        <div className="vehicle-modal-actions">
          <button className="button button-outline" disabled={isDeleting} onClick={onCancel} type="button">Cancel</button>
          <button className="button button-danger" disabled={isDeleting} onClick={onConfirm} type="button">
            {isDeleting ? <LoaderCircle className="animate-spin" size={16} /> : <Trash2 size={16} />}
            {isDeleting ? 'Removing...' : 'Remove vehicle'}
          </button>
        </div>
      </div>
    </div>
  )
}

function VehicleDetail({ label, value }) {
  return (
    <div className="vehicle-detail">
      <dt>{label}</dt>
      <dd>{value || 'Not available'}</dd>
    </div>
  )
}

function Field({ label, children }) {
  return (
    <label className="vehicle-field">
      <span>{label}</span>
      {children}
    </label>
  )
}

function VehicleState({ icon: Icon, message, spinning = false }) {
  return (
    <section className="vehicle-page">
      <div className="vehicle-shell">
        <article className="vehicle-panel vehicle-empty-state">
          <Icon className={spinning ? 'animate-spin' : ''} size={30} />
          <p>{message}</p>
        </article>
      </div>
    </section>
  )
}

function Feedback({ children, type }) {
  const isSuccess = type === 'success'
  return (
    <div className={`mb-5 flex items-center gap-2 rounded-lg border px-4 py-3 text-sm font-medium ${isSuccess ? 'border-green-100 bg-green-50 text-green-800' : 'border-red-100 bg-red-50 text-red-700'}`}>
      {isSuccess ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
      {children}
    </div>
  )
}

function createEmptyForm() {
  return {
    vehicleNumber: '',
    vehicleType: 'BIKE',
    vehicleModel: '',
    vehicleColor: '',
    totalSeats: '1',
    ownershipType: 'OWN',
  }
}

function toEditableForm(vehicle) {
  return {
    vehicleNumber: vehicle?.vehicleNumber || '',
    vehicleType: vehicle?.vehicleType || 'BIKE',
    vehicleModel: vehicle?.vehicleModel || '',
    vehicleColor: vehicle?.vehicleColor || '',
    totalSeats: vehicle?.totalSeats?.toString() || '1',
    ownershipType: vehicle?.ownershipType || 'OWN',
  }
}

function formatLabel(value) {
  return value ? `${value.charAt(0)}${value.slice(1).toLowerCase()}` : ''
}

function extractErrorMessage(requestError, fallbackMessage) {
  const fieldErrors = requestError.response?.data?.fieldErrors
  return Object.values(fieldErrors || {})[0] || requestError.response?.data?.message || fallbackMessage
}

export default VehiclePage
