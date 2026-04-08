const bookingForm = document.getElementById('bookingForm');
const branchIdEl = document.getElementById('branchId');
const playDatePicker = document.getElementById('playDatePicker');
const selectionList = document.getElementById('selectionList');
const selectedSlotsHolder = document.getElementById('selectedSlotsHolder');
const serverSlotElements = document.querySelectorAll('.server-slot');
const totalHoursEl = document.getElementById('totalHours');
const totalAmountEl = document.getElementById('totalAmount');
const selectedSummary = document.getElementById('selectedSummary');
const bottomBarToggle = document.getElementById('bottomBarToggle');

const bookingConfirmModal = document.getElementById('bookingConfirmModal');
const confirmBranchNameEl = document.getElementById('confirmBranchName');
const confirmPlayDateEl = document.getElementById('confirmPlayDate');
const confirmTotalHoursEl = document.getElementById('confirmTotalHours');
const confirmTotalAmountEl = document.getElementById('confirmTotalAmount');
const confirmSelectionList = document.getElementById('confirmSelectionList');
const confirmSubmitBtn = document.getElementById('confirmSubmitBtn');
const confirmCloseElements = document.querySelectorAll('[data-confirm-close]');

const selectedMap = new Map();
let isSummaryExpanded = false;
let isSubmitConfirmed = false;

function todayString() {
    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const dd = String(now.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

function ensurePlayDate() {
    if (!playDatePicker) return;

    if (!playDatePicker.value) {
        const firstServerDate = document.querySelector('.server-slot[data-play-date]');
        playDatePicker.value = firstServerDate ? firstServerDate.dataset.playDate : todayString();
    }

    playDatePicker.min = todayString();
}

function applyFilters() {
    if (!bookingForm) return;

    const createUrl = bookingForm.dataset.createUrl;
    const userId = bookingForm.dataset.userId;
    const params = new URLSearchParams();
    params.set('userId', userId);

    if (branchIdEl && branchIdEl.value) {
        params.set('branchId', branchIdEl.value);
    }

    if (playDatePicker && playDatePicker.value) {
        params.set('playDate', playDatePicker.value);
    }

    window.location.href = `${createUrl}?${params.toString()}`;
}

function formatCurrency(value) {
    return `${new Intl.NumberFormat('vi-VN').format(value)} đ`;
}

function formatDuration(totalMinutes) {
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return `${hours}h${String(minutes).padStart(2, '0')}`;
}

function formatPlayDate(value) {
    if (!value) return '-';

    const date = new Date(`${value}T00:00:00`);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return new Intl.DateTimeFormat('vi-VN', {
        weekday: 'long',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    }).format(date);
}

function getSelectedItems() {
    return Array.from(selectedMap.values()).sort((left, right) => {
        if (left.courtName !== right.courtName) {
            return String(left.courtName).localeCompare(String(right.courtName), 'vi');
        }

        return String(left.timeLabel).localeCompare(String(right.timeLabel), 'vi');
    });
}

function updateSummaryVisibility() {
    if (!selectedSummary) return;

    if (selectedMap.size === 0) {
        selectedSummary.classList.add('hidden');
        selectedSummary.classList.add('collapsed');

        if (bottomBarToggle) {
            bottomBarToggle.classList.remove('expanded');
            bottomBarToggle.setAttribute('aria-expanded', 'false');
        }

        isSummaryExpanded = false;
        return;
    }

    selectedSummary.classList.remove('hidden');

    if (isSummaryExpanded) {
        selectedSummary.classList.remove('collapsed');

        if (bottomBarToggle) {
            bottomBarToggle.classList.add('expanded');
            bottomBarToggle.setAttribute('aria-expanded', 'true');
        }
    } else {
        selectedSummary.classList.add('collapsed');

        if (bottomBarToggle) {
            bottomBarToggle.classList.remove('expanded');
            bottomBarToggle.setAttribute('aria-expanded', 'false');
        }
    }
}

function toggleSummaryPanel() {
    if (selectedMap.size === 0) return;

    isSummaryExpanded = !isSummaryExpanded;
    updateSummaryVisibility();
}

function renderSelections() {
    if (!selectionList || !selectedSlotsHolder || !totalHoursEl || !totalAmountEl) {
        return;
    }

    selectionList.innerHTML = '';
    selectedSlotsHolder.innerHTML = '';

    if (selectedMap.size === 0) {
        totalHoursEl.textContent = '0h00';
        totalAmountEl.textContent = '0 đ';
        updateSummaryVisibility();
        return;
    }

    const items = getSelectedItems();

    items.forEach((item, index) => {
        const row = document.createElement('div');
        row.className = 'selected-item-inline';
        row.textContent = `${item.courtName}: ${item.timeLabel}${item.endTimeLabel ? ` - ${item.endTimeLabel}` : ''}`;
        selectionList.appendChild(row);

        const courtInput = document.createElement('input');
        courtInput.type = 'hidden';
        courtInput.name = `slots[${index}].courtId`;
        courtInput.value = item.courtId;

        const timeSlotInput = document.createElement('input');
        timeSlotInput.type = 'hidden';
        timeSlotInput.name = `slots[${index}].timeSlotId`;
        timeSlotInput.value = item.timeSlotId;

        const playDateInput = document.createElement('input');
        playDateInput.type = 'hidden';
        playDateInput.name = `slots[${index}].playDate`;
        playDateInput.value = playDatePicker ? playDatePicker.value : '';

        selectedSlotsHolder.appendChild(courtInput);
        selectedSlotsHolder.appendChild(timeSlotInput);
        selectedSlotsHolder.appendChild(playDateInput);
    });

    const totalMinutes = items.reduce((sum, item) => sum + Number(item.durationMinutes || 0), 0);
    const totalPrice = items.reduce((sum, item) => sum + Number(item.price || 0), 0);

    totalHoursEl.textContent = formatDuration(totalMinutes);
    totalAmountEl.textContent = formatCurrency(totalPrice);

    updateSummaryVisibility();
}

function fillConfirmationModal() {
    if (!bookingConfirmModal || !confirmSelectionList) {
        return;
    }

    const selectedBranchOption = branchIdEl ? branchIdEl.options[branchIdEl.selectedIndex] : null;
    const branchName = selectedBranchOption && selectedBranchOption.value
        ? selectedBranchOption.textContent.trim()
        : 'Chưa chọn';
    const items = getSelectedItems();

    confirmBranchNameEl.textContent = branchName;
    confirmPlayDateEl.textContent = formatPlayDate(playDatePicker ? playDatePicker.value : '');
    confirmTotalHoursEl.textContent = totalHoursEl ? totalHoursEl.textContent : '0h00';
    confirmTotalAmountEl.textContent = totalAmountEl ? totalAmountEl.textContent : '0 đ';

    confirmSelectionList.innerHTML = '';

    items.forEach((item, index) => {
        const row = document.createElement('div');
        row.className = 'confirm-selection-row';
        row.textContent = `${index + 1}. ${item.courtName}: ${item.timeLabel}${item.endTimeLabel ? ` - ${item.endTimeLabel}` : ''}`;
        confirmSelectionList.appendChild(row);
    });
}

function buildConfirmMessage() {
    const selectedBranchOption = branchIdEl ? branchIdEl.options[branchIdEl.selectedIndex] : null;
    const branchName = selectedBranchOption && selectedBranchOption.value
        ? selectedBranchOption.textContent.trim()
        : 'Chưa chọn';
    const lines = getSelectedItems().map((item, index) =>
        `${index + 1}. ${item.courtName}: ${item.timeLabel}${item.endTimeLabel ? ` - ${item.endTimeLabel}` : ''}`
    );

    return [
        'Xác nhận đặt sân?',
        `Chi nhánh: ${branchName}`,
        `Ngày chơi: ${formatPlayDate(playDatePicker ? playDatePicker.value : '')}`,
        `Tổng giờ: ${totalHoursEl ? totalHoursEl.textContent : '0h00'}`,
        `Tổng tiền: ${totalAmountEl ? totalAmountEl.textContent : '0 đ'}`,
        '',
        'Khung giờ đã chọn:',
        ...lines
    ].join('\n');
}

function openConfirmModal() {
    if (!bookingConfirmModal) return;

    bookingConfirmModal.classList.remove('hidden');
    bookingConfirmModal.setAttribute('aria-hidden', 'false');

    if (confirmSubmitBtn) {
        confirmSubmitBtn.focus();
    }
}

function closeConfirmModal() {
    if (!bookingConfirmModal) return;

    bookingConfirmModal.classList.add('hidden');
    bookingConfirmModal.setAttribute('aria-hidden', 'true');
}

function toggleSlot(slotEl, forceAdd = false) {
    if (!slotEl) return;

    const status = String(slotEl.dataset.status || '').trim().toUpperCase();
    const price = Number(slotEl.dataset.price || 0);
    const durationMinutes = Number(slotEl.dataset.durationMinutes || 0);

    if (status !== 'AVAILABLE' || price <= 0) {
        return;
    }

    const key = `${slotEl.dataset.courtId}_${slotEl.dataset.timeSlotId}`;
    const isSelected = selectedMap.has(key);

    if (isSelected && !forceAdd) {
        selectedMap.delete(key);
        slotEl.classList.remove('status-selected');
    } else if (!isSelected) {
        selectedMap.set(key, {
            key,
            courtId: slotEl.dataset.courtId,
            courtName: slotEl.dataset.courtName,
            timeSlotId: slotEl.dataset.timeSlotId,
            timeLabel: slotEl.dataset.timeLabel,
            endTimeLabel: slotEl.dataset.endTimeLabel,
            price,
            durationMinutes
        });
        slotEl.classList.add('status-selected');
    }

    renderSelections();
}

function preloadSelections() {
    serverSlotElements.forEach(serverSlot => {
        const courtId = serverSlot.dataset.courtId;
        const timeSlotId = serverSlot.dataset.timeSlotId;

        if (!courtId || !timeSlotId) return;

        const slotEl = document.querySelector(
            `.slot[data-court-id="${courtId}"][data-time-slot-id="${timeSlotId}"]`
        );

        if (slotEl && String(slotEl.dataset.status || '').toUpperCase() === 'AVAILABLE') {
            toggleSlot(slotEl, true);
        }
    });
}

ensurePlayDate();
preloadSelections();
renderSelections();

document.querySelectorAll('.slot').forEach(slot => {
    slot.addEventListener('click', function (event) {
        event.preventDefault();
        toggleSlot(this);
    });
});

if (bottomBarToggle) {
    bottomBarToggle.addEventListener('click', function () {
        toggleSummaryPanel();
    });

    bottomBarToggle.addEventListener('keydown', function (event) {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            toggleSummaryPanel();
        }
    });
}

confirmCloseElements.forEach(element => {
    element.addEventListener('click', function () {
        closeConfirmModal();
    });
});

if (confirmSubmitBtn && bookingForm) {
    confirmSubmitBtn.addEventListener('click', function () {
        isSubmitConfirmed = true;
        closeConfirmModal();

        if (typeof bookingForm.requestSubmit === 'function') {
            bookingForm.requestSubmit();
            return;
        }

        bookingForm.submit();
    });
}

document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && bookingConfirmModal && !bookingConfirmModal.classList.contains('hidden')) {
        closeConfirmModal();
    }
});

if (bookingForm) {
    bookingForm.addEventListener('submit', function (event) {
        renderSelections();

        if (isSubmitConfirmed) {
            isSubmitConfirmed = false;
            return;
        }

        if (selectedMap.size === 0) {
            event.preventDefault();
            window.alert('Vui lòng chọn ít nhất 1 khung giờ trước khi đặt sân.');
            return;
        }

        event.preventDefault();

        if (bookingConfirmModal && confirmSelectionList && confirmSubmitBtn) {
            fillConfirmationModal();
            openConfirmModal();
            return;
        }

        if (window.confirm(buildConfirmMessage())) {
            isSubmitConfirmed = true;

            if (typeof bookingForm.requestSubmit === 'function') {
                bookingForm.requestSubmit();
                return;
            }

            bookingForm.submit();
        }
    });
}

window.applyFilters = applyFilters;
