const {branchesMap, currentMode, revenueData, rankingData, baseUrl} = window.DashboardData;
const currentBranchId = document.getElementById('selectedBranchId').value;

// ==========================================
// 1. LOGIC CHO BỘ LỌC TÌM KIẾM CƠ SỞ
// ==========================================
const branchSearchInput = document.getElementById('branchSearchInput');
const branchDropdownList = document.getElementById('branchDropdownList');

// Hiển thị tên cơ sở đang chọn lúc mới load trang
if (currentBranchId && branchesMap[currentBranchId]) {
    branchSearchInput.value = branchesMap[currentBranchId];
}

function renderDropdown(entries) {
    branchDropdownList.innerHTML = '';
    if (entries.length === 0) {
        branchDropdownList.innerHTML = '<li class="px-4 py-3 text-sm text-gray-500 text-center">Không tìm thấy cơ sở</li>';
        return;
    }
    entries.forEach(([id, name]) => {
        const li = document.createElement('li');
        li.className = 'px-4 py-2 hover:bg-emerald-50 hover:text-emerald-700 cursor-pointer text-sm text-gray-700 border-b border-gray-50 last:border-0';
        li.textContent = name;
        li.onclick = () => {
            document.getElementById('selectedBranchId').value = id;
            branchSearchInput.value = name;
            branchDropdownList.classList.add('hidden');
        };
        branchDropdownList.appendChild(li);
    });
}

// Lọc cơ sở khi gõ
branchSearchInput.addEventListener('input', function () {
    const keyword = this.value.toLowerCase().trim();
    const filtered = Object.entries(branchesMap).filter(([id, name]) => name.toLowerCase().includes(keyword));
    renderDropdown(filtered);
    branchDropdownList.classList.remove('hidden');
});

// Xổ list khi click vào input
branchSearchInput.addEventListener('focus', () => {
    branchDropdownList.classList.remove('hidden');
    if (!branchSearchInput.value.trim()) renderDropdown(Object.entries(branchesMap));
});

// Ẩn list khi click ra ngoài
document.addEventListener('click', e => {
    if (!document.getElementById('branchSelectContainer').contains(e.target)) {
        branchDropdownList.classList.add('hidden');
        const selectedId = document.getElementById('selectedBranchId').value;
        if (selectedId && branchesMap[selectedId]) {
            branchSearchInput.value = branchesMap[selectedId];
        }
    }
});


// ==========================================
// 2. XỬ LÝ ẨN/HIỆN Ô NGÀY THÁNG VÀ NÚT TẢI
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
    e.preventDefault(); // Chặn hành vi submit mặc định

    const mode = document.getElementById('viewMode').value;
    const branchId = document.getElementById('selectedBranchId').value;

    // Lấy areaId từ URL hiện tại để không bị mất khi F5
    const urlParams = new URLSearchParams(window.location.search);
    const areaId = urlParams.get('areaId') || 1;

    if (!branchId) return alert("Vui lòng chọn cơ sở!");

    // Dùng baseUrl động từ Thymeleaf thay vì gõ cứng
    let url = `${baseUrl}?areaId=${areaId}&branchId=${branchId}&mode=${mode}`;

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
// 3. VẼ BIỂU ĐỒ (Sử dụng data từ Java truyền qua)
// ==========================================
document.addEventListener('DOMContentLoaded', () => {

    // Khởi tạo list dropdown lần đầu
    renderDropdown(Object.entries(branchesMap));

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