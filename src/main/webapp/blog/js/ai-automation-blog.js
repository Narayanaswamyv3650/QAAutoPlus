/* ===== AutomationPulse Blog JS ===== */

const API = '/api';
const tagClasses = { ai:'tag-ai', ml:'tag-ml', rpa:'tag-rpa', llm:'tag-llm', devops:'tag-devops' };
let articles = [];
let activeFilter = 'all';
let selectedTag = 'ai';

/* ---- Render helpers ---- */
function postCard(p, big) {
  const tagClass = tagClasses[p.categorySlug] || 'tag-ai';
  const label = p.categoryLabel || p.categorySlug;
  const date = p.createdAt ? new Date(p.createdAt).toLocaleDateString('en-US',{month:'short',day:'numeric',year:'numeric'}) : '';
  return `<div class="${big?'featured-post':'blog-card'}" onclick="openModal(${p.id})">
    <span class="post-tag ${tagClass}">${label}</span>
    <div class="post-title">${p.title}</div>
    <div class="post-excerpt">${p.excerpt}</div>
    <div class="post-meta">
      <span class="author">${p.author}</span>
      <span class="sep">·</span>
      <span>${date}</span>
      <span class="read-time">⏱ ${p.readTime || ''}</span>
    </div>
  </div>`;
}

function renderFeatured() {
  const featured = articles.filter(p => p.featured);
  const rest = articles.filter(p => !p.featured).slice(0, 2);
  const show = [...featured, ...rest].slice(0, 3);
  const grid = document.getElementById('featuredGrid');
  if (!show.length) { grid.innerHTML = '<div class="loading-msg">No featured articles yet.</div>'; return; }
  grid.innerHTML = show.map(p => postCard(p, true)).join('');
}

function renderBlogGrid(tag) {
  const posts = tag === 'all' ? articles : articles.filter(p => p.categorySlug === tag);
  const grid = document.getElementById('blogGrid');
  if (!posts.length) {
    grid.innerHTML = '<div class="loading-msg">No articles in this category yet.</div>';
    return;
  }
  grid.innerHTML = posts.map(p => postCard(p, false)).join('');
}

/* ---- API calls ---- */
async function loadArticles(category) {
  try {
    const url = category && category !== 'all'
      ? `${API}/articles?category=${category}`
      : `${API}/articles`;
    const resp = await fetch(url);
    const data = await resp.json();
    articles = data.articles || [];
    document.getElementById('postCount').textContent = data.count || 0;
    renderFeatured();
    renderBlogGrid(activeFilter);
  } catch (e) {
    console.error('Failed to load articles:', e);
    document.getElementById('featuredGrid').innerHTML = '<div class="loading-msg">Could not load articles.</div>';
    document.getElementById('blogGrid').innerHTML = '<div class="loading-msg">Could not load articles.</div>';
  }
}

/* ---- UI actions ---- */
function filterPosts(tag, btn) {
  activeFilter = tag;
  document.querySelectorAll('.cat-pill').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderBlogGrid(tag);
}

function openModal(id) {
  const p = articles.find(x => x.id === id);
  if (!p) return;
  const tagClass = tagClasses[p.categorySlug] || 'tag-ai';
  const label = p.categoryLabel || p.categorySlug;
  const date = p.createdAt ? new Date(p.createdAt).toLocaleDateString('en-US',{month:'short',day:'numeric',year:'numeric'}) : '';
  document.getElementById('modalInner').innerHTML = `
    <span class="post-tag ${tagClass} modal-tag">${label}</span>
    <div class="modal-title">${p.title}</div>
    <div class="modal-meta">${p.author} &nbsp;·&nbsp; ${date} &nbsp;·&nbsp; ${p.readTime || ''}</div>
    <div class="modal-body">${p.content}</div>`;
  document.getElementById('modal').classList.add('open');
}

function closeModalOverlay(e) {
  if (e.target === document.getElementById('modal')) document.getElementById('modal').classList.remove('open');
}

function selectTag(btn) {
  document.querySelectorAll('.tag-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  selectedTag = btn.dataset.tag;
}

async function submitPost(e) {
  e.preventDefault();
  const btn = document.getElementById('submitBtn');
  btn.disabled = true;
  btn.textContent = 'Publishing…';

  const body = {
    title:    document.getElementById('postTitle').value.trim(),
    excerpt:  document.getElementById('postExcerpt').value.trim(),
    content:  document.getElementById('postContent').value.trim(),
    author:   document.getElementById('authorName').value.trim(),
    category: selectedTag
  };

  try {
    const resp = await fetch(`${API}/articles`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });

    if (!resp.ok) {
      const err = await resp.json();
      alert(err.message || 'Failed to publish');
      return;
    }

    // Reload articles from DB
    await loadArticles();

    e.target.reset();
    document.querySelectorAll('.tag-btn').forEach(b => b.classList.remove('active'));
    document.querySelector('.tag-btn').classList.add('active');
    selectedTag = 'ai';

    const toast = document.getElementById('toast');
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3000);
    document.getElementById('posts').scrollIntoView({ behavior: 'smooth' });
  } catch (err) {
    alert('Network error: ' + err.message);
  } finally {
    btn.disabled = false;
    btn.textContent = 'Publish Article →';
  }
}

/* ---- B&W Theme Toggle ---- */
function initThemeToggle() {
  const btn = document.getElementById('themeToggle');
  const saved = localStorage.getItem('theme');
  if (saved === 'bw') document.body.classList.add('bw');

  btn.addEventListener('click', () => {
    document.body.classList.toggle('bw');
    localStorage.setItem('theme', document.body.classList.contains('bw') ? 'bw' : 'dark');
  });
}

/* ---- Init ---- */
initThemeToggle();
loadArticles();
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') document.getElementById('modal').classList.remove('open');
});
