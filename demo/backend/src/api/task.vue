<template>
  <div class="container">
    <h1>我的备忘录</h1>
    <div class="add-task">
      <input v-model="newTaskName" placeholder="输入备忘录内容" @keyup.enter="addTask" />
      <button @click="addTask">添加</button>
    </div>
    <ul class="task-list">
      <li v-for="task in tasks" :key="task.id">
        <span :class="{ completed: task.status === 'COMPLETED' }">{{ task.name }}</span>
        <button @click="toggleComplete(task)" v-if="task.status !== 'COMPLETED'">
          完成
        </button>
        <button @click="deleteTask(task.id)">删除</button>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import taskApi from './task'

const tasks = ref([])
const newTaskName = ref('')

// 加载任务列表
const loadTasks = async () => {
  try {
    const res = await taskApi.getTasks()
    tasks.value = res.data
  } catch (error) {
    console.error('加载任务失败', error)
  }
}

// 添加任务
const addTask = async () => {
  if (!newTaskName.value.trim()) return
  try {
    await taskApi.createTask({ name: newTaskName.value, status: 'PENDING' })
    newTaskName.value = ''
    loadTasks()
  } catch (error) {
    console.error('添加失败', error)
  }
}

// 完成任务
const toggleComplete = async (task) => {
  try {
    await taskApi.updateTask(task.id, { ...task, status: 'COMPLETED' })
    loadTasks()
  } catch (error) {
    console.error('更新失败', error)
  }
}

// 删除任务
const deleteTask = async (id) => {
  try {
    await taskApi.deleteTask(id)
    loadTasks()
  } catch (error) {
    console.error('删除失败', error)
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}
.add-task {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.add-task input {
  flex: 1;
  padding: 8px;
  font-size: 16px;
}
.task-list {
  list-style: none;
  padding: 0;
}
.task-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
}
.completed {
  text-decoration: line-through;
  color: #999;
}
button {
  margin-left: 10px;
  padding: 4px 12px;
  cursor: pointer;
}
</style>