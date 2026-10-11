(function () {
    'use strict';

    // Mobile sidebar toggle
    var sidebar = document.getElementById('appSidebar');
    var backdrop = document.getElementById('sidebarBackdrop');
    var toggle = document.getElementById('sidebarToggle');

    function setSidebar(open) {
        if (!sidebar || !backdrop) { return; }
        sidebar.classList.toggle('open', open);
        backdrop.classList.toggle('show', open);
    }

    if (toggle) {
        toggle.addEventListener('click', function () {
            setSidebar(!sidebar.classList.contains('open'));
        });
    }
    if (backdrop) {
        backdrop.addEventListener('click', function () { setSidebar(false); });
    }

    // Show / hide password fields
    document.querySelectorAll('.toggle-password').forEach(function (button) {
        button.addEventListener('click', function () {
            var input = button.parentElement.querySelector('input');
            var icon = button.querySelector('i');
            var show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            icon.className = show ? 'bi bi-eye-slash' : 'bi bi-eye';
        });
    });

    // Auto-dismiss success alerts
    document.querySelectorAll('.alert-success').forEach(function (alert) {
        setTimeout(function () {
            if (window.bootstrap) {
                bootstrap.Alert.getOrCreateInstance(alert).close();
            }
        }, 5000);
    });
})();