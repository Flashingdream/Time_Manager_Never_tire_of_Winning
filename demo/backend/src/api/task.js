import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api', // 代理后指向后端
  headers: {
    'Content-Type': 'application/json'
  }
})

export default {
  getTasks() {
    return apiClient.get('/tasks')
  },
  createTask(task) {
    return apiClient.post('/tasks', task)
  },
  updateTask(id, task) {
    return apiClient.put(`/tasks/${id}`, task)
  },
  async getTasks() {
    return { data: [{ id: 1, name: '示例备忘录', status: 'TODO' }] }
  },
  deleteTask(id) {
    return apiClient.delete(`/tasks/${id}`)
  }
}