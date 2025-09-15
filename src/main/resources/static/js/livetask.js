// ===== Utils =====
const $  = (s, r=document) => r.querySelector(s);
const $$ = (s, r=document) => Array.from(r.querySelectorAll(s));
const CSRF = { token:$('meta[name="_csrf"]')?.content||'', header:$('meta[name="_csrf_header"]')?.content||'' };

const jsonFetch = async (url,opt={})=>{
  const res = await fetch(url,opt);
  if(!res.ok) throw new Error(`${opt.method||'GET'} ${url} -> ${res.status}`);
  const ct = res.headers.get('content-type')||'';
  return ct.includes('application/json') ? res.json() : res.text();
};
const toISO = s => (s&&s.trim()) ? new Date(s).toISOString() : '';
const fmt = raw => !raw ? 'None' : new Date(raw).toLocaleString('en-US',{year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',hour12:false});
const prLabel = p => p===1?'High':p===2?'Medium':'Low';
const prClass = p => p===1?'high':p===2?'medium':'low';

// ===== Render =====
function renderTask(t){
  const n = $('#taskTemplate').content.firstElementChild.cloneNode(true);
  n.dataset.id = t.id;
  if(t.completed){ n.classList.add('completed'); $('.toggleBtn',n).textContent='In Progress'; }
  $('.title',n).textContent = t.title;
  $('.dueText',n).textContent = (t.dueDateFrom||t.dueDateTo)
    ? [t.dueDateFrom?fmt(t.dueDateFrom):null, t.dueDateTo?fmt(t.dueDateTo):null].filter(Boolean).join(' – ')
    : 'None';
  const pill = $('.priority-pill', n);
  pill.textContent = prLabel(t.priority);
  pill.classList.add(prClass(t.priority));

  $('.deleteBtn', n).addEventListener('click', ()=>delTask(t.id));
  $('.toggleBtn', n).addEventListener('click', ()=>toggleTask(t.id));
  return n;
}
function mountTasks(tasks){
  const ip = $('#inProgressTasks'), cp = $('#completedTasks');
  ip.innerHTML = cp.innerHTML = '';
  tasks.forEach(t => (t.completed?cp:ip).appendChild(renderTask(t)));
  toggleCompletedEmpty();
}
function appendTask(t){
  (t.completed?$('#completedTasks'):$('#inProgressTasks')).appendChild(renderTask(t));
  toggleCompletedEmpty();
}
function toggleCompletedEmpty(){
  const empty = !$('#completedTasks').children.length;
  $('#completedEmpty').style.display = empty ? '' : 'none';
}

// ===== Server actions =====
async function delTask(id){
  if(!confirm('Delete this task?')) return;
  const res = await fetch(`/delete/${id}`, { method:'POST', headers:{ [CSRF.header]: CSRF.token } });
  if(res.ok){ $(`.task[data-id="${id}"]`)?.remove(); toggleCompletedEmpty(); }
  else alert('Failed to delete.');
}
async function toggleTask(id){
  const card = $(`.task[data-id="${id}"]`);
  const toCompleted = !card.classList.contains('completed');
  // optimistic UI
  card.classList.toggle('completed', toCompleted);
  $('.toggleBtn', card).textContent = toCompleted? 'In Progress' : 'Done';
  (toCompleted?$('#completedTasks'):$('#inProgressTasks')).appendChild(card);
  toggleCompletedEmpty();
  try{
    const updated = await jsonFetch(`/toggle/${id}`, { method:'POST', headers:{ [CSRF.header]: CSRF.token } });
    if(updated?.completed && updated?.message) showPraise(updated.message);
  }catch(e){ console.error(e); }
}

// ===== Sort =====
async function applySort(){
  const key = $('#sortBy').value;        // 'dueDate' | 'priority' | 'created'
  const desc = $('#sortDir').getAttribute('aria-pressed')==='true';
  try{
    const tasks = await jsonFetch('/tasks/sort', {
      method:'POST',
      headers:{ 'Content-Type':'application/json', [CSRF.header]: CSRF.token },
      body: JSON.stringify({ tags:[key], desc })
    });
    if(Array.isArray(tasks)) mountTasks(tasks);
  }catch(e){ console.error('sort failed', e); }
}

// ===== Drag & Drop (toggle by moving between columns) =====
const initSortable = el => new Sortable(el, {
  group:'tasks', animation:150, delay:120, delayOnTouchOnly:true, touchStartThreshold:5,
  filter:'.deleteBtn, .toggleBtn', preventOnFilter:false,
  onEnd: async (evt)=>{ if(evt.from.id!==evt.to.id){ await toggleTask(evt.item.dataset.id); } }
});

// ===== Modal / Menu / Toast =====
const modal = $('#taskModal');
const openModal  = ()=>{ modal.hidden=false; $('#title').focus(); };
const closeModal = ()=>{ modal.hidden=true; $('#taskForm').reset(); fpFrom?.clear(); fpTo?.clear(); $('#formErrors').innerHTML=''; };

let toastLock=false;
function showPraise(text){ if(toastLock) return; toastLock=true;
  const el=document.createElement('div'); el.className='praise-message'; el.textContent=text;
  document.body.appendChild(el); setTimeout(()=>{el.remove(); toastLock=false;}, 8000);
}

// ===== Boot =====
document.addEventListener('DOMContentLoaded', ()=>{
  // user menu
  const menu = $('#userMenu');
  $('#userMenuBtn').addEventListener('click', ()=>{
    const open = menu.classList.toggle('open');
    $('#userMenuBtn').setAttribute('aria-expanded', String(open));
  });
  document.addEventListener('click',(e)=>{ if(!menu.contains(e.target)) menu.classList.remove('open'); });

  // modal
  $('#openNewTask').addEventListener('click', openModal);
  $('#closeModal').addEventListener('click', closeModal);
  $('#modalBackdrop').addEventListener('click', closeModal);

  // hydrate from SSR
  const ssrTasks = $$('#ssr [data-json]').map(n=>JSON.parse(n.getAttribute('data-json')));
  if(ssrTasks.length) mountTasks(ssrTasks);

  // Sort UI
  $('#sortBy').addEventListener('change', applySort);
  $('#sortDir').addEventListener('click', (e)=>{
    const on = e.currentTarget.getAttribute('aria-pressed')==='true';
    e.currentTarget.setAttribute('aria-pressed', String(!on));
    applySort();
  });

  // Sortable
  initSortable($('#inProgressTasks'));
  initSortable($('#completedTasks'));
});

// ===== Form submit (modal) =====
$('#taskForm').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const err = $('#formErrors'); err.innerHTML='';
  const title = $('#title').value.trim();
  const from  = $('#dueDateFrom').value.trim();
  const to    = $('#dueDateTo').value.trim();
  const priority = Number($('#priority').value||3);

  if(from && to && new Date(from)>new Date(to)){
    err.textContent='The end time must be after the start time.'; return;
  }
  const params = new URLSearchParams({ title, priority:String(priority) });
  if(from) params.append('dueDateFrom', toISO(from));
  if(to)   params.append('dueDateTo',   toISO(to));

  try{
    const res = await fetch('/add', {
      method:'POST',
      headers:{ [CSRF.header]: CSRF.token, 'Content-Type':'application/x-www-form-urlencoded' },
      body: params
    });
    if(res.ok){ const t = await res.json(); appendTask(t); closeModal(); }
    else{ const errors = await res.json().catch(()=>({error:'Failed to add the task.'})); err.textContent = Object.values(errors).join(' '); }
  }catch(ex){ err.textContent = 'Failed to add the task. Please try again.'; }
});

// ===== flatpickr =====
const now = new Date();
const opts = { dateFormat:'m/d/Y H:i', locale:'en', clickOpens:true, allowInput:true, disableMobile:true, enableTime:true, time_24hr:true, defaultHour:now.getHours(), defaultMinute:now.getMinutes() };
const fpFrom = flatpickr('#dueDateFrom', opts);
const fpTo   = flatpickr('#dueDateTo',   opts);

$('#dueDateFrom').addEventListener('change', (e)=>{ const v=e.target.value?new Date(e.target.value):null; fpTo.set('minDate', v||null); });
$('#dueDateTo').addEventListener('change',   (e)=>{ const v=e.target.value?new Date(e.target.value):null; fpFrom.set('maxDate', v||null); });
$('#copyFromTo').addEventListener('click', ()=>{
  const v=$('#dueDateFrom').value; if(v){ $('#dueDateTo').value=v; fpTo.setDate(v,true); fpFrom.set('maxDate',new Date(v)); fpTo.set('minDate',new Date(v)); }
});
