document.addEventListener('DOMContentLoaded', function () {
    const bellBtn = document.getElementById('bellBtn');
    const notiDropdown = document.getElementById('notiDropdown');
    const markAllReadBtn = document.getElementById('markAllReadBtn');
    const notiBadge = document.getElementById('notiBadge');

    if (bellBtn && notiDropdown) {
        // 1. CLICK CHUÔNG: Mở / Đóng
        bellBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            notiDropdown.classList.toggle('show');
        });

        // 2. CLICK RA NGOÀI: Đóng hộp
        document.addEventListener('click', function (e) {
            // Nếu vị trí click KHÔNG nằm trên chuông VÀ KHÔNG nằm trong hộp -> Đóng
            if (!bellBtn.contains(e.target) && !notiDropdown.contains(e.target)) {
                notiDropdown.classList.remove('show');
            }
        });

        // 3. CLICK VÀO 1 THÔNG BÁO: Đóng hộp ngay lập tức
        const notiItems = document.querySelectorAll('.noti-item');
        notiItems.forEach(item => {
            item.addEventListener('click', function () {
                notiDropdown.classList.remove('show');
            });
        });

        // 4. CLICK "ĐÁNH DẤU ĐÃ ĐỌC": Clear UI và đóng hộp
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', function (e) {
                e.preventDefault();
                fetch('/notifications/mark-all-read', {method: 'POST'})
                    .then(response => {
                        if (response.ok) {
                            // Ẩn chấm đỏ
                            if (notiBadge) notiBadge.style.display = 'none';
                            // Gỡ màu xanh chưa đọc
                            document.querySelectorAll('.noti-item.unread').forEach(item => {
                                item.classList.remove('unread');
                            });
                            // Đóng hộp
                            notiDropdown.classList.remove('show');

                            if (window.location.pathname === '/notifications') {
                                // Nếu đang đứng ở trang danh sách -> F5 load lại trang luôn để update data mới nhất
                                window.location.reload();
                            }
                        }
                        else if (response.status === 401) {
                            response.text().then(msg => alert("Lỗi: " + msg));
                        }
                    })
                    .catch(error => console.error('Lỗi khi call API đánh dấu đã đọc:', error));
            });
        }
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const btnFilterToggle = document.getElementById('btnFilterToggle');
    const filterDropdown = document.getElementById('filterDropdown');
    const btnResetFilter = document.getElementById('btnResetFilter');
    const filterForm = document.getElementById('filterForm');

    if (btnFilterToggle && filterDropdown) {
        // Bật/tắt dropdown khi click nút Lọc
        btnFilterToggle.addEventListener('click', function(e) {
            e.stopPropagation();
            filterDropdown.classList.toggle('show');
        });

        // Click ra ngoài thì đóng dropdown
        document.addEventListener('click', function(e) {
            if (!filterDropdown.contains(e.target) && e.target !== btnFilterToggle) {
                filterDropdown.classList.remove('show');
            }
        });

        // Ngăn chặn việc đóng menu khi đang loay hoay click chọn bên trong Dropdown
        filterDropdown.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }

    // Xử lý nút xóa trắng các lựa chọn
    if (btnResetFilter && filterForm) {
        btnResetFilter.addEventListener('click', function() {
            filterForm.reset(); // Đưa các select về trạng thái mặc định
        });
    }
});