import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { CheckCircle2, FileText, LoaderCircle, Users, XCircle } from 'lucide-react'
import { userApi } from '../api/client'

function AdminDashboard() {
  const navigate = useNavigate()
  const [stats, setStats] = useState({
    totalRequests: 0,
    pendingRequests: 0,
    approvedRequests: 0,
    rejectedRequests: 0,
  })
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    setIsLoading(true)
    try {
      const { data } = await userApi.get('/api/admin/driver-requests')
      setStats({
        totalRequests: data.length,
        pendingRequests: data.filter(r => r.status === 'PENDING').length,
        approvedRequests: data.filter(r => r.status === 'APPROVED').length,
        rejectedRequests: data.filter(r => r.status === 'REJECTED').length,
      })
    } catch (err) {
      console.error('Failed to load stats:', err)
    } finally {
      setIsLoading(false)
    }
  }

  if (isLoading) {
    return (
      <section className="grid min-h-[400px] place-items-center">
        <div className="flex flex-col items-center gap-3">
          <LoaderCircle className="animate-spin text-[#2563eb]" size={32} />
          <p className="text-sm font-medium text-[#53637f]">Loading dashboard...</p>
        </div>
      </section>
    )
  }

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[1200px]">
        <header className="mb-6">
          <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
            Admin Dashboard
          </h1>
          <p className="mt-1 text-sm text-[#647590]">Manage driver requests and users</p>
        </header>

        <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard
            icon={FileText}
            label="Total Requests"
            value={stats.totalRequests}
            color="bg-blue-500"
          />
          <StatCard
            icon={LoaderCircle}
            label="Pending"
            value={stats.pendingRequests}
            color="bg-yellow-500"
          />
          <StatCard
            icon={CheckCircle2}
            label="Approved"
            value={stats.approvedRequests}
            color="bg-green-500"
          />
          <StatCard
            icon={XCircle}
            label="Rejected"
            value={stats.rejectedRequests}
            color="bg-red-500"
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <ActionCard
            icon={FileText}
            title="Driver Requests"
            description="Review and approve driver applications"
            buttonText="View Requests"
            onClick={() => navigate('/admin/driver-requests')}
            badge={stats.pendingRequests > 0 ? `${stats.pendingRequests} pending` : null}
          />
          <ActionCard
            icon={Users}
            title="User Management"
            description="View and manage all platform users"
            buttonText="View Users"
            onClick={() => navigate('/admin/users')}
          />
        </div>
      </div>
    </section>
  )
}

function StatCard({ icon: Icon, label, value, color }) {
  return (
    <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white p-5 shadow-sm">
      <div className="flex items-center gap-3">
        <div className={`rounded-lg ${color} p-3 text-white`}>
          <Icon size={24} />
        </div>
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.035em] text-[#72829d]">
            {label}
          </p>
          <p className="mt-1 text-2xl font-bold text-[#12214a]">{value}</p>
        </div>
      </div>
    </article>
  )
}

function ActionCard({ icon: Icon, title, description, buttonText, onClick, badge }) {
  return (
    <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white p-6 shadow-sm">
      <div className="mb-4 flex items-start justify-between">
        <div className="rounded-lg bg-[#f0f5ff] p-3 text-[#2563eb]">
          <Icon size={28} />
        </div>
        {badge && (
          <span className="rounded-full bg-yellow-100 px-3 py-1 text-xs font-bold text-yellow-700">
            {badge}
          </span>
        )}
      </div>
      <h3 className="mb-2 text-lg font-bold text-[#12214a]">{title}</h3>
      <p className="mb-4 text-sm text-[#647590]">{description}</p>
      <button
        className="w-full rounded-lg bg-[#2563eb] px-4 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-[#164bc7]"
        onClick={onClick}
        type="button"
      >
        {buttonText}
      </button>
    </article>
  )
}

export default AdminDashboard
