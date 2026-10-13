import { useEffect, useState } from 'react'
import {
  AlertCircle,
  CheckCircle2,
  Clock,
  Filter,
  LoaderCircle,
  Mail,
  Phone,
  User,
  XCircle,
} from 'lucide-react'
import { userApi } from '../api/client'

function AdminDriverRequests() {
  const [requests, setRequests] = useState([])
  const [filteredRequests, setFilteredRequests] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState('ALL')
  const [processingId, setProcessingId] = useState(null)

  useEffect(() => {
    loadRequests()
  }, [])

  useEffect(() => {
    filterRequests()
  }, [filter, requests])

  const loadRequests = async () => {
    setIsLoading(true)
    setError('')
    try {
      const { data } = await userApi.get('/api/admin/driver-requests')
      setRequests(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load driver requests.')
    } finally {
      setIsLoading(false)
    }
  }

  const filterRequests = () => {
    if (filter === 'ALL') {
      setFilteredRequests(requests)
    } else {
      setFilteredRequests(requests.filter(req => req.status === filter))
    }
  }

  const handleApprove = async (requestId) => {
    if (!confirm('Are you sure you want to approve this driver request?')) return

    setProcessingId(requestId)
    setError('')
    try {
      await userApi.post(`/api/admin/driver-requests/${requestId}/approve`)
      await loadRequests()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to approve request.')
    } finally {
      setProcessingId(null)
    }
  }

  const handleReject = async (requestId) => {
    if (!confirm('Are you sure you want to reject this driver request?')) return

    setProcessingId(requestId)
    setError('')
    try {
      await userApi.post(`/api/admin/driver-requests/${requestId}/reject`)
      await loadRequests()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reject request.')
    } finally {
      setProcessingId(null)
    }
  }

  if (isLoading) {
    return (
      <section className="grid min-h-[400px] place-items-center">
        <div className="flex flex-col items-center gap-3">
          <LoaderCircle className="animate-spin text-[#2563eb]" size={32} />
          <p className="text-sm font-medium text-[#53637f]">Loading requests...</p>
        </div>
      </section>
    )
  }

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[1200px]">
        <header className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
              Driver Requests
            </h1>
            <p className="mt-1 text-sm text-[#647590]">
              {filteredRequests.length} {filter === 'ALL' ? 'total' : filter.toLowerCase()} request
              {filteredRequests.length !== 1 ? 's' : ''}
            </p>
          </div>

          <div className="flex items-center gap-2">
            <Filter size={16} className="text-[#72829d]" />
            <select
              className="rounded-lg border border-[#e0e9f7] bg-white px-4 py-2 text-sm font-semibold text-[#12214a] shadow-sm transition hover:border-[#2563eb] focus:border-[#2563eb] focus:outline-none"
              onChange={(e) => setFilter(e.target.value)}
              value={filter}
            >
              <option value="ALL">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
        </header>

        {error && (
          <div className="mb-5 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        )}

        {filteredRequests.length === 0 ? (
          <div className="flex flex-col items-center rounded-2xl border border-[#e0e9f7] bg-white px-8 py-10 text-center">
            <AlertCircle className="text-[#72829d]" size={40} />
            <p className="mt-3 max-w-md text-sm font-medium text-[#53637f]">
              No {filter === 'ALL' ? '' : filter.toLowerCase()} driver requests found.
            </p>
          </div>
        ) : (
          <div className="grid gap-4">
            {filteredRequests.map((request) => (
              <RequestCard
                key={request.id}
                request={request}
                onApprove={handleApprove}
                onReject={handleReject}
                isProcessing={processingId === request.id}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  )
}

function RequestCard({ request, onApprove, onReject, isProcessing }) {
  const statusColors = {
    PENDING: 'bg-yellow-100 text-yellow-700 border-yellow-200',
    APPROVED: 'bg-green-100 text-green-700 border-green-200',
    REJECTED: 'bg-red-100 text-red-700 border-red-200',
  }

  const statusIcons = {
    PENDING: Clock,
    APPROVED: CheckCircle2,
    REJECTED: XCircle,
  }

  const StatusIcon = statusIcons[request.status] || AlertCircle

  const requestDate = new Date(request.requestedAt).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })

  const requestTime = new Date(request.requestedAt).toLocaleTimeString('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
  })

  return (
    <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white shadow-sm">
      <div className="p-5 sm:p-6">
        <div className="mb-4 flex flex-wrap items-start justify-between gap-3">
          <div className="flex items-start gap-3">
            <div className="rounded-full bg-[#f0f5ff] p-3 text-[#2563eb]">
              <User size={24} />
            </div>
            <div>
              <h3 className="text-lg font-bold text-[#12214a]">
                {request.firstName} {request.lastName}
              </h3>
              <p className="mt-0.5 text-xs text-[#72829d]">
                Requested on {requestDate} at {requestTime}
              </p>
            </div>
          </div>
          <span
            className={`flex items-center gap-1.5 rounded-lg border px-3 py-1.5 text-xs font-bold ${
              statusColors[request.status] || 'bg-gray-100 text-gray-700 border-gray-200'
            }`}
          >
            <StatusIcon size={14} />
            {request.status}
          </span>
        </div>

        <div className="mb-5 grid gap-3 sm:grid-cols-2">
          <InfoItem icon={Mail} label="Email" value={request.email} />
          <InfoItem icon={Phone} label="Phone" value={request.phoneNumber} />
        </div>

        {request.status === 'PENDING' && (
          <div className="flex flex-wrap gap-3 border-t border-[#edf1f8] pt-5">
            <button
              className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-green-600 px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-green-700 disabled:opacity-60 sm:flex-none"
              disabled={isProcessing}
              onClick={() => onApprove(request.id)}
              type="button"
            >
              <CheckCircle2 size={16} />
              {isProcessing ? 'Processing...' : 'Approve'}
            </button>
            <button
              className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg border border-red-200 bg-white px-5 py-2.5 text-sm font-bold text-red-600 shadow-sm transition hover:bg-red-50 disabled:opacity-60 sm:flex-none"
              disabled={isProcessing}
              onClick={() => onReject(request.id)}
              type="button"
            >
              <XCircle size={16} />
              {isProcessing ? 'Processing...' : 'Reject'}
            </button>
          </div>
        )}

        {request.reviewedAt && (
          <div className="mt-4 border-t border-[#edf1f8] pt-4">
            <p className="text-xs text-[#72829d]">
              Reviewed on{' '}
              {new Date(request.reviewedAt).toLocaleDateString('en-IN', {
                day: 'numeric',
                month: 'short',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </p>
          </div>
        )}
      </div>
    </article>
  )
}

function InfoItem({ icon: Icon, label, value }) {
  return (
    <div className="flex items-center gap-2">
      <Icon className="text-[#72829d]" size={16} />
      <div>
        <p className="text-xs font-semibold text-[#72829d]">{label}</p>
        <p className="text-sm font-bold text-[#182950]">{value}</p>
      </div>
    </div>
  )
}

export default AdminDriverRequests
