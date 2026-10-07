(function () {
    var savedTheme = localStorage.getItem('autoShieldTheme');
    if (savedTheme === 'light' || savedTheme === 'dark') {
        document.documentElement.dataset.theme = savedTheme;
    }

    document.addEventListener('DOMContentLoaded', function () {
        var themeToggle = document.getElementById('dashboard-theme-toggle');
        if (!themeToggle) {
            return;
        }

        var themeIcon = themeToggle.querySelector('i');
        var themeLabel = themeToggle.querySelector('span');

        function updateThemeToggle() {
            var isLight = document.documentElement.dataset.theme === 'light';
            themeToggle.setAttribute('aria-pressed', String(isLight));
            themeToggle.title = isLight ? 'Switch to dark mode' : 'Switch to light mode';
            if (themeIcon) {
                themeIcon.className = isLight ? 'fa-solid fa-moon' : 'fa-solid fa-sun';
            }
            if (themeLabel) {
                themeLabel.textContent = isLight ? 'Dark mode' : 'Light mode';
            }
        }

        themeToggle.addEventListener('click', function () {
            var isLight = document.documentElement.dataset.theme !== 'light';
            document.documentElement.dataset.theme = isLight ? 'light' : 'dark';
            localStorage.setItem('autoShieldTheme', isLight ? 'light' : 'dark');
            updateThemeToggle();
        });

        updateThemeToggle();
    });
})();
