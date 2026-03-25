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

    const selectedMap = new Map();
    let isSummaryExpanded = false;

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
        return new Intl.NumberFormat('vi-VN').format(value) + ' đ';
    }

    function formatDuration(totalMinutes) {
        const hours = Math.floor(totalMinutes / 60);
        const minutes = totalMinutes % 60;
        return `${hours}h${String(minutes).padStart(2, '0')}`;
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
        selectionList.innerHTML = '';
        selectedSlotsHolder.innerHTML = '';

        if (selectedMap.size === 0) {
            totalHoursEl.textContent = '0h00';
            totalAmountEl.textContent = '0 đ';
            updateSummaryVisibility();
            return;
        }

        Array.from(selectedMap.values()).forEach((item, index) => {
            const row = document.createElement('div');
            row.className = 'selected-item-inline';
            row.textContent = `${item.courtName}: ${item.timeLabel} - ${item.endTimeLabel || ''}`.replace(/\s-\s$/, '');
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

        const totalMinutes = Array.from(selectedMap.values())
            .reduce((sum, item) => sum + Number(item.durationMinutes || 0), 0);

        const totalPrice = Array.from(selectedMap.values())
            .reduce((sum, item) => sum + Number(item.price || 0), 0);

        totalHoursEl.textContent = formatDuration(totalMinutes);
        totalAmountEl.textContent = formatCurrency(totalPrice);

        updateSummaryVisibility();
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

            if (slotEl && (slotEl.dataset.status || '').toUpperCase() === 'AVAILABLE') {
                toggleSlot(slotEl, true);
            }
        });
    }

    ensurePlayDate();
    preloadSelections();
    renderSelections();

    document.querySelectorAll('.slot').forEach(slot => {
        slot.addEventListener('click', function (e) {
            e.preventDefault();
            toggleSlot(this);
        });
    });

    if (bottomBarToggle) {
        bottomBarToggle.addEventListener('click', function () {
            toggleSummaryPanel();
        });

        bottomBarToggle.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                toggleSummaryPanel();
            }
        });
    }

    bookingForm.addEventListener('submit', function () {
        renderSelections();
    });

    window.applyFilters = applyFilters;