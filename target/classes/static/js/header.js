document.addEventListener('DOMContentLoaded', function () {

    // ==========================================
    // 1. XỬ LÝ THÔNG BÁO (NOTIFICATION)
    // ==========================================
    const bellBtn = document.getElementById('bellBtn');
    const notiDropdown = document.getElementById('notiDropdown');
    const markAllReadBtn = document.getElementById('markAllReadBtn');
    const notiBadge = document.getElementById('notiBadge');

    if (bellBtn && notiDropdown) {
        // Mở / Đóng hộp thông báo
        bellBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            notiDropdown.classList.toggle('show');
        });

        // Click 1 thông báo -> Đóng hộp
        const notiItems = document.querySelectorAll('.noti-item');
        notiItems.forEach(item => {
            item.addEventListener('click', function () {
                notiDropdown.classList.remove('show');
            });
        });

        // Đánh dấu đã đọc
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', function (e) {
                e.preventDefault();
                fetch('/notifications/mark-all-read', { method: 'POST' })
                    .then(response => {
                        if (response.ok) {
                            if (notiBadge) notiBadge.style.display = 'none';
                            document.querySelectorAll('.noti-item.unread').forEach(item => {
                                item.classList.remove('unread');
                            });
                            notiDropdown.classList.remove('show');

                            if (window.location.pathname === '/notifications') {
                                window.location.reload();
                            }
                        } else if (response.status === 401) {
                            response.text().then(msg => alert("Lỗi: " + msg));
                        }
                    })
                    .catch(error => console.error('Lỗi khi call API đánh dấu đã đọc:', error));
            });
        }
    }

    // ==========================================
    // 2. XỬ LÝ BỘ LỌC TÌM KIẾM
    // ==========================================
    const btnFilterToggle = document.getElementById('btnFilterToggle');
    const filterDropdown = document.getElementById('filterDropdown');
    const btnResetFilter = document.getElementById('btnResetFilter');
    const mainSearchForm = document.getElementById('mainSearchForm');

    if (btnFilterToggle && filterDropdown) {
        // Mở / Đóng bộ lọc
        btnFilterToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            filterDropdown.classList.toggle('show');
        });

        // Ngăn click bên trong dropdown làm đóng menu
        filterDropdown.addEventListener('click', function (e) {
            e.stopPropagation();
        });
    }

    // Xử lý nút Xóa Lọc
    if (btnResetFilter && mainSearchForm) {
        btnResetFilter.addEventListener('click', function () {
            const selects = mainSearchForm.querySelectorAll('select');
            selects.forEach(select => {
                select.selectedIndex = 0;
            });
        });
    }

    // ==========================================
    // 3. XỬ LÝ CLICK RA NGOÀI
    // ==========================================
    document.addEventListener('click', function (e) {
        // Xử lý đóng Thông báo
        if (bellBtn && notiDropdown && !bellBtn.contains(e.target) && !notiDropdown.contains(e.target)) {
            notiDropdown.classList.remove('show');
        }

        // Xử lý đóng Bộ lọc
        if (btnFilterToggle && filterDropdown && !btnFilterToggle.contains(e.target) && !filterDropdown.contains(e.target)) {
            filterDropdown.classList.remove('show');
        }
    });

});