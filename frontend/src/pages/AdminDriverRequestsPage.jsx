import { useEffect, useState } from 'react'
import { Check, LoaderCircle, X } from 'lucide-react'
import { userApi } from '../api/client'

function AdminDriverRequestsPage() {
  const [requests, setRequests] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [actionId, setActionId] = useState(null)
  const [manageId, setManageId] = useState(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    let isActive = true

    async function loadRequests() {
      setIsLoading(true)
      setError('')
      try {
        const { data } = await userApi.get('/api/admin/driver-requests')
        if (isActive) setRequests(data)
      } catch (requestError) {
        if (isActive) setError(requestError.response?.data?.message || 'Driver requests could not be loaded.')
      } finally {
        if (isActive) setIsLoading(false)
      }
    }

    loadRequests()
    return () => { isActive = false }
  }, [])

  const reviewRequest = async (requestId, action) => {
    setActionId(requestId)
    setError('')
    setSuccess('')
    try {
      const { data } = await userApi.post(`/api/admin/driver-requests/${requestId}/${action}`)
      setRequests((currentRequests) => currentRequests.map((request) => request.id === requestId ? data : request))
      setSuccess(`Driver request ${action === 'approve' ? 'approved' : 'rejected'} successfully.`)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'The driver request could not be reviewed.')
    } finally {
      setActionId(null)
    }
  }

  const revokeRequest = async (requestId) => {
    setActionId(requestId)
    setError('')
    setSuccess('')
    try {
      const { data } = await userApi.post(`/api/admin/driver-requests/${requestId}/revoke`)
      setRequests((currentRequests) => currentRequests.map((request) => request.id === requestId ? data : request))
      setManageId(null)
      setSuccess('Driver access revoked successfully.')
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Driver access could not be revoked.')
    } finally {
      setActionId(null)
    }
  }

  const pendingRequests = requests.filter((request) => request.status === 'PENDING')
  const historyRequests = requests.filter((request) => request.status !== 'PENDING')

  return (
    <section className="dashboard-page">
      <p className="section-kicker">Admin panel</p>
      <div className="dashboard-welcome"><div><h1>Driver Requests</h1><p>Review students who want to use Driver mode.</p></div></div>
      {error && <p className="form-error">{error}</p>}
      {success && <p className="form-success">{success}</p>}
      {isLoading ? <RequestState message="Loading driver requests..." spinning /> : <>
        <RequestSection title="Pending Requests" emptyMessage="There are no pending driver requests." requests={pendingRequests}>
          {pendingRequests.map((request) => <PendingRequestCard actionId={actionId} key={request.id} request={request} onReview={reviewRequest} />)}
        </RequestSection>
        <RequestSection title="Request History" emptyMessage="No approved or rejected requests yet." requests={historyRequests}>
          {historyRequests.map((request) => <HistoryRequestCard actionId={actionId} key={request.id} manageId={manageId} onCancelManage={() => setManageId(null)} onManage={setManageId} onRevoke={revokeRequest} request={request} />)}
        </RequestSection>
      </>}
    </section>
  )
}

function RequestSection({ children, emptyMessage, requests, title }) {
  return <section className="mt-7"><h2 className="text-xl font-bold tracking-[-0.02em] text-[#14244c]">{title}</h2>{requests.length ? <div className="mt-4 grid gap-4">{children}</div> : <RequestState message={emptyMessage} />}</section>
}

function PendingRequestCard({ actionId, request, onReview }) {
  const isBusy = actionId === request.id
  return <article className="rounded-2xl border border-[#e0e9f7] bg-white p-5 shadow-[0_10px_28px_rgba(31,61,111,0.06)] sm:p-6"><RequestUserInfo request={request} /><div className="mt-4 flex items-center justify-between gap-3"><StatusBadge status={request.status} /><span className="text-sm text-[#53637f]">Requested {formatDate(request.requestedAt)}</span></div><div className="mt-5 flex flex-col gap-3 sm:flex-row sm:justify-end"><button className="inline-flex items-center justify-center gap-2 rounded-lg border border-red-200 bg-white px-4 py-2.5 text-sm font-bold text-red-700 disabled:opacity-60" disabled={isBusy} onClick={() => onReview(request.id, 'reject')} type="button"><X size={16} />Reject</button><button className="inline-flex items-center justify-center gap-2 rounded-lg bg-[#2563eb] px-4 py-2.5 text-sm font-bold text-white disabled:opacity-60" disabled={isBusy} onClick={() => onReview(request.id, 'approve')} type="button">{isBusy ? <LoaderCircle className="animate-spin" size={16} /> : <Check size={16} />}Approve</button></div></article>
}

function HistoryRequestCard({ actionId, manageId, onCancelManage, onManage, onRevoke, request }) {
  const isApproved = request.status === 'APPROVED'
  const isManaged = manageId === request.id
  const isBusy = actionId === request.id

  return <article className="rounded-2xl border border-[#e0e9f7] bg-white p-5 shadow-[0_10px_28px_rgba(31,61,111,0.06)] sm:p-6"><div className="flex flex-wrap items-start justify-between gap-4"><RequestUserInfo request={request} /><StatusBadge status={request.status} /></div><dl className="mt-5 grid gap-4 text-sm sm:grid-cols-3"><HistoryDetail label="Requested" value={formatDate(request.requestedAt)} /><HistoryDetail label="Reviewed" value={formatDate(request.reviewedAt)} /><HistoryDetail label="Reviewed by" value={request.reviewedBy ?? 'Not available'} /></dl>{isApproved && !isManaged && <div className="mt-5 flex justify-end"><button className="rounded-lg border border-[#cddbef] bg-white px-4 py-2.5 text-sm font-bold text-[#304260]" onClick={() => onManage(request.id)} type="button">Manage</button></div>}{isApproved && isManaged && <div className="mt-5 rounded-xl border border-amber-200 bg-amber-50 p-4"><p className="m-0 text-sm font-medium text-amber-900">Revoke Driver Access? This removes Driver eligibility while keeping this request in history.</p><div className="mt-4 flex flex-col gap-3 sm:flex-row sm:justify-end"><button className="rounded-lg border border-[#cddbef] bg-white px-4 py-2.5 text-sm font-bold text-[#304260]" disabled={isBusy} onClick={onCancelManage} type="button">Cancel</button><button className="inline-flex items-center justify-center gap-2 rounded-lg bg-red-600 px-4 py-2.5 text-sm font-bold text-white disabled:opacity-60" disabled={isBusy} onClick={() => onRevoke(request.id)} type="button">{isBusy && <LoaderCircle className="animate-spin" size={16} />}Revoke Driver Access</button></div></div>}</article>
}

function RequestUserInfo({ request }) {
  return <div><h3 className="m-0 text-lg font-bold text-[#14244c]">{request.firstName} {request.lastName}</h3><p className="mt-1 text-sm text-[#647590]">{request.email} · {request.phoneNumber}</p></div>
}

function HistoryDetail({ label, value }) {
  return <div><dt className="text-xs font-bold uppercase tracking-[0.04em] text-[#72829d]">{label}</dt><dd className="mt-1 font-semibold text-[#304260]">{value}</dd></div>
}

function StatusBadge({ status }) {
  const className = status === 'APPROVED'
    ? 'bg-green-50 text-green-700'
    : status === 'REJECTED' ? 'bg-red-50 text-red-700'
      : status === 'REVOKED' ? 'bg-slate-100 text-slate-700'
        : 'bg-[#fff7e8] text-[#b66b08]'
  return <span className={`rounded-full px-3 py-1 text-xs font-bold uppercase tracking-[0.04em] ${className}`}>{status}</span>
}

function RequestState({ message, spinning = false }) {
  return <div className="mt-4 rounded-2xl border border-dashed border-[#cddbef] bg-white p-8 text-center text-sm font-medium text-[#647590]">{spinning && <LoaderCircle className="mx-auto mb-3 animate-spin text-[#2563eb]" size={24} />}{message}</div>
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : 'Not available'
}

export default AdminDriverRequestsPage
