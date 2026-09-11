import { useEffect, useRef } from 'react'
import Chart from 'chart.js/auto'

export default function SimpleChart({ type = 'bar', labels, values, label }) {
  const canvasRef = useRef(null)

  useEffect(() => {
    if (!canvasRef.current) return undefined
    const chart = new Chart(canvasRef.current, {
      type,
      data: {
        labels,
        datasets: [{
          label,
          data: values,
          borderWidth: 2,
          borderRadius: type === 'bar' ? 8 : 0,
          tension: 0.35,
          fill: type === 'line'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: type !== 'bar' } },
        scales: type === 'doughnut' ? {} : {
          y: { beginAtZero: true, ticks: { precision: 0 } },
          x: { grid: { display: false } }
        }
      }
    })
    return () => chart.destroy()
  }, [type, labels, values, label])

  return <div className="chart-canvas"><canvas ref={canvasRef} /></div>
}
