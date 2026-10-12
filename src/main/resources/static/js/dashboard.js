// SentinelTrace Dashboard Interactive Charts & Polling
let trendChart = null;
let severityChart = null;

document.addEventListener('DOMContentLoaded', function () {
    const data = window.INITIAL_SUMMARY || {};
    initTrendChart(data.eventTimeline || []);
    initSeverityChart(data.severityDistribution || {});

    // Periodic polling every 8 seconds for real-time dashboard updates
    setInterval(refreshDashboardData, 8000);
});

function initTrendChart(timeline) {
    const ctx = document.getElementById('eventTrendChart');
    if (!ctx) return;

    const labels = timeline.map(p => p.timestamp);
    const totalData = timeline.map(p => p.totalCount);
    const failedData = timeline.map(p => p.failedLoginCount);

    trendChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Total Events',
                    data: totalData,
                    borderColor: '#00e5ff',
                    backgroundColor: 'rgba(0, 229, 255, 0.08)',
                    fill: true,
                    tension: 0.35,
                    borderWidth: 2,
                    pointRadius: 3,
                    pointBackgroundColor: '#00e5ff'
                },
                {
                    label: 'Failed Logins',
                    data: failedData,
                    borderColor: '#f97316',
                    backgroundColor: 'rgba(249, 115, 22, 0.1)',
                    fill: true,
                    tension: 0.35,
                    borderWidth: 2,
                    pointRadius: 3,
                    pointBackgroundColor: '#f97316'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: '#94a3b8',
                        font: { family: 'Outfit', size: 12 }
                    }
                },
                tooltip: {
                    backgroundColor: '#162032',
                    titleColor: '#f8fafc',
                    bodyColor: '#cbd5e1',
                    borderColor: 'rgba(56, 75, 107, 0.5)',
                    borderWidth: 1
                }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(56, 75, 107, 0.15)' },
                    ticks: { color: '#64748b', font: { family: 'JetBrains Mono', size: 10 } }
                },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(56, 75, 107, 0.15)' },
                    ticks: {
                        color: '#64748b',
                        precision: 0,
                        font: { family: 'JetBrains Mono', size: 10 }
                    }
                }
            }
        }
    });
}

function initSeverityChart(distribution) {
    const ctx = document.getElementById('severityChart');
    if (!ctx) return;

    const labels = Object.keys(distribution);
    const values = Object.values(distribution);

    // If all zeros, show placeholder
    const allZero = values.every(v => v === 0);
    const displayValues = allZero ? [1, 0, 0, 0] : values;
    const colors = ['#06b6d4', '#eab308', '#f97316', '#ef4444'];

    severityChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels.length > 0 ? labels : ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'],
            datasets: [{
                data: displayValues,
                backgroundColor: colors,
                borderColor: '#111827',
                borderWidth: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#94a3b8',
                        font: { family: 'Outfit', size: 11 },
                        padding: 12
                    }
                },
                tooltip: {
                    backgroundColor: '#162032',
                    titleColor: '#f8fafc',
                    bodyColor: '#cbd5e1',
                    borderColor: 'rgba(56, 75, 107, 0.5)',
                    borderWidth: 1
                }
            },
            cutout: '68%'
        }
    });
}

function refreshDashboardData() {
    fetch('/api/dashboard/summary')
        .then(res => res.json())
        .then(summary => {
            // Update stat values
            const totalEventsEl = document.getElementById('stat-total-events');
            if (totalEventsEl) totalEventsEl.textContent = summary.totalEvents;

            const failedLoginsEl = document.getElementById('stat-failed-logins');
            if (failedLoginsEl) failedLoginsEl.textContent = summary.failedLoginCount;

            const openIncidentsEl = document.getElementById('stat-open-incidents');
            if (openIncidentsEl) openIncidentsEl.textContent = summary.openIncidents;

            const resolvedIncidentsEl = document.getElementById('stat-resolved-incidents');
            if (resolvedIncidentsEl) resolvedIncidentsEl.textContent = summary.resolvedIncidents;

            // Update Trend Chart
            if (trendChart && summary.eventTimeline) {
                trendChart.data.labels = summary.eventTimeline.map(p => p.timestamp);
                trendChart.data.datasets[0].data = summary.eventTimeline.map(p => p.totalCount);
                trendChart.data.datasets[1].data = summary.eventTimeline.map(p => p.failedLoginCount);
                trendChart.update();
            }

            // Update Severity Chart
            if (severityChart && summary.severityDistribution) {
                const values = Object.values(summary.severityDistribution);
                severityChart.data.datasets[0].data = values;
                severityChart.update();
            }
        })
        .catch(err => console.debug('Metrics update poller error (benign):', err));
}
