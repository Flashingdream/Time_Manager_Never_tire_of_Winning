import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { requestNotificationPermission, connectWebSocket } from './notification.js'

axios.defaults.baseURL = 'http://localhost:8080'

// 请求拦截器：自动附带 token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器：token 过期或无效时自动跳转登录
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('isLogin')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

const app = createApp(App).use(store).use(router).use(ElementPlus, { locale: zhCn })

requestNotificationPermission();
connectWebSocket();

app.mount('#app')
