import { createRouter, createWebHistory } from 'vue-router';

import CalendarIndexView from '@/views/calendar/CalendarIndexView.vue';
import MemorandumIndexView from '@/views/memorandum/MemorandumIndexView.vue';
import EventIndexView from '@/views/event/EventIndexView.vue';
import UserInformationIndexView from '@/views/user/information/UserInformationIndexView.vue';
import LoginView from '@/views/login/LoginView.vue'; 
import { ElMessage } from 'element-plus';

const routes = [
  {
    path: '/',
    redirect: '/login' // 默认跳登录页
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: CalendarIndexView,
    meta: { requiresAuth: true } // 需要登录才能访问
  },
  {
    path: '/memorandum',
    name: 'Memorandum',
    component: MemorandumIndexView,
    meta: { requiresAuth: true }
  },
  {
    path: '/event',
    name: 'Event',
    component: EventIndexView,
    meta: { requiresAuth: true }
  },
  {
    path: '/user/information',
    name: 'UserInformation',
    component: UserInformationIndexView,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

// 极简路由守卫：仅校验本地登录态
router.beforeEach((to, from, next) => {
  // 判断页面是否需要登录
  if (to.meta.requiresAuth) {
    // 读取本地登录标记
    const isLogin = localStorage.getItem('isLogin') === 'true';
    if (isLogin) {
      next(); // 已登录，放行
    } else {
      ElMessage.warning('请先登录！');
      next('/login'); // 未登录，跳登录页
    }
  } else {
    next(); // 无需登录，直接放行
  }
});

export default router;