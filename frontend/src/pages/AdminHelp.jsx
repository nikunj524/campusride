import {
  Book,
  CheckCircle2,
  FileQuestion,
  Mail,
  MessageSquare,
  Phone,
  Video,
} from 'lucide-react'

function AdminHelp() {
  const faqs = [
    {
      question: 'How do I approve a driver request?',
      answer:
        'Go to "Driver Requests" from the sidebar, find the pending request, and click the green "Approve" button. The user will be able to switch to driver mode immediately.',
    },
    {
      question: 'Can I reject a driver request?',
      answer:
        'Yes, click the red "Reject" button on any pending request. The user will see a rejection message and can submit a new request later.',
    },
    {
      question: 'How do I view all users?',
      answer:
        'Click on "Users" in the sidebar to see a complete list of all registered users. You can filter by role and search by name, email, or phone.',
    },
    {
      question: 'What are the user roles?',
      answer:
        'There are three roles: STUDENT (can book rides), DRIVER (can create and offer rides), and ADMIN (can manage users and approve drivers).',
    },
    {
      question: 'How does the driver approval process work?',
      answer:
        'When a student requests driver access, a request is created. Admin reviews and approves/rejects. Once approved, the user can register a vehicle and create rides.',
    },
  ]

  const resources = [
    {
      icon: Book,
      title: 'Documentation',
      description: 'Complete guide to using the admin panel',
      link: '#',
    },
    {
      icon: Video,
      title: 'Video Tutorials',
      description: 'Watch step-by-step video guides',
      link: '#',
    },
    {
      icon: FileQuestion,
      title: 'Knowledge Base',
      description: 'Search our comprehensive knowledge base',
      link: '#',
    },
  ]

  return (
    <section className="bg-gradient-to-br from-slate-50 to-blue-50 px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[900px]">
        <header className="mb-6">
          <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
            Help & Support
          </h1>
          <p className="mt-1 text-sm text-[#647590]">
            Get help with managing your CampusRide admin panel
          </p>
        </header>

        {/* Contact Support */}
        <article className="mb-6 overflow-hidden rounded-2xl border border-[#e0e9f7] bg-gradient-to-br from-blue-600 to-blue-700 p-6 text-white shadow-lg">
          <h2 className="mb-2 text-xl font-bold">Need Immediate Help?</h2>
          <p className="mb-4 text-blue-100">
            Our support team is available 24/7 to assist you
          </p>
          <div className="flex flex-wrap gap-3">
            <a
              className="inline-flex items-center gap-2 rounded-lg bg-white px-4 py-2.5 text-sm font-bold text-blue-600 transition hover:bg-blue-50"
              href="mailto:admin@campusride.com"
            >
              <Mail size={16} />
              Email Support
            </a>
            <a
              className="inline-flex items-center gap-2 rounded-lg border-2 border-white px-4 py-2.5 text-sm font-bold text-white transition hover:bg-white/10"
              href="tel:9999999999"
            >
              <Phone size={16} />
              Call Us
            </a>
            <button
              className="inline-flex items-center gap-2 rounded-lg border-2 border-white px-4 py-2.5 text-sm font-bold text-white transition hover:bg-white/10"
              type="button"
            >
              <MessageSquare size={16} />
              Live Chat
            </button>
          </div>
        </article>

        {/* Resources */}
        <div className="mb-6 grid gap-4 sm:grid-cols-3">
          {resources.map((resource) => (
            <a
              key={resource.title}
              className="block overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white p-5 shadow-sm transition hover:shadow-md"
              href={resource.link}
            >
              <div className="mb-3 inline-flex rounded-lg bg-[#f0f5ff] p-3 text-[#2563eb]">
                <resource.icon size={24} />
              </div>
              <h3 className="mb-2 font-bold text-[#12214a]">{resource.title}</h3>
              <p className="text-sm text-[#647590]">{resource.description}</p>
            </a>
          ))}
        </div>

        {/* FAQ */}
        <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white shadow-sm">
          <div className="border-b border-[#edf1f8] bg-gradient-to-r from-[#f8fbff] to-white px-6 py-4">
            <h2 className="text-lg font-bold text-[#12214a]">Frequently Asked Questions</h2>
            <p className="text-sm text-[#647590]">Common questions about admin features</p>
          </div>
          <div className="divide-y divide-[#edf1f8]">
            {faqs.map((faq, index) => (
              <div key={index} className="p-6">
                <div className="mb-3 flex items-start gap-3">
                  <CheckCircle2 className="mt-0.5 text-green-500" size={20} />
                  <h3 className="font-bold text-[#12214a]">{faq.question}</h3>
                </div>
                <p className="pl-8 text-sm text-[#647590]">{faq.answer}</p>
              </div>
            ))}
          </div>
        </article>

        {/* Quick Tips */}
        <article className="mt-6 rounded-2xl border border-blue-100 bg-blue-50 p-6">
          <h3 className="mb-4 font-bold text-blue-900">💡 Quick Tips</h3>
          <ul className="space-y-2 text-sm text-blue-800">
            <li className="flex items-start gap-2">
              <span className="text-blue-600">•</span>
              <span>Use the search bar in "Users" to quickly find specific users</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-blue-600">•</span>
              <span>Filter driver requests by status to focus on pending approvals</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-blue-600">•</span>
              <span>Check the dashboard daily for new driver request notifications</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-blue-600">•</span>
              <span>Review user profiles before approving driver requests</span>
            </li>
          </ul>
        </article>
      </div>
    </section>
  )
}

export default AdminHelp
