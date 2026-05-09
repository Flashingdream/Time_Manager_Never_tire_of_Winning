<template>
  <div class="navbar">
    <!-- 左侧菜单 -->
    <div class="navbar-left">
      <span>时间管理系统</span>
      <router-link to="/calendar">日历</router-link>
      <router-link to="/memorandum">添加当日事件</router-link>
      <router-link to="/event">添加备忘录</router-link>
      <router-link v-if="isAdmin" to="/user/information">用户管理</router-link>
    </div>

    <!-- 右侧用户信息 + 退出 -->
    <div class="navbar-right">
      <span>{{ username }}</span>
      <el-button type="text" @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();
const username = ref('');
const isAdmin = ref(false);

onMounted(() => {
  username.value = localStorage.getItem('username') || '用户';
  isAdmin.value = localStorage.getItem('role') === 'admin';
});

const logout = () => {
  localStorage.removeItem('isLogin');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
  ElMessage.success('退出成功！');
  router.push('/login');
};
</script>

<style scoped>
body{
  background-image: url("@/assets/images/IMG_1637.JPG");   /* 设置背景图片 */
  background-size: cover;  /* 设置背景图片覆盖整个页面 */
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background: #2c3e50;
  color: #fff;
  padding: 0 20px;
}

.navbar-left {
  display: flex;
  gap: 20px;
  align-items: center;
}

.navbar-left a {
  color: #fff;
  text-decoration: none;
}

.navbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.navbar-right button {
  color: #fff;
}
</style>