import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { requestNotificationPermission, connectWebSocket } from './notification.js'

const app = createApp(App).use(store).use(router).use(ElementPlus)

// 请求通知权限并连接WebSocket
requestNotificationPermission();
connectWebSocket();

app.mount('#app')
