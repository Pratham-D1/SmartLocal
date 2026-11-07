/**
 * SmartLocal - Client-Side Interactive Script (Module 1)
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log('SmartLocal Module 1 initialized successfully.');

    // Interactive Location Selector
    const locationCards = document.querySelectorAll('.location-card');
    const locationInput = document.getElementById('selectedLocationInput');
    const activeLocationDisplay = document.getElementById('activeLocationBadge');

    locationCards.forEach(card => {
        card.addEventListener('click', () => {
            const locationId = card.getAttribute('data-location-id');
            const locationName = card.getAttribute('data-location-name');

            // Toggle active state
            locationCards.forEach(c => c.classList.remove('active', 'border-primary', 'bg-light'));
            card.classList.add('active', 'border-primary', 'bg-light');

            if (locationInput) {
                locationInput.value = locationId;
            }

            if (activeLocationDisplay) {
                activeLocationDisplay.innerHTML = `<i class="bi bi-geo-alt-fill text-danger me-1"></i> Selected: <strong>${locationName}</strong>`;
                activeLocationDisplay.classList.remove('d-none');
            }

            // Scroll feedback notice
            showToast(`Location Selected: ${locationName}`);
        });
    });

    // Search bar submit handler
    const searchForm = document.getElementById('productSearchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            const queryInput = document.getElementById('productSearchInput');
            if (queryInput && queryInput.value.trim() === '') {
                e.preventDefault();
                queryInput.focus();
                showToast('Please type a product keyword to search (e.g., "Milk", "Headphones", "Bread")');
            }
        });
    }

    // Helper Toast Notification
    function showToast(message) {
        let toastContainer = document.getElementById('smartToastContainer');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'smartToastContainer';
            toastContainer.className = 'position-fixed bottom-0 end-0 p-3';
            toastContainer.style.zIndex = '1090';
            document.body.appendChild(toastContainer);
        }

        const toastEl = document.createElement('div');
        toastEl.className = 'toast align-items-center text-bg-dark border-0 shadow-lg';
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'assertive');
        toastEl.setAttribute('aria-atomic', 'true');
        toastEl.innerHTML = `
            <div class="d-flex">
                <div class="toast-body d-flex align-items-center gap-2">
                    <i class="bi bi-info-circle-fill text-info fs-5"></i>
                    <span>${message}</span>
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        `;
        toastContainer.appendChild(toastEl);
        const bsToast = new bootstrap.Toast(toastEl, { delay: 4000 });
        bsToast.show();
        toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
    }
});
