import { AlertTriangle, X } from 'lucide-react'

function ConfirmDialog({ isOpen, onClose, onConfirm, title, message, confirmText = 'Confirm', cancelText = 'Cancel', variant = 'primary' }) {
  if (!isOpen) return null

  const variantStyles = {
    primary: 'bg-[#2563eb] hover:bg-[#164bc7] text-white',
    danger: 'bg-red-600 hover:bg-red-700 text-white',
    warning: 'bg-orange-600 hover:bg-orange-700 text-white',
  }

  return (
    <>
      {/* Backdrop */}
      <div 
        className="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm"
        onClick={onClose}
      />
      
      {/* Dialog */}
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div 
          className="relative w-full max-w-md rounded-2xl border border-slate-200 bg-white shadow-2xl"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Close button */}
          <button
            className="absolute right-4 top-4 rounded-lg p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
            onClick={onClose}
            type="button"
          >
            <X size={20} />
          </button>

          <div className="p-6">
            {/* Icon */}
            {variant === 'warning' && (
              <div className="mb-4 flex size-12 items-center justify-center rounded-full bg-orange-100">
                <AlertTriangle className="text-orange-600" size={24} />
              </div>
            )}

            {/* Title */}
            <h2 className="mb-2 text-xl font-bold text-[#12214a]">
              {title}
            </h2>

            {/* Message */}
            <p className="mb-6 text-sm leading-relaxed text-[#647590]">
              {message}
            </p>

            {/* Actions */}
            <div className="flex gap-3">
              <button
                className="flex-1 rounded-lg border-2 border-slate-300 bg-white px-4 py-2.5 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
                onClick={onClose}
                type="button"
              >
                {cancelText}
              </button>
              <button
                className={`flex-1 rounded-lg px-4 py-2.5 text-sm font-bold shadow-lg transition ${variantStyles[variant]}`}
                onClick={() => {
                  onConfirm()
                  onClose()
                }}
                type="button"
              >
                {confirmText}
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default ConfirmDialog
