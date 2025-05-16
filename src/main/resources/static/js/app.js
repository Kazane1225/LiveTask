// フォームの非同期送信
document.getElementById('taskForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const formData = new FormData(e.target);
  const errorContainer = document.getElementById('formErrors');
  errorContainer.innerHTML = '';

  const response = await fetch('/add', {
    method: 'POST',
    body: new URLSearchParams(formData)
  });

  if (response.ok) {
    const newTask = await response.json();
    addTaskToDOM(newTask);
    e.target.reset();
  } else {
    const errors = await response.json();
    for (const key in errors) {
      const p = document.createElement('p');
      p.textContent = errors[key];
      errorContainer.appendChild(p);
    }
  }
});

// タスクをDOMに追加
function addTaskToDOM(task) {
  const template = document.getElementById('taskTemplate');
  const clone = template.content.cloneNode(true);
  const taskDiv = clone.querySelector('.task');

  taskDiv.dataset.id = task.id;
  taskDiv.classList.toggle('completed', task.completed);

  const titleEl = clone.querySelector('.title');
  const dueDateEl = clone.querySelector('.dueDate');
  const priorityEl = clone.querySelector('.priority');

  if (!titleEl || !dueDateEl || !priorityEl) return;

  titleEl.textContent = task.title;
  dueDateEl.textContent = task.dueDate ?? 'なし';

  const priorityText = task.priority === 1 ? '高' : task.priority === 2 ? '中' : '低';
  priorityEl.setAttribute('data-priority', priorityText);

  taskDiv.setAttribute('draggable', 'true');

  const targetList = task.completed
    ? document.getElementById('completedTasks')
    : document.getElementById('inProgressTasks');

  targetList.appendChild(clone);
}

// タスク削除
async function deleteTask(id) {
  if (!window.confirm('タスクを削除しますか？')) return;

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  const response = await fetch(`/delete/${id}`, {
    method: 'POST',
    headers: {
      [csrfHeader]: csrfToken
    }
  });

  console.log('削除レスポンス:', response.status);

  if (response.ok) {
    const el = document.querySelector(`.task[data-id="${id}"]`);
    if (el) el.remove();
  } else {
    alert('削除に失敗しました。');
  }
}

// タスクの状態切り替え
async function toggleTask(id) {
  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  const response = await fetch(`/toggle/${id}`, {
    method: 'POST',
    headers: { [csrfHeader]: csrfToken }
  });
  if (!response.ok) return;

  const updatedTask = await response.json();
  const task = document.querySelector(`.task[data-id="${id}"]`);

  task.classList.toggle('completed', updatedTask.completed);
  const toggleBtn = task.querySelector('.toggleBtn');
  toggleBtn.textContent = updatedTask.completed ? '進行中' : '完了';

  const targetList = updatedTask.completed
    ? document.getElementById('completedTasks')
    : document.getElementById('inProgressTasks');

  targetList.appendChild(task);
}

// ドラッグ後の処理共通化
async function handleTaskDropToggle(evt) {
  const id = evt.item.dataset.id;
  const from = evt.from.id;
  const to = evt.to.id;
  if (from === to) return;

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  const response = await fetch(`/toggle/${id}`, {
    method: 'POST',
    headers: { [csrfHeader]: csrfToken }
  });
  if (!response.ok) return;

  const updated = await response.json();
  const task = evt.item;
  task.classList.toggle('completed', updated.completed);

  const toggleBtn = task.querySelector('.toggleBtn');
  toggleBtn.textContent = updated.completed ? '進行中' : '完了';
}

// Sortable.js 初期化
new Sortable(document.getElementById('inProgressTasks'), {
  group: 'tasks',
  animation: 150,
  onEnd: handleTaskDropToggle,
  filter: '.deleteBtn, .toggleBtn',
  preventOnFilter: false  
});

new Sortable(document.getElementById('completedTasks'), {
  group: 'tasks',
  animation: 150,
  delay: 150,
  delayOnTouchOnly: true,
  touchStartThreshold: 5,
  onEnd: handleTaskDropToggle,
  filter: '.deleteBtn, .toggleBtn',
  preventOnFilter: false  
});

// カレンダーUI
flatpickr("input[name='dueDate']", {
  dateFormat: 'Y-m-d',
  locale: 'ja',
  clickOpens: true,
  allowInput: true,
  disableMobile: true
});

// ボタン操作のイベント委任
document.addEventListener('DOMContentLoaded', () => {
  const setupDynamicListeners = (container) => {
    container.addEventListener('click', async (e) => {
      const task = e.target.closest('.task');
      if (!task) return;

      if (e.target.classList.contains('deleteBtn')) {
        e.target.disabled = true; // 二重実行防止
        await deleteTask(task.dataset.id);
        e.target.disabled = false;
      }

      if (e.target.classList.contains('toggleBtn')) {
        await toggleTask(task.dataset.id);
      }
    });
  };

  setupDynamicListeners(document.getElementById('inProgressTasks'));
  setupDynamicListeners(document.getElementById('completedTasks'));
});
