/* ===== AutomationPulse — AI News JS ===== */

/* ---- Dark Theme Toggle ---- */
(function initThemeToggle() {
  const btn = document.getElementById('themeToggle');

  // Clear stale values
  const saved = localStorage.getItem('theme');
  if (saved && saved !== 'dark' && saved !== 'light') {
    localStorage.removeItem('theme');
  }

  // Apply saved theme
  if (localStorage.getItem('theme') === 'dark') {
    document.body.classList.add('bw');
  }

  btn.addEventListener('click', () => {
    document.body.classList.toggle('bw');
    localStorage.setItem('theme', document.body.classList.contains('bw') ? 'dark' : 'light');
  });
})();
