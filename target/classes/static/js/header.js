document.addEventListener('DOMContentLoaded', function () {
    const bellBtn = document.getElementById('bellBtn');
    const notiDropdown = document.getElementById('notiDropdown');
    const markAllReadBtn = document.getElementById('markAllReadBtn');
    const notiBadge = document.getElementById('notiBadge');

    if (bellBtn && notiDropdown) {
        // 1. CLICK CHUÔNG: Mở / Đóng
        bellBtn.addEventListener('click', function (e) {
            e.stopPropagation(); // Ngăn click lọt ra ngoài
            notiDropdown.classList.toggle('show');
        });

        // 2. CLICK RA NGOÀI: Đóng hộp
        document.addEventListener('click', function (e) {
            // Nếu vị trí click KHÔNG nằm trên chuông VÀ KHÔNG nằm trong hộp -> Đóng!
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
                fetch('/notifications/mark-all-read', {method: 'POST'}) // Nhớ thêm CSRF token nếu Spring Security yêu cầu
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