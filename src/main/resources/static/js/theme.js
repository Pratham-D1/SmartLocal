/**
 * SmartLocal - Theme Manager (Light / Dark / System)
 * Manages theme selection, persistence, OS synchronization, and UI state.
 */
(function () {
    'use strict';

    const THEME_KEY = 'smartlocal-theme';

    /**
     * Retrieves stored preference or defaults to 'light'
     */
    function getStoredTheme() {
        return localStorage.getItem(THEME_KEY) || 'light';
    }

    /**
     * Determines whether OS prefers dark mode
     */
    function getSystemPreference() {
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    /**
     * Resolves setting ('light' | 'dark' | 'system') into effective mode ('light' | 'dark')
     */
    function resolveTheme(setting) {
        if (setting === 'system') {
            return getSystemPreference();
        }
        return setting === 'dark' ? 'dark' : 'light';
    }

    /**
     * Applies the theme attribute to the root document element and syncs the UI
     */
    function applyTheme(setting) {
        const effective = resolveTheme(setting);
        document.documentElement.setAttribute('data-theme', effective);
        document.documentElement.setAttribute('data-theme-setting', setting);
        updateDropdownUI(setting, effective);
    }

    /**
     * Updates the navbar dropdown button icon and active checkmarks
     */
    function updateDropdownUI(setting, effective) {
        const iconEl = document.getElementById('themeIconActive');
        if (iconEl) {
            iconEl.className = 'bi';
            if (setting === 'system') {
                iconEl.classList.add('bi-circle-half');
                iconEl.style.color = effective === 'dark' ? '#60A5FA' : '#64748B';
            } else if (setting === 'dark') {
                iconEl.classList.add('bi-moon-stars-fill');
                iconEl.style.color = '#60A5FA';
            } else {
                iconEl.classList.add('bi-sun-fill');
                iconEl.style.color = '#D97706';
            }
        }

        const items = document.querySelectorAll('[data-theme-value]');
        items.forEach(btn => {
            const val = btn.getAttribute('data-theme-value');
            const check = btn.querySelector('.theme-check-icon');
            if (val === setting) {
                btn.classList.add('active', 'fw-semibold');
                if (check) check.classList.remove('d-none');
            } else {
                btn.classList.remove('active', 'fw-semibold');
                if (check) check.classList.add('d-none');
            }
        });
    }

    /**
     * Public handler called when user selects an option
     */
    window.setSmartLocalTheme = function (setting) {
        localStorage.setItem(THEME_KEY, setting);
        applyTheme(setting);
    };

    // Listen to OS scheme changes if 'system' is currently selected
    if (window.matchMedia) {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
            const currentSetting = getStoredTheme();
            if (currentSetting === 'system') {
                applyTheme('system');
            }
        });
    }

    // Initialize immediately to prevent flash
    const initialSetting = getStoredTheme();
    applyTheme(initialSetting);

    // Synchronize UI once DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            updateDropdownUI(getStoredTheme(), resolveTheme(getStoredTheme()));
        });
    } else {
        updateDropdownUI(initialSetting, resolveTheme(initialSetting));
    }
})();

