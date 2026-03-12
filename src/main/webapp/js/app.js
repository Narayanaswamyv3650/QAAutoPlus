/**
 * QAAutoPlus — Main JavaScript
 * TechCrunch-style interactions: dark mode, search, scroll effects, back-to-top
 */
(function () {
    'use strict';

    // ============================
    // DARK MODE TOGGLE
    // ============================
    const themeBtn = document.getElementById('themeToggle');
    if (themeBtn) {
        // Restore saved theme
        const saved = localStorage.getItem('qaplus-theme');
        if (saved === 'dark') {
            document.body.classList.add('dark');
            themeBtn.textContent = '☀️';
        }

        themeBtn.addEventListener('click', function () {
            document.body.classList.toggle('dark');
            const isDark = document.body.classList.contains('dark');
            localStorage.setItem('qaplus-theme', isDark ? 'dark' : 'light');
            themeBtn.textContent = isDark ? '☀️' : '🌙';
        });
    }

    // ============================
    // TOPBAR SCROLL SHADOW
    // ============================
    const topbar = document.querySelector('.topbar');
    if (topbar) {
        window.addEventListener('scroll', function () {
            if (window.scrollY > 10) {
                topbar.classList.add('scrolled');
            } else {
                topbar.classList.remove('scrolled');
            }
        }, { passive: true });
    }

    // ============================
    // BACK TO TOP BUTTON
    // ============================
    const backToTop = document.getElementById('backToTop');
    if (backToTop) {
        window.addEventListener('scroll', function () {
            if (window.scrollY > 400) {
                backToTop.classList.add('visible');
            } else {
                backToTop.classList.remove('visible');
            }
        }, { passive: true });

        backToTop.addEventListener('click', function () {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // ============================
    // SEARCH OVERLAY
    // ============================
    const searchToggle = document.getElementById('searchToggle');
    const searchOverlay = document.getElementById('searchOverlay');
    const searchInput = document.getElementById('searchInput');
    const searchClose = document.getElementById('searchClose');
    const searchResults = document.getElementById('searchResults');

    // Build search index from the page content
    var searchIndex = [];
    function buildSearchIndex() {
        // Index category sections
        document.querySelectorAll('.category-section').forEach(function (section) {
            var catName = '';
            var h2 = section.querySelector('.section-header h2');
            if (h2) catName = h2.textContent.trim();

            section.querySelectorAll('.article-featured, .article-card').forEach(function (card) {
                var title = '';
                var h = card.querySelector('h3, h4');
                if (h) title = h.textContent.trim();
                var excerpt = '';
                var p = card.querySelector('p');
                if (p) excerpt = p.textContent.trim();
                var url = card.getAttribute('href') || '#';

                if (title) {
                    searchIndex.push({
                        title: title,
                        excerpt: excerpt,
                        category: catName,
                        url: url
                    });
                }
            });
        });

        // Index explore cards
        document.querySelectorAll('.explore-card').forEach(function (card) {
            var h2 = card.querySelector('h2');
            var p = card.querySelector('p');
            var url = card.getAttribute('href') || '#';
            if (h2) {
                searchIndex.push({
                    title: h2.textContent.trim(),
                    excerpt: p ? p.textContent.trim() : '',
                    category: 'Category',
                    url: url
                });
            }
        });

        // Index sidebar links
        document.querySelectorAll('.topic-tag').forEach(function (tag) {
            searchIndex.push({
                title: tag.textContent.trim(),
                excerpt: 'Testing tool / framework',
                category: 'Tool',
                url: tag.getAttribute('href') || '#'
            });
        });
    }

    if (searchToggle && searchOverlay) {
        buildSearchIndex();

        searchToggle.addEventListener('click', function () {
            searchOverlay.classList.add('active');
            setTimeout(function () {
                if (searchInput) searchInput.focus();
            }, 100);
        });

        if (searchClose) {
            searchClose.addEventListener('click', function () {
                searchOverlay.classList.remove('active');
                if (searchInput) searchInput.value = '';
                if (searchResults) searchResults.innerHTML = '';
            });
        }

        // Close on clicking overlay background
        searchOverlay.addEventListener('click', function (e) {
            if (e.target === searchOverlay) {
                searchOverlay.classList.remove('active');
                if (searchInput) searchInput.value = '';
                if (searchResults) searchResults.innerHTML = '';
            }
        });

        // Close on Escape
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && searchOverlay.classList.contains('active')) {
                searchOverlay.classList.remove('active');
                if (searchInput) searchInput.value = '';
                if (searchResults) searchResults.innerHTML = '';
            }
        });

        // Search as you type
        if (searchInput && searchResults) {
            searchInput.addEventListener('input', function () {
                var query = this.value.trim().toLowerCase();
                if (query.length < 2) {
                    searchResults.innerHTML = '';
                    return;
                }

                var matches = searchIndex.filter(function (item) {
                    return item.title.toLowerCase().indexOf(query) !== -1 ||
                           item.excerpt.toLowerCase().indexOf(query) !== -1 ||
                           item.category.toLowerCase().indexOf(query) !== -1;
                });

                if (matches.length === 0) {
                    searchResults.innerHTML = '<div class="search-result-item">No results found for "' + escHtml(query) + '"</div>';
                    return;
                }

                var html = '';
                matches.slice(0, 10).forEach(function (item) {
                    html += '<a href="' + escAttr(item.url) + '" target="_blank" class="search-result-item">';
                    html += '<span class="sr-cat">' + escHtml(item.category) + '</span>';
                    html += escHtml(item.title);
                    html += '</a>';
                });
                searchResults.innerHTML = html;
            });
        }
    }

    // ============================
    // KEYBOARD SHORTCUT: / to search
    // ============================
    document.addEventListener('keydown', function (e) {
        if (e.key === '/' && document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA') {
            e.preventDefault();
            if (searchToggle) searchToggle.click();
        }
    });

    // ============================
    // SMOOTH SCROLL FOR NAV LINKS
    // ============================
    document.querySelectorAll('.topbar-nav a[href^="/home#"]').forEach(function (link) {
        link.addEventListener('click', function (e) {
            var hash = this.getAttribute('href').split('#')[1];
            if (hash) {
                var target = document.getElementById(hash);
                if (target) {
                    e.preventDefault();
                    var offset = 80; // topbar height + padding
                    var top = target.getBoundingClientRect().top + window.pageYOffset - offset;
                    window.scrollTo({ top: top, behavior: 'smooth' });
                }
            }
        });
    });

    // ============================
    // LAZY ANIMATION ON SCROLL
    // ============================
    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });

        document.querySelectorAll('.article-card, .explore-card, .sidebar-card').forEach(function (el) {
            el.style.opacity = '0';
            el.style.transform = 'translateY(15px)';
            el.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            observer.observe(el);
        });
    }

    // ============================
    // NEWSLETTER SUBSCRIBE FORM
    // ============================
    var subForm  = document.getElementById('subscribeForm');
    var subName  = document.getElementById('subName');
    var subEmail = document.getElementById('subEmail');
    var subBtn   = document.getElementById('subBtn');
    var subMsg   = document.getElementById('subMsg');

    if (subForm) {
        subForm.addEventListener('submit', function (e) {
            e.preventDefault();
            var name  = subName  ? subName.value.trim()  : '';
            var email = subEmail ? subEmail.value.trim() : '';

            if (!email || email.indexOf('@') === -1) {
                showSubMsg('Please enter a valid email address.', 'error');
                return;
            }

            if (subBtn) { subBtn.disabled = true; subBtn.classList.add('loading'); subBtn.textContent = 'Sending...'; }

            fetch('/api/subscribe', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: name, email: email })
            })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (data.success) {
                    showSubMsg('✅ ' + data.message, 'success');
                    if (subName)  subName.value  = '';
                    if (subEmail) subEmail.value = '';
                } else {
                    showSubMsg('⚠️ ' + data.message, 'error');
                }
            })
            .catch(function () { showSubMsg('⚠️ Network error. Try again.', 'error'); })
            .finally(function () {
                if (subBtn) { subBtn.disabled = false; subBtn.classList.remove('loading'); subBtn.textContent = 'Subscribe'; }
            });
        });
    }

    function showSubMsg(text, type) {
        if (!subMsg) return;
        subMsg.textContent = text;
        subMsg.className = 'sub-msg show ' + type;
        setTimeout(function () { subMsg.className = 'sub-msg'; }, 6000);
    }

    // ============================
    // TRENDING: AUTO-SCROLL + DYNAMIC TIME
    // ============================
    // Duplicate trending items for seamless infinite scroll
    var trendingScroll = document.getElementById('trendingScroll');
    if (trendingScroll && trendingScroll.children.length > 0) {
        var clone = trendingScroll.innerHTML;
        trendingScroll.innerHTML += clone; // double items for seamless loop
    }

    // Update relative times every 30s
    function updateRelativeTimes() {
        document.querySelectorAll('.trending-time[data-published]').forEach(function (el) {
            var ts = parseInt(el.getAttribute('data-published'), 10);
            if (!ts || ts === 0) return;
            var diffSec = Math.floor((Date.now() - ts) / 1000);
            if (diffSec < 0) diffSec = 0;
            var text;
            if (diffSec < 60)        text = diffSec + 's ago';
            else if (diffSec < 3600) text = Math.floor(diffSec / 60) + 'm ago';
            else if (diffSec < 86400) text = Math.floor(diffSec / 3600) + 'h ago';
            else if (diffSec < 604800) text = Math.floor(diffSec / 86400) + 'd ago';
            else text = Math.floor(diffSec / 604800) + 'w ago';
            el.textContent = text;
        });
    }
    updateRelativeTimes();
    setInterval(updateRelativeTimes, 30000);

    // ============================
    // UTILITY: HTML ESCAPE
    // ============================
    function escHtml(str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    function escAttr(str) {
        return str.replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    // ============================
    // CONSOLE BRANDING
    // ============================
    console.log(
        '%c QAAutoPlus %c Tech News for QA Engineers ',
        'background:#4F46E5;color:#fff;font-weight:bold;padding:4px 8px;border-radius:4px 0 0 4px;',
        'background:#1a1a1a;color:#00A562;font-weight:bold;padding:4px 8px;border-radius:0 4px 4px 0;'
    );

})();
