import { useState, useEffect } from 'react'
import axios from 'axios'
import {
  Bell,
  CheckCircle2,
  Database,
  Globe,
  Lock,
  Save,
  Shield,
  Users,
  AlertCircle,
} from 'lucide-react'

function AdminSettings() {
  const [settings, setSettings] = useState({
    siteName: 'CampusRide',
    supportEmail: 'support@campusride.com',
    supportPhone: '+91 9999999999',
    requireDriverApproval: true,
    allowRegistration: true,
    maxRidesPerDay: 10,
    minDriverRating: 4.0,
    maxPassengersPerRide: 4,
    enableEmailNotifications: true,
    enableSmsNotifications: false,
    autoApproveDrivers: false,
  })
  const [success, setSuccess] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchSettings()
  }, [])

  const fetchSettings = async () => {
    try {
      const token = localStorage.getItem('token')
      const response = await axios.get('http://localhost:8080/api/admin/settings', {
        headers: { Authorization: `Bearer ${token}` }
      })
      if (response.data) {
        setSettings(response.data)
      }
    } catch (err) {
      console.log('Using default settings')
    }
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    setSuccess('')

    try {
      const token = localStorage.getItem('token')
      await axios.put('http://localhost:8080/api/admin/settings', settings, {
        headers: { 
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      setSuccess('Settings saved successfully!')
      setTimeout(() => setSuccess(''), 3000)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save settings')
      setTimeout(() => setError(''), 5000)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="bg-gradient-to-br from-slate-50 to-blue-50 px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[900px]">
        <header className="mb-6">
          <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
            Admin Settings
          </h1>
          <p className="mt-1 text-sm text-[#647590]">Manage application-wide settings (Admin Only)</p>
        </header>

        {success && (
          <div className="mb-5 flex items-center gap-2 rounded-lg border border-green-200 bg-green-100 px-4 py-3 text-sm font-medium text-green-900">
            <CheckCircle2 size={18} />
            {success}
          </div>
        )}

        {error && (
          <div className="mb-5 flex items-center gap-2 rounded-lg border border-red-200 bg-red-100 px-4 py-3 text-sm font-medium text-red-900">
            <AlertCircle size={18} />
            {error}
          </div>
        )}

        {/* Warning Banner */}
        <div className="mb-6 rounded-xl border-2 border-orange-200 bg-gradient-to-r from-orange-50 to-yellow-50 p-4">
          <div className="flex items-start gap-3">
            <Shield className="mt-0.5 text-orange-600" size={22} />
            <div>
              <p className="font-bold text-orange-900">Admin Only Access</p>
              <p className="mt-1 text-sm text-orange-800">
                These settings affect the entire application. Only administrators can view and modify these settings.
              </p>
            </div>
          </div>
        </div>

        <form onSubmit={handleSave}>
          {/* General Settings */}
          <SettingsSection
            icon={Globe}
            title="General Settings"
            description="Application name and contact information"
          >
            <SettingField
              label="Site Name"
              value={settings.siteName}
              onChange={(siteName) => setSettings({ ...settings, siteName })}
            />
            <SettingField
              label="Support Email"
              type="email"
              value={settings.supportEmail}
              onChange={(supportEmail) => setSettings({ ...settings, supportEmail })}
            />
            <SettingField
              label="Support Phone"
              type="tel"
              value={settings.supportPhone}
              onChange={(supportPhone) => setSettings({ ...settings, supportPhone })}
            />
          </SettingsSection>

          {/* User Management */}
          <SettingsSection
            icon={Users}
            title="User & Driver Management"
            description="Configure user registration and driver approval policies"
          >
            <SettingToggle
              label="Require Driver Approval"
              description="New drivers must be manually approved by admin before they can create rides"
              checked={settings.requireDriverApproval}
              onChange={(requireDriverApproval) =>
                setSettings({ ...settings, requireDriverApproval })
              }
            />
            <SettingToggle
              label="Auto-Approve Drivers"
              description="Automatically approve all driver requests (not recommended)"
              checked={settings.autoApproveDrivers}
              onChange={(autoApproveDrivers) => setSettings({ ...settings, autoApproveDrivers })}
            />
            <SettingToggle
              label="Allow User Registration"
              description="Enable new users to register on the platform"
              checked={settings.allowRegistration}
              onChange={(allowRegistration) => setSettings({ ...settings, allowRegistration })}
            />
            <SettingField
              label="Minimum Driver Rating"
              type="number"
              step="0.1"
              min="1"
              max="5"
              value={settings.minDriverRating}
              onChange={(minDriverRating) =>
                setSettings({ ...settings, minDriverRating: Number(minDriverRating) })
              }
            />
          </SettingsSection>

          {/* Ride Settings */}
          <SettingsSection
            icon={Database}
            title="Ride Configuration"
            description="Configure ride creation limits and passenger capacity"
          >
            <SettingField
              label="Max Rides Per Driver Per Day"
              type="number"
              min="1"
              max="50"
              value={settings.maxRidesPerDay}
              onChange={(maxRidesPerDay) =>
                setSettings({ ...settings, maxRidesPerDay: Number(maxRidesPerDay) })
              }
            />
            <SettingField
              label="Max Passengers Per Ride"
              type="number"
              min="1"
              max="8"
              value={settings.maxPassengersPerRide}
              onChange={(maxPassengersPerRide) =>
                setSettings({ ...settings, maxPassengersPerRide: Number(maxPassengersPerRide) })
              }
            />
          </SettingsSection>

          {/* Notifications */}
          <SettingsSection
            icon={Bell}
            title="Notification Settings"
            description="Configure system-wide notification preferences"
          >
            <SettingToggle
              label="Enable Email Notifications"
              description="Send email notifications to users for bookings, approvals, etc."
              checked={settings.enableEmailNotifications}
              onChange={(enableEmailNotifications) =>
                setSettings({ ...settings, enableEmailNotifications })
              }
            />
            <SettingToggle
              label="Enable SMS Notifications"
              description="Send SMS notifications for critical updates (requires SMS gateway)"
              checked={settings.enableSmsNotifications}
              onChange={(enableSmsNotifications) =>
                setSettings({ ...settings, enableSmsNotifications })
              }
            />
          </SettingsSection>

          {/* Security */}
          <SettingsSection
            icon={Shield}
            title="Security & Authentication"
            description="Security policies and authentication settings"
          >
            <div className="space-y-3 rounded-lg bg-gradient-to-r from-blue-50 to-indigo-50 p-5">
              <div className="flex items-start gap-3">
                <Lock className="mt-0.5 text-blue-600" size={20} />
                <div>
                  <p className="font-semibold text-blue-900">Password Policy</p>
                  <p className="mt-1 text-sm text-blue-700">
                    Passwords must be at least 8 characters with uppercase, lowercase, and numbers.
                  </p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <Shield className="mt-0.5 text-blue-600" size={20} />
                <div>
                  <p className="font-semibold text-blue-900">Session Timeout</p>
                  <p className="mt-1 text-sm text-blue-700">
                    User sessions expire after 24 hours of inactivity for security.
                  </p>
                </div>
              </div>
            </div>
          </SettingsSection>

          {/* Save Button */}
          <div className="flex justify-end gap-3">
            <button
              className="rounded-lg border-2 border-slate-300 bg-white px-6 py-3 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
              type="button"
              onClick={fetchSettings}
            >
              Reset
            </button>
            <button
              className="inline-flex items-center gap-2 rounded-lg bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-3 text-sm font-bold text-white shadow-lg transition hover:from-blue-700 hover:to-blue-800 disabled:opacity-50"
              type="submit"
              disabled={loading}
            >
              <Save size={16} />
              {loading ? 'Saving...' : 'Save Settings'}
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}

function SettingsSection({ icon: Icon, title, description, children }) {
  return (
    <article className="mb-6 overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white shadow-sm">
      <div className="border-b border-[#edf1f8] bg-gradient-to-r from-[#f8fbff] to-white px-6 py-4">
        <div className="flex items-center gap-3">
          <div className="rounded-lg bg-[#2563eb] p-2 text-white">
            <Icon size={20} />
          </div>
          <div>
            <h2 className="text-lg font-bold text-[#12214a]">{title}</h2>
            <p className="text-sm text-[#647590]">{description}</p>
          </div>
        </div>
      </div>
      <div className="space-y-5 p-6">{children}</div>
    </article>
  )
}

function SettingField({ label, type = 'text', value, onChange, ...props }) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-bold text-[#304260]">{label}</span>
      <input
        className="w-full rounded-lg border-2 border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        {...props}
      />
    </label>
  )
}

function SettingToggle({ label, description, checked, onChange }) {
  return (
    <div className="flex items-center justify-between">
      <div className="flex-1">
        <p className="font-semibold text-[#12214a]">{label}</p>
        <p className="mt-1 text-sm text-[#647590]">{description}</p>
      </div>
      <button
        className={`relative h-6 w-11 rounded-full transition ${
          checked ? 'bg-[#2563eb]' : 'bg-gray-300'
        }`}
        type="button"
        onClick={() => onChange(!checked)}
      >
        <span
          className={`absolute top-0.5 h-5 w-5 rounded-full bg-white shadow-md transition ${
            checked ? 'left-5' : 'left-0.5'
          }`}
        />
      </button>
    </div>
  )
}

export default AdminSettings
