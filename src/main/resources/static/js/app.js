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
        const errors = await response.json(); // {"title": "...", "priority": "..."}
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
    if (task.completed) taskDiv.classList.add('completed');

    clone.querySelector('.title').textContent = task.title;
    clone.querySelector('.dueDate').textContent = task.dueDate ?? 'なし';
    if(task.priority == 1) {
        clone.querySelector('.priority').textContent = '高'
    } else if(task.priority == 2) {
        clone.querySelector('.priority').textContent = '中'
    } else if(task.priority == 3) {
        clone.querySelector('.priority').textContent = '低'
    }

    const deleteBtn = clone.querySelector('.deleteBtn');
    deleteBtn.onclick = () => deleteTask(task.id);

    const toggleBtn = clone.querySelector('.toggleBtn');
    toggleBtn.textContent = task.completed ? '進行中' : '完了';
    toggleBtn.onclick = () => toggleTask(task.id);

    const targetList = task.completed
    ? document.getElementById('completedTasks')
    : document.getElementById('inProgressTasks');

    targetList.appendChild(clone);
}

// 削除処理
async function deleteTask(id) {
    await fetch(`/delete/${id}`, { method: 'POST' });
    let deleteFlg = window.confirm('タスクを削除しますか？');
    if(deleteFlg) {
        document.querySelector(`.task[data-id="${id}"]`).remove();
    }
}

// 完了/未完了切り替え
async function toggleTask(id) {
    const response = await fetch(`/toggle/${id}`, { method: 'POST' });
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

let draggedTaskId = null;

function onDragStart(event) {
  draggedTaskId = event.target.dataset.id;
}

function onDragOver(event) {
  event.preventDefault();
  event.currentTarget.classList.add('dragover');
}

async function onDrop(event, dropToCompleted) {
  event.preventDefault();
  event.currentTarget.classList.remove('dragover');

  const task = document.querySelector(`.task[data-id="${draggedTaskId}"]`);
  const isCurrentlyCompleted = task.classList.contains('completed');

  // 同じ状態のリストにドロップされた場合は何もしない
  if (isCurrentlyCompleted === dropToCompleted) {
    return;
  }

  const targetList = dropToCompleted
    ? document.getElementById('completedTasks')
    : document.getElementById('inProgressTasks');

  const res = await fetch(`/toggle/${draggedTaskId}`, { method: 'POST' });
  if (!res.ok) return;

  const updated = await res.json();
  task.classList.toggle('completed', updated.completed);
  task.querySelector('.toggleBtn').textContent = updated.completed ? '進行中' : '完了';

  targetList.appendChild(task);
  location.reload();
}


