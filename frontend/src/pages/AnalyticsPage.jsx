import { useEffect, useState } from 'react'
import { analyticsApi } from '../api/client'
import { TICKET_PRIORITIES, TICKET_STATUSES } from '../config/constants'
import Loader from '../components/Loader'
import SimpleChart from '../components/SimpleChart'
import StatCard from '../components/StatCard'
import TicketCategoryOptions from '../components/TicketCategoryOptions'
import { formatDateTime, formatEnum } from '../utils/format'

/** Converts backend analytics maps into the label/value arrays expected by Chart.js. */
function chartData(map = {}) {
  return {
    labels: Object.keys(map).map((key) => key.includes('_') ? formatEnum(key) : key),
    values: Object.values(map)
  }
}

const blankFilters = {
  startDate: '',
  endDate: '',
  category: '',
  priority: '',
  status: '',
  minSentiment: '',
  maxSentiment: ''
}

/**
 * Loads administrative support analytics, filters them and generates weekly or monthly reports.
 *
 * @returns {JSX.Element} Analytics dashboard page.
 */
export default function AnalyticsPage() {
  const [data, setData] = useState(null)
  const [filters, setFilters] = useState(blankFilters)
  const [reportDate, setReportDate] = useState(() => {
    const today = new Date()
    const year = today.getFullYear()
    const month = String(today.getMonth() + 1).padStart(2, '0')
    const day = String(today.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  })
  const [reportType, setReportType] = useState('WEEKLY')
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = async (selectedFilters = null) => {
    setLoading(true)
    setError('')
    try {
      const result = selectedFilters ? await analyticsApi.filter(selectedFilters) : await analyticsApi.summary()
      setData(result)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const applyFilters = (event) => {
    event.preventDefault()
    load(filters)
  }

  const clearFilters = () => {
    setFilters(blankFilters)
    load()
  }

  const generateReport = async () => {
    setError('')
    try {
      const result = reportType === 'WEEKLY'
        ? await analyticsApi.weeklyReport(reportDate)
        : await analyticsApi.monthlyReport(reportDate)
      setReport(result)
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading && !data) return <Loader label="Calculating support analytics…" />
  if (!data && error) return <div className="alert alert-error">{error}</div>

  const daily = chartData(data?.dailyTicketVolume)
  const category = chartData(data?.ticketsByCategory)
  const sentiment = chartData(data?.sentimentDistribution)
  const status = chartData(data?.ticketsByStatus)

  return (
    <>
      <section className="page-heading split">
        <div>
          <p className="eyebrow">Real-Time Analytics</p>
          <h1>Customer Support Performance</h1>
        </div>
        <button className="button button-secondary" onClick={() => load()}>Refresh data</button>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      <form className="panel analytics-filters" onSubmit={applyFilters}>
        <div className="panel-header"><div><h2>Analytics filters</h2></div></div>
        <div className="filter-grid">
          <label>Start date<input type="date" value={filters.startDate} onChange={(event) => setFilters({ ...filters, startDate: event.target.value })} /></label>
          <label>End date<input type="date" value={filters.endDate} onChange={(event) => setFilters({ ...filters, endDate: event.target.value })} /></label>
          <label>Category<select value={filters.category} onChange={(event) => setFilters({ ...filters, category: event.target.value })}>
            <option value="">All categories</option><TicketCategoryOptions />
          </select></label>
          <label>Priority<select value={filters.priority} onChange={(event) => setFilters({ ...filters, priority: event.target.value })}>
            <option value="">All priorities</option>{TICKET_PRIORITIES.map((item) => <option key={item} value={item}>{formatEnum(item)}</option>)}
          </select></label>
          <label>Status<select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value })}>
            <option value="">All statuses</option>{TICKET_STATUSES.map((item) => <option key={item} value={item}>{formatEnum(item)}</option>)}
          </select></label>
          <label>Minimum sentiment<input type="number" min="-1" max="1" step="0.1" value={filters.minSentiment}
            onChange={(event) => setFilters({ ...filters, minSentiment: event.target.value })} placeholder="-1.0" /></label>
          <label>Maximum sentiment<input type="number" min="-1" max="1" step="0.1" value={filters.maxSentiment}
            onChange={(event) => setFilters({ ...filters, maxSentiment: event.target.value })} placeholder="1.0" /></label>
        </div>
        <div className="button-row">
          <button className="button button-primary" disabled={loading}>{loading ? 'Applying…' : 'Apply filters'}</button>
          <button type="button" className="button button-ghost" onClick={clearFilters}>Reset</button>
        </div>
      </form>

      <section className="stats-grid">
        <StatCard label="Total tickets" value={data.totalTickets} helper={`${data.openTickets} currently open`} icon="▣" />
        <StatCard label="Escalation rate" value={`${data.escalationRate}%`} helper={`${data.escalatedTickets} human reviews`} icon="↗" />
        <StatCard label="AI success rate" value={`${data.chatbotSuccessRate}%`} helper="Resolved by automation" icon="✦" />
        <StatCard label="Avg. first response" value={`${data.averageFirstResponseSeconds}s`} helper="Target: under 3 seconds" icon="◷" />
      </section>

      <section className="analytics-grid">
        <article className="panel chart-panel wide">
          <div className="panel-header"><div><h2>Tickets created in the last 7 days</h2></div></div>
          <SimpleChart type="line" labels={daily.labels} values={daily.values} label="Tickets" />
        </article>
        <article className="panel chart-panel">
          <div className="panel-header"><div><h2>Tickets by category</h2></div></div>
          <SimpleChart type="bar" labels={category.labels} values={category.values} label="Tickets" />
        </article>
        <article className="panel chart-panel">
          <div className="panel-header"><div><h2>Sentiment distribution</h2></div></div>
          <SimpleChart type="doughnut" labels={sentiment.labels} values={sentiment.values} label="Tickets" />
        </article>
        <article className="panel chart-panel">
          <div className="panel-header"><div><h2>Tickets by status</h2></div></div>
          <SimpleChart type="bar" labels={status.labels} values={status.values} label="Tickets" />
        </article>
        <article className="panel metric-summary">
          <h2>Resolution summary</h2>
          <div className="large-metric"><strong>{data.resolvedTickets}</strong><span>resolved tickets</span></div>
          <div className="large-metric"><strong>{data.averageResolutionMinutes} min</strong><span>average resolution time</span></div>
        </article>
      </section>

      <section className="panel report-panel">
        <div className="panel-header"><div><h2>Report summary</h2></div></div>
        <div className="report-controls">
          <select value={reportType} onChange={(event) => setReportType(event.target.value)}>
            <option value="WEEKLY">Weekly report</option>
            <option value="MONTHLY">Monthly report</option>
          </select>
          <input type="date" value={reportDate} onChange={(event) => setReportDate(event.target.value)} />
          <button className="button button-secondary" onClick={generateReport}>Generate report</button>
        </div>
        {report && (
          <div className="report-result">
            <strong>{formatEnum(report.reportType)}: {report.startDate} to {report.endDate}</strong>
            <span>Generated {formatDateTime(report.generatedAt)}</span>
            <span>{report.summary.totalTickets} tickets · {report.summary.escalatedTickets} escalated · {report.summary.resolvedTickets} resolved</span>
          </div>
        )}
      </section>
    </>
  )
}
