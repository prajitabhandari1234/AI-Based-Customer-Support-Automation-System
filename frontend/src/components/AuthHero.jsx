import authBackground from '../assets/auth-background.jpg'

const teamMembers = [
  {
    name: 'Prajita Bhandari',
    studentId: '12255441',
    role: 'Backend & Database'
  },
  {
    name: 'Akrishta Ale',
    studentId: '12267761',
    role: 'Frontend & UI/UX'
  },
  {
    name: 'Ayush Bhandari',
    studentId: 'S12157470',
    role: 'AI Integration & Research'
  }
]

/**
 * Displays the project introduction and development team on authentication pages.
 *
 * @returns {JSX.Element} Shared authentication hero section.
 */
export default function AuthHero() {
  return (
    <section
      className="auth-hero"
      style={{ '--auth-background-image': `url(${authBackground})` }}
    >
      <div className="auth-brand">
        <span className="auth-brand-icon" aria-hidden="true">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M6 15V11C6 7.69 8.69 5 12 5C15.31 5 18 7.69 18 11V15"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
            />
            <path
              d="M6 13H5C4.45 13 4 13.45 4 14V17C4 17.55 4.45 18 5 18H7V13H6Z"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinejoin="round"
            />
            <path
              d="M18 13H19C19.55 13 20 13.45 20 14V17C20 17.55 19.55 18 19 18H17V13H18Z"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinejoin="round"
            />
            <path
              d="M17 18C17 19.1 16.1 20 15 20H13"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
            />
          </svg>
        </span>

        <span>AI Based Customer Support Automation System</span>
      </div>

      <div className="auth-hero-copy">
        <h1 className="auth-project-title">
          <span>COIT13230</span>
          <span>Application Development Project</span>
        </h1>

        <p className="project-description">
          An intelligent customer support platform combining AI, natural
          language processing, automated ticketing, sentiment analysis, smart
          escalation and real-time analytics to deliver faster, more accessible
          and effective service.
        </p>

        <div className="project-team">
          <p className="project-team-heading">Developed by</p>

          <div className="project-team-grid">
            {teamMembers.map((member) => (
              <div className="project-member" key={member.name}>
                <strong>{member.name}</strong>
                <span>{member.studentId}</span>
                <small>{member.role}</small>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}
