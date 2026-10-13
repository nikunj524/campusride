import { useEffect, useState } from 'react'
import {
  AlertCircle,
  Filter,
  LoaderCircle,
  Mail,
  Phone,
  Search,
  ShieldCheck,
  User,
} from 'lucide-react'
import { userApi } from '../api/client'

function AdminUsers() {
  const [users, setUsers] = useState([])
  const [filteredUsers, setFilteredUsers] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [roleFilter, setRoleFilter] = useState('ALL')

  useEffect(() => {
    loadUsers()
  }, [])

  useEffect(() => {
    filterUsers()
  }, [roleFilter, searchTerm, users])

  const loadUsers = async () => {
    setIsLoading(true)
    setError('')
    try {
      const { data } = await userApi.get('/api/users')
      setUsers(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load users.')
    } finally {
      setIsLoading(false)
    }
  }

  const filterUsers = () => {
    let filtered = users

    // Filter by role
    if (roleFilter !== 'ALL') {
      filtered = filtered.filter(user => user.role === roleFilter)
    }

    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase()
      filtered = filtered.filter(user =>
        user.firstName.toLowerCase().includes(term) ||
        user.lastName.toLowerCase().includes(term) ||
        user.email.toLowerCase().includes(term) ||
        user.phoneNumber?.toLowerCase().includes(term)
      )
    }

    setFilteredUsers(filtered)
  }

  const getRoleCounts = () => {
    return {
      total: users.length,
      students: users.filter(u => u.role === 'STUDENT').length,
      drivers: users.filter(u => u.role === 'DRIVER').length,
      admins: users.filter(u => u.role === 'ADMIN').length,
    }
  }

  if (isLoading) {
    return (
      <section className="grid min-h-[400px] place-items-center">
        <div className="flex flex-col items-center gap-3">
          <LoaderCircle className="animate-spin text-[#2563eb]" size={32} />
          <p className="text-sm font-medium text-[#53637f]">Loading users...</p>
        </div>
      </section>
    )
  }

  const counts = getRoleCounts()

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[1200px]">
        <header className="mb-6">
          <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
            User Management
          </h1>
          <p className="mt-1 text-sm text-[#647590]">
            {filteredUsers.length} {roleFilter === 'ALL' ? 'total' : roleFilter.toLowerCase()} user
            {filteredUsers.length !== 1 ? 's' : ''}
          </p>
        </header>

        {/* Stats Cards */}
        <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard label="Total Users" value={counts.total} color="bg-blue-500" />
          <StatCard label="Students" value={counts.students} color="bg-green-500" />
          <StatCard label="Drivers" value={counts.drivers} color="bg-purple-500" />
          <StatCard label="Admins" value={counts.admins} color="bg-orange-500" />
        </div>

        {/* Filters */}
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-[#72829d]" size={18} />
            <input
              className="w-full rounded-lg border border-[#e0e9f7] bg-white py-2.5 pl-10 pr-4 text-sm font-medium text-[#12214a] shadow-sm transition focus:border-[#2563eb] focus:outline-none focus:ring-2 focus:ring-[#e5efff]"
              placeholder="Search by name, email, or phone..."
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="flex items-center gap-2">
            <Filter size={16} className="text-[#72829d]" />
            <select
              className="rounded-lg border border-[#e0e9f7] bg-white px-4 py-2.5 text-sm font-semibold text-[#12214a] shadow-sm transition hover:border-[#2563eb] focus:border-[#2563eb] focus:outline-none"
              onChange={(e) => setRoleFilter(e.target.value)}
              value={roleFilter}
            >
              <option value="ALL">All Roles</option>
              <option value="STUDENT">Students</option>
              <option value="DRIVER">Drivers</option>
              <option value="ADMIN">Admins</option>
            </select>
          </div>
        </div>

        {error && (
          <div className="mb-5 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        )}

        {/* Users List */}
        {filteredUsers.length === 0 ? (
          <div className="flex flex-col items-center rounded-2xl border border-[#e0e9f7] bg-white px-8 py-10 text-center">
            <AlertCircle className="text-[#72829d]" size={40} />
            <p className="mt-3 max-w-md text-sm font-medium text-[#53637f]">
              No users found matching your criteria.
            </p>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {filteredUsers.map((user) => (
              <UserCard key={user.id} user={user} />
            ))}
          </div>
        )}
      </div>
    </section>
  )
}

function StatCard({ label, value, color }) {
  return (
    <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white p-5 shadow-sm">
      <div className="flex items-center gap-3">
        <div className={`rounded-lg ${color} p-3 text-white`}>
          <User size={24} />
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

function UserCard({ user }) {
  const roleColors = {
    STUDENT: 'bg-green-100 text-green-700 border-green-200',
    DRIVER: 'bg-purple-100 text-purple-700 border-purple-200',
    ADMIN: 'bg-orange-100 text-orange-700 border-orange-200',
  }

  const memberSince = new Date(user.createdAt).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })

  return (
    <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white shadow-sm transition hover:shadow-md">
      <div className="p-5">
        <div className="mb-4 flex items-start justify-between">
          <div className="flex items-start gap-3">
            <div className="flex size-12 items-center justify-center rounded-full bg-gradient-to-br from-[#dceaff] to-[#aac9fb] text-lg font-bold text-[#174ba6]">
              {getInitials(user)}
            </div>
            <div>
              <h3 className="text-base font-bold text-[#12214a]">
                {user.firstName} {user.lastName}
              </h3>
              <p className="mt-0.5 text-xs text-[#72829d]">ID: {user.id}</p>
            </div>
          </div>
          <span
            className={`rounded-lg border px-2.5 py-1 text-xs font-bold ${
              roleColors[user.role] || 'bg-gray-100 text-gray-700 border-gray-200'
            }`}
          >
            {user.role}
          </span>
        </div>

        <div className="space-y-2.5">
          <InfoItem icon={Mail} value={user.email} />
          <InfoItem icon={Phone} value={user.phoneNumber} />
          {user.role === 'DRIVER' && user.driverEligible && (
            <InfoItem icon={ShieldCheck} value="Driver Eligible" />
          )}
        </div>

        <div className="mt-4 border-t border-[#edf1f8] pt-4">
          <p className="text-xs text-[#72829d]">Member since {memberSince}</p>
        </div>
      </div>
    </article>
  )
}

function InfoItem({ icon: Icon, value }) {
  return (
    <div className="flex items-center gap-2">
      <Icon className="text-[#72829d]" size={14} />
      <p className="text-sm text-[#182950]">{value}</p>
    </div>
  )
}

function getInitials(user) {
  return `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase() || 'U'
}

export default AdminUsers
