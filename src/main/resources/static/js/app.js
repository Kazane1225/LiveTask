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

// タスクの動的追加
function addTaskToDOM(task) {
    const taskList = document.getElementById('taskList');
    const taskDiv = document.createElement('div');
    taskDiv.className = 'task';
    taskDiv.innerHTML = `
        <span>${task.title}</span>
        <span>${task.dueDate}</span>
        <button onclick="deleteTask(${task.id})">Delete</button>
        <button onclick="toggleTask(${task.id})">
            ${task.completed ? 'Undo' : 'Complete'}
        </button>
    `;
    taskList.appendChild(taskDiv);
}

// 削除処理
async function deleteTask(id) {
    await fetch(`/delete/${id}`, { method: 'POST' });
    document.querySelector(`.task[data-id="${id}"]`).remove();
}

// 完了/未完了切り替え
async function toggleTask(id) {
    await fetch(`/toggle/${id}`, { method: 'POST' });
    const task = document.querySelector(`.task[data-id="${id}"]`);
    task.classList.toggle('completed');
    task.querySelector('button:nth-child(3)').textContent = 
        task.classList.contains('completed') ? 'Undo' : 'Complete';
}