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
    await fetch(`/toggle/${id}`, { method: 'POST' });
    const task = document.querySelector(`.task[data-id="${id}"]`);
    task.classList.toggle('completed');
    task.querySelector('button:nth-child(3)').textContent = 
        task.classList.contains('completed') ? 'Undo' : 'Complete';
}