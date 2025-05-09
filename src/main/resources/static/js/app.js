// フォームの非同期送信
document.getElementById('taskForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const response = await fetch('/add', {
        method: 'POST',
        body: new URLSearchParams(formData)
    });

    if (response.ok) {
        const newTask = await response.json();
        addTaskToDOM(newTask); // DOMに追加
        e.target.reset(); // フォームクリア
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
    toggleBtn.textContent = task.completed ? 'Undo' : 'Complete';
    toggleBtn.onclick = () => toggleTask(task.id);

    document.getElementById('taskList').appendChild(clone);
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
}

