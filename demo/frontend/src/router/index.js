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
    meta: { requiresAuth: true, requireAdmin: true }
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

// 路由守卫：校验本地登录态和管理员权限
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const isLogin = localStorage.getItem('isLogin') === 'true';
    if (!isLogin) {
      ElMessage.warning('请先登录！');
      next('/login');
      return;
    }
    if (to.meta.requireAdmin) {
      const role = localStorage.getItem('role');
      if (role !== 'admin') {
        ElMessage.warning('无管理员权限');
        next('/calendar');
        return;
      }
    }
    next();
  } else {
    next();
  }
});

export default router;