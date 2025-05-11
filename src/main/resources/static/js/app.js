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

// 追加処理
function addTaskToDOM(task) {
    const template = document.getElementById('taskTemplate');
    const clone = template.content.cloneNode(true);
    const taskDiv = clone.querySelector('.task');

    taskDiv.dataset.id = task.id;
    taskDiv.dataset.user = task.user;
    taskDiv.classList.toggle('completed', task.completed);

    clone.querySelector('.title').textContent = task.title;
    clone.querySelector('.dueDate').textContent = task.dueDate ?? 'なし';
    
    // 厳密な等価演算子を使用
    if(task.priority === 1) {
        clone.querySelector('.priority').textContent = '高';
    } else if(task.priority === 2) {
        clone.querySelector('.priority').textContent = '中';
    } else {
        clone.querySelector('.priority').textContent = '低';
    }

    taskDiv.setAttribute("draggable", "true");
    taskDiv.addEventListener("dragstart", onDragStart);

    const targetList = task.completed
        ? document.getElementById('completedTasks')
        : document.getElementById('inProgressTasks');

    targetList.appendChild(clone);
}

// 削除処理
async function deleteTask(id) {
    let deleteFlg = window.confirm('タスクを削除しますか？');
    if(deleteFlg) {
        await fetch(`/delete/${id}`, { method: 'POST' });
        document.querySelector(`.task[data-id="${id}"]`)?.remove();
    }
}

// 完了/未完了切り替え
async function toggleTask(id) {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    const response = await fetch(`/toggle/${id}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        }
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

// ドラッグ&ドロップ機能
let draggedTaskId = null;

function onDragStart(event) {
    draggedTaskId = event.target.closest('.task').dataset.id;
}

async function onDrop(event, dropToCompleted) {
    event.preventDefault();
    event.currentTarget.classList.remove('dragover');

    const task = document.querySelector(`.task[data-id="${draggedTaskId}"]`);
    const isCurrentlyCompleted = task.classList.contains('completed');

    if (isCurrentlyCompleted === dropToCompleted) return;

    const targetList = dropToCompleted
        ? document.getElementById('completedTasks')
        : document.getElementById('inProgressTasks');

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    const res = await fetch(`/toggle/${draggedTaskId}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        }
    });
    if (!res.ok) return;

    const updated = await res.json();
    task.classList.toggle('completed', updated.completed);
    task.querySelector('.toggleBtn').textContent = updated.completed ? '進行中' : '完了';
    targetList.appendChild(task);
}

// 進行中タスクの並べ替え + ドロップ先切り替え
new Sortable(document.getElementById('inProgressTasks'), {
  group: 'tasks',
  animation: 150,
  onEnd: async (evt) => {
    const id = evt.item.dataset.id;
    const from = evt.from.id;
    const to = evt.to.id;

    evt.from.classList.remove('dragover');
    evt.to.classList.remove('dragover');

    if (from !== to) {
      await fetch(`/toggle/${id}`, {
        method: 'POST',
        headers: { [csrfHeader]: csrfToken }
      });
    }
  }
});

new Sortable(document.getElementById('completedTasks'), {
  group: 'tasks',
  animation: 150,
  delay: 150,
  delayOnTouchOnly: true,
});

flatpickr("input[type='date']", {
  dateFormat: "Y-m-d",
  locale: "ja",
  clickOpens: true,
  allowInput: true
});


// イベントデリゲーションの設定
document.addEventListener('DOMContentLoaded', () => {
    // 動的要素用のイベントリスナー
    const setupDynamicListeners = (container) => {
        container.addEventListener('click', (e) => {
            const task = e.target.closest('.task');
            if (!task) return;

            if (e.target.classList.contains('deleteBtn')) {
                deleteTask(task.dataset.id);
            }
            if (e.target.classList.contains('toggleBtn')) {
                toggleTask(task.dataset.id);
            }
        });

        container.addEventListener('dragstart', (e) => {
            if (e.target.classList.contains('task')) {
                onDragStart(e);
            }
        });
    };

    // 両方のタスクリストに適用
    setupDynamicListeners(document.getElementById('inProgressTasks'));
    setupDynamicListeners(document.getElementById('completedTasks'));
});