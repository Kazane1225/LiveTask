document.getElementById('taskForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const title = document.querySelector("input[name='title']").value.trim();
  const dueDateRaw = document.querySelector("input[name='dueDate']").value;
  const priority = document.querySelector("select[name='priority']").value;
  const errorContainer = document.getElementById('formErrors');
  errorContainer.innerHTML = '';

  const utcISOString = dueDateRaw
    ? new Date(dueDateRaw).toISOString()
    : '';

  const params = new URLSearchParams();
  params.append("title", title);
  params.append("dueDate", utcISOString);
  params.append("priority", priority);

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  const response = await fetch('/add', {
    method: 'POST',
    headers: {
      [csrfHeader]: csrfToken,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: params
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
  const dueDateEl = clone.querySelector('.dueDate span');
  const priorityEl = clone.querySelector('.priority');

  if (!titleEl || !dueDateEl || !priorityEl) return;

  titleEl.textContent = task.title;
  dueDateEl.textContent = task.dueDate ? formatDueDate(task.dueDate) : 'None';

  const priorityText = task.priority === 1 ? 'High' : task.priority === 2 ? 'Medium' : 'Low';
  priorityEl.setAttribute('data-priority', priorityText);

  taskDiv.setAttribute('draggable', 'true');

  const targetList = task.completed
    ? document.getElementById('completedTasks')
    : document.getElementById('inProgressTasks');

  targetList.appendChild(clone);
}

function formatDueDate(raw) {
  if (!raw) return 'None';
  const date = new Date(raw); // UTC→ローカル自動変換

  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
  });
}

// タスク削除
async function deleteTask(id) {
  if (!window.confirm('Are you sure you want to delete this task?')) return;

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
    alert('Failed to delete the task.');
  }
}

// タスクの状態切り替え
async function toggleTask(id) {
  const task = document.querySelector(`.task[data-id="${id}"]`);
  if (!task) return;

  // ✅ 先に見た目だけ切り替え
  const isNowCompleted = !task.classList.contains('completed');
  task.classList.toggle('completed', isNowCompleted);

  const toggleBtn = task.querySelector('.toggleBtn');
  toggleBtn.textContent = isNowCompleted ? 'In Progress' : 'Done';

  const targetList = isNowCompleted
    ? document.getElementById('completedTasks')
    : document.getElementById('inProgressTasks');

  targetList.appendChild(task);

  // ChatGPTメッセージなどは後から
  fetchToggleAndPraise(id);
}

async function fetchToggleAndPraise(id) {
  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  try {
    const response = await fetch(`/toggle/${id}`, {
      method: 'POST',
      headers: { [csrfHeader]: csrfToken }
    });

    if (!response.ok) return;

    const updatedTask = await response.json();

    // 🎉 褒め言葉があれば表示
    if (updatedTask.completed && updatedTask.message) {
      console.log("ほめる");
      showPraiseMessage(updatedTask.message);
    }
  } catch (err) {
    console.error('toggle失敗:', err);
  }
}

// ドラッグ後の処理共通化
async function handleTaskDropToggle(evt) {
  const id = evt.item.dataset.id;
  const from = evt.from.id;
  const to = evt.to.id;
  if (from === to) return;

  // 先にUI更新
  const task = evt.item;
  task.classList.toggle('completed');

  const toggleBtn = task.querySelector('.toggleBtn');
  toggleBtn.textContent = task.classList.contains('completed') ? 'In Progress' : 'Done';
  fetchToggleAndPraise(id);
}

function updateTaskLists(tasks) {
  const inProgress = document.getElementById('inProgressTasks');
  const completed = document.getElementById('completedTasks');
  inProgress.innerHTML = '';
  completed.innerHTML = '';

  tasks.forEach(task => {
    const template = document.getElementById('taskTemplate');
    const clone = template.content.cloneNode(true);
    const taskDiv = clone.querySelector('.task');

    taskDiv.dataset.id = task.id;
    taskDiv.classList.toggle('completed', task.completed);
    clone.querySelector('.title').textContent = task.title;
    clone.querySelector('.dueDate span').textContent = task.dueDate ? formatDueDate(task.dueDate) : 'None';

    const priorityText = task.priority === 1 ? 'High' : task.priority === 2 ? 'Medium' : 'Low';
    const priorityEl = clone.querySelector('.priority');
    priorityEl.setAttribute('data-priority', priorityText);
    priorityEl.textContent = '';

    if (task.completed) {
      completed.appendChild(clone);
    } else {
      inProgress.appendChild(clone);
    }
  });
}

// Sortable.js 初期化
new Sortable(document.getElementById('inProgressTasks'), {
  group: 'tasks',
  animation: 150,
  delay: 120,
  delayOnTouchOnly: true,
  touchStartThreshold: 5,
  onEnd: handleTaskDropToggle,
  filter: '.deleteBtn, .toggleBtn',
  preventOnFilter: false  
});

new Sortable(document.getElementById('completedTasks'), {
  group: 'tasks',
  animation: 150,
  delay: 120,
  delayOnTouchOnly: true,
  touchStartThreshold: 5,
  onEnd: handleTaskDropToggle,
  filter: '.deleteBtn, .toggleBtn',
  preventOnFilter: false  
});

const now = new Date();
// カレンダーUI
flatpickr("input[name='dueDate']", {
  dateFormat: "m/d/Y h:i K",
  locale: 'en',
  clickOpens: true,
  allowInput: true,
  disableMobile: true,
  enableTime: true,
  time_24hr: true,
  defaultDate: null,
  defaultHour: now.getHours(),
  defaultMinute: now.getMinutes(),
  minDate: "today"
});

let praiseMessageDisplayed = false;
function showPraiseMessage(text) {
  if (praiseMessageDisplayed) return;
  praiseMessageDisplayed = true;

  const box = document.createElement('div');
  box.className = 'praise-message';
  box.textContent = text;
  document.body.appendChild(box);

  setTimeout(() => {
    box.remove();
    praiseMessageDisplayed = false;
  }, 10000);
}

let listenersInitialized = false;
// ボタン操作のイベント委任
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.dueDate span').forEach(el => {
    if (el.textContent.includes('T')) {
      const raw = el.textContent.trim();
      el.textContent = formatDueDate(raw);
    }
  })
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

      if (e.target.classList.contains('task-checkboxes')) {
        await taskCheckBoxes(task.dataset.id);
      }
    });
  };

  document.querySelectorAll('.task-checkboxes').forEach(form => {
    form.addEventListener('change', async (e) => {
      const formData = new FormData(form);
      const tags = formData.getAll('tags');

      const response = await fetch('/tasks/sort', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [document.querySelector('meta[name="_csrf_header"]').content]:
            document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({tags})
      });

      const tasks = await response.json();
      updateTaskLists(tasks);
    })
  })

  if (!listenersInitialized) {
    setupDynamicListeners(document.getElementById('inProgressTasks'));
    setupDynamicListeners(document.getElementById('completedTasks'));
    listenersInitialized = true;
  }
});
