const POSTS = [
  {
    id:1, tag:'llm', tagLabel:'LLMs', tagClass:'tag-llm',
    title:'Building Production-Ready LLM Pipelines with LangChain & FastAPI',
    excerpt:'A practical guide to structuring async LLM workflows — from prompt engineering to streaming responses and error recovery in real-world deployments.',
    author:'Ravi Mehta', date:'Feb 18, 2025', readTime:'9 min read', featured:true,
    content:`<p>LangChain has become the go-to framework for chaining LLM calls, but moving from a Jupyter notebook prototype to a scalable production service introduces a new class of challenges.</p>
<p>In this guide, we'll walk through setting up a FastAPI backend that exposes streaming LLM endpoints, using <code>AsyncOpenAI</code> with proper retry logic, timeout handling, and structured output parsing via Pydantic V2.</p>
<p>The architecture we'll build supports 400+ concurrent users on a single 4-core instance — far beyond what naive synchronous implementations can handle. Key topics: LangChain chain structuring for testability, Server-Sent Events for streaming UX, cost tracking and token budgeting per request.</p>`
  },
  {
    id:2, tag:'rpa', tagLabel:'RPA', tagClass:'tag-rpa',
    title:'UiPath + AI: Combining Classical RPA with Vision Models',
    excerpt:'Extending UiPath workflows with GPT-4o Vision to handle unstructured document automation that rule-based bots consistently fail at.',
    author:'Priya Nair', date:'Feb 14, 2025', readTime:'7 min read', featured:true,
    content:`<p>Traditional RPA excels at deterministic, structured workflows. But the moment you encounter a handwritten invoice, an inconsistently formatted PDF, or a dynamic web UI — rule-based automation breaks down.</p>
<p>By integrating GPT-4o Vision as an "intelligent pre-processor" inside a UiPath activity, we can classify documents, extract fields, and route them to the right downstream bot — all without hardcoded selectors.</p>
<p>The key insight: treat AI as a <code>perception layer</code> and RPA as the <code>action layer</code>. The bot sees with AI, acts with RPA. This hybrid approach reduces maintenance costs by 60% in our production deployment.</p>`
  },
  {
    id:3, tag:'devops', tagLabel:'MLOps', tagClass:'tag-devops',
    title:'MLflow + Prefect: Orchestrating ML Pipelines at Scale',
    excerpt:'How to version, track, and schedule ML experiments using the MLflow-Prefect combo without losing your mind.',
    author:'Arjun Kapoor', date:'Feb 10, 2025', readTime:'11 min read', featured:false,
    content:`<p>MLOps tooling has matured rapidly. Two tools that pair especially well are MLflow for experiment tracking and Prefect 2 for workflow orchestration.</p>
<p>In this deep-dive, we cover registering models programmatically with <code>mlflow.register_model()</code>, setting up Prefect Deployments with CRON schedules, and wiring both together via shared artifact stores on S3.</p>
<p>We also tackle the common failure modes: stale model versions in production, pipeline drift, and how to set up alerting when model performance degrades beyond acceptable thresholds.</p>`
  },
  {
    id:4, tag:'ai', tagLabel:'AI / ML', tagClass:'tag-ai',
    title:'Agentic AI in 2025: What Actually Works in Enterprise Settings',
    excerpt:'Separating hype from reality — which agent frameworks are production-proven, and which are still science projects.',
    author:'Sneha Rao', date:'Feb 6, 2025', readTime:'8 min read', featured:false,
    content:`<p>AI agents went from research curiosity to enterprise mandate in under 18 months. But "agent" means radically different things to different vendors.</p>
<p>After deploying agents in three Fortune 500 environments, here's what I've learned: <code>ReAct-style</code> agents with strict tool guardrails outperform unconstrained agents by 3x on task completion rate. Planning reliability, not raw intelligence, is the bottleneck.</p>
<p>Frameworks worth using in production today: LangGraph for stateful multi-step agents, CrewAI for role-based multi-agent teams, and bare-metal function-calling when you need maximum control.</p>`
  },
  {
    id:5, tag:'ml', tagLabel:'Computer Vision', tagClass:'tag-ml',
    title:'Real-Time Defect Detection on Assembly Lines with YOLOv9',
    excerpt:'Deploying a custom YOLOv9 model at 60fps on edge hardware — from data collection to ONNX export.',
    author:'Dev Anand', date:'Jan 30, 2025', readTime:'14 min read', featured:false,
    content:`<p>Manufacturing defect detection is one of computer vision's most impactful applications. With YOLOv9 and a modest edge device, you can build a system that outperforms human visual inspection on many defect types.</p>
<p>We'll cover dataset curation with Roboflow, training with mixed precision on a single A100, and exporting to <code>ONNX</code> for inference on NVIDIA Jetson Orin. The final model runs at 58fps with 94.7% mAP@0.5, catching micro-fractures invisible to the human eye.</p>`
  },
  {
    id:6, tag:'llm', tagLabel:'LLMs', tagClass:'tag-llm',
    title:'Fine-Tuning Mistral 7B for Domain-Specific Automation Tasks',
    excerpt:'A cost-effective approach to adapting open-source LLMs using QLoRA and synthetic data generation.',
    author:'Kavya Sharma', date:'Jan 25, 2025', readTime:'12 min read', featured:false,
    content:`<p>You don't always need GPT-4. For many automation tasks — intent classification, entity extraction, structured output generation — a fine-tuned 7B model running locally will match or exceed GPT-4-mini at a fraction of the cost.</p>
<p>Using QLoRA with 4-bit quantization, we fine-tune Mistral 7B on a synthetic dataset of automation scripts, achieving 89% task accuracy while fitting on a single 24GB GPU. Total training cost: under $12 on Lambda Cloud. Inference: 45ms on CPU, 8ms on GPU.</p>`
  }
];

const tagClasses = {ai:'tag-ai',ml:'tag-ml',rpa:'tag-rpa',llm:'tag-llm',devops:'tag-devops'};
const tagLabels = {ai:'AI / ML',ml:'Computer Vision',rpa:'RPA',llm:'LLMs',devops:'MLOps'};

let activeFilter = 'all';
let selectedTag = 'ai';

function postCard(p, big) {
  return `<div class="${big?'featured-post':'blog-card'}" onclick="openModal(${p.id})">
    <span class="post-tag ${p.tagClass}">${p.tagLabel}</span>
    <div class="post-title">${p.title}</div>
    <div class="post-excerpt">${p.excerpt}</div>
    <div class="post-meta">
      <span class="author">${p.author}</span>
      <span class="sep">·</span>
      <span>${p.date}</span>
      <span class="read-time">⏱ ${p.readTime}</span>
    </div>
  </div>`;
}

function renderFeatured() {
  const featured = POSTS.filter(p=>p.featured);
  const rest = POSTS.filter(p=>!p.featured).slice(0,2);
  document.getElementById('featuredGrid').innerHTML = [featured[0],...rest].map((p,i)=>postCard(p,true)).join('');
}

function renderBlogGrid(tag) {
  const posts = tag==='all' ? POSTS : POSTS.filter(p=>p.tag===tag);
  const grid = document.getElementById('blogGrid');
  if (!posts.length) { grid.innerHTML=`<div style="padding:3rem;color:var(--muted);font-family:'Space Mono',monospace;font-size:0.8rem;">No articles in this category yet.</div>`; return; }
  grid.innerHTML = posts.map(p=>postCard(p,false)).join('');
}

function filterPosts(tag, btn) {
  activeFilter = tag;
  document.querySelectorAll('.cat-pill').forEach(b=>b.classList.remove('active'));
  btn.classList.add('active');
  renderBlogGrid(tag);
}

function openModal(id) {
  const p = POSTS.find(x=>x.id===id);
  if (!p) return;
  document.getElementById('modalInner').innerHTML = `
    <span class="post-tag ${p.tagClass} modal-tag">${p.tagLabel}</span>
    <div class="modal-title">${p.title}</div>
    <div class="modal-meta">${p.author} &nbsp;·&nbsp; ${p.date} &nbsp;·&nbsp; ${p.readTime}</div>
    <div class="modal-body">${p.content}</div>`;
  document.getElementById('modal').classList.add('open');
}

function closeModalOverlay(e) {
  if (e.target===document.getElementById('modal')) document.getElementById('modal').classList.remove('open');
}

function selectTag(btn) {
  document.querySelectorAll('.tag-btn').forEach(b=>b.classList.remove('active'));
  btn.classList.add('active');
  selectedTag = btn.dataset.tag;
}

function submitPost(e) {
  e.preventDefault();
  const title = document.getElementById('postTitle').value.trim();
  const excerpt = document.getElementById('postExcerpt').value.trim();
  const content = document.getElementById('postContent').value.trim();
  const author = document.getElementById('authorName').value.trim();

  const newPost = {
    id: Date.now(),
    tag: selectedTag,
    tagLabel: tagLabels[selectedTag] || selectedTag,
    tagClass: tagClasses[selectedTag] || 'tag-ai',
    title, excerpt, author,
    date: new Date().toLocaleDateString('en-US',{month:'short',day:'numeric',year:'numeric'}),
    readTime: `${Math.max(2,Math.ceil(content.split(' ').length/200))} min read`,
    featured: false,
    content: '<p>'+content.replace(/\n\n/g,'</p><p>').replace(/\n/g,'<br>')+'</p>'
  };

  POSTS.unshift(newPost);
  renderFeatured();
  renderBlogGrid(activeFilter);
  renderTicker();
  document.getElementById('postCount').textContent = POSTS.length;

  e.target.reset();
  document.querySelectorAll('.tag-btn').forEach(b=>b.classList.remove('active'));
  document.querySelector('.tag-btn').classList.add('active');
  selectedTag = 'ai';

  const toast = document.getElementById('toast');
  toast.classList.add('show');
  setTimeout(()=>toast.classList.remove('show'), 3000);

  document.getElementById('posts').scrollIntoView({behavior:'smooth'});
}

function renderTicker() {
  const items = POSTS.slice(0,8).map(p=>`<div class="ticker-item"><span class="dot">●</span> ${p.title}</div>`).join('');
  document.getElementById('ticker').innerHTML = items + items;
}

function animateCounter(el, target, duration) {
  let start=0, step=Math.ceil(target/(duration/16));
  const t = setInterval(()=>{ start=Math.min(start+step,target); el.textContent=start; if(start>=target)clearInterval(t); },16);
}

renderFeatured();
renderBlogGrid('all');
renderTicker();
setTimeout(()=>animateCounter(document.getElementById('postCount'), POSTS.length, 800), 600);
document.addEventListener('keydown', e=>{ if(e.key==='Escape') document.getElementById('modal').classList.remove('open'); });

