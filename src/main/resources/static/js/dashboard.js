const {currentMode, revenueData, rankingData, baseUrl, currentBranchId} = window.DashboardData;

// ==========================================
// XỬ LÝ ẨN/HIỆN Ô NGÀY THÁNG VÀ NÚT TẢI
// ==========================================
document.getElementById('viewMode').addEventListener('change', function (e) {
    if (e.target.value === 'monthly') {
        document.getElementById('monthSelector').classList.replace('hidden', 'block');
        document.getElementById('yearSelector').classList.replace('block', 'hidden');
    } else {
        document.getElementById('monthSelector').classList.replace('block', 'hidden');
        document.getElementById('yearSelector').classList.replace('hidden', 'block');
    }
});

// Khi bấm Tải dữ Liệu -> Tạo URL mới và Load lại trang (Luồng MVC)
document.getElementById('btnLoadData').addEventListener('click', (e) => {
    e.preventDefault();

    const mode = document.getElementById('viewMode').value;

    // Dùng baseUrl động từ Thymeleaf thay vì gõ cứng
    let url = `${baseUrl}?mode=${mode}`;

    if (currentBranchId) {
        url += `&branchId=${currentBranchId}`;
    }

    if (mode === 'monthly') {
        const val = document.getElementById('monthSelector').value;
        if (!val) return alert("Vui lòng chọn tháng!");
        const [y, m] = val.split('-');
        url += `&month=${parseInt(m)}&year=${y}`;
    } else {
        const y = document.getElementById('yearSelector').value;
        if (!y) return alert("Vui lòng nhập năm!");
        url += `&year=${y}`;
    }

    window.location.href = url; // F5 lại trang với data mới
});


// ==========================================
// VẼ BIỂU ĐỒ (Sử dụng data từ Java truyền qua)
// ==========================================
document.addEventListener('DOMContentLoaded', () => {

    // Vẽ biểu đồ Doanh thu
    if (revenueData) {
        let chartLabels = [], chartValues = [];
        if (currentMode === 'monthly') {
            chartLabels = revenueData.dailyBreakdown.map(item => item.day);
            chartValues = revenueData.dailyBreakdown.map(item => item.revenue);
        } else {
            chartLabels = revenueData.monthlyBreakdown.map(item => 'Tháng ' + parseInt(item.monthAndYear.split('-')[1]));
            chartValues = revenueData.monthlyBreakdown.map(item => item.revenue);
        }

        new Chart(document.getElementById('revenueChart').getContext('2d'), {
            type: 'line',
            data: {
                labels: chartLabels,
                datasets: [{
                    label: 'Doanh thu (VNĐ)',
                    data: chartValues,
                    borderColor: '#10B981',
                    backgroundColor: 'rgba(16, 185, 129, 0.2)',
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {legend: {display: false}},
                scales: {
                    y: {
                        beginAtZero: true,
                        min: 0
                    }
                }
            }
        });
    }

    // Vẽ biểu đồ Xếp hạng
    if (rankingData && rankingData.courtRanking) {
        new Chart(document.getElementById('rankingChart').getContext('2d'), {
            type: 'bar',
            data: {
                labels: rankingData.courtRanking.map(item => item.courtName),
                datasets: [{
                    label: 'Lượt đặt',
                    data: rankingData.courtRanking.map(item => item.totalBookings),
                    backgroundColor: '#3B82F6',
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {legend: {display: false}},
                scales: {y: {beginAtZero: true, min: 0}},
            }
        });
    }
});