/**
 * sidebar.js - Xử lý tương tác cho sidebar fragment
 */

/**
 * 1. Đóng/mở menu con (Dropdown)
 */
function toggleSubmenu(menuId, arrowId) {
    const menu = document.getElementById(menuId);
    const arrow = document.getElementById(arrowId);
    if (menu && arrow) {
        menu.classList.toggle('open');
        arrow.classList.toggle('open');
    }
}

/**
 * 2. Khởi tạo logic Sidebar
 */
function initSidebar() {
    const btnLogout = document.getElementById('btn-logout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            localStorage.removeItem('userToken');
            localStorage.removeItem('adminName');
            console.log("Client-side storage cleared.");
        });
    }

    // Tự động cuộn menu đang Active vào tầm nhìn nếu menu quá dài
    const activeLink = document.querySelector('.sidebar-link.active');
    if (activeLink) {
        activeLink.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

// Khởi chạy khi DOM đã sẵn sàng
document.addEventListener('DOMContentLoaded', () => {
    // Kiểm tra nếu có sidebar trên trang mới chạy init
    if (document.getElementById('admin-sidebar')) {
        initSidebar();
    }
});