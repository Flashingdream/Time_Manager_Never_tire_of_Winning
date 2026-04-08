<template>
  <div class="simple-login-container">
    <div class="login-card">
      <h3>时间管理系统 - 登录</h3>
      <el-form :model="loginForm" @submit.prevent="handleLogin" label-width="60px">
        <el-form-item label="账号">
          <el-input v-model="loginForm.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

// 1. 登录表单数据
const loginForm = ref({
  username: '',
  password: ''
});

// 2. 极简登录逻辑（本地固定账号密码）
const handleLogin = () => {
  // 本地模拟校验：固定账号admin，密码123456
  if (loginForm.value.username === 'admin' && loginForm.value.password === '123456') {
    // 存储登录态（本地localStorage）
    localStorage.setItem('isLogin', 'true'); // 标记已登录
    localStorage.setItem('username', loginForm.value.username); // 存储用户名
    ElMessage.success('登录成功！');
    router.push('/calendar'); // 跳转到日历首页
  } else {
    ElMessage.error('账号或密码错误（默认：admin/123456）');
  }
};
</script>

<style scoped>
/* 极简样式，保证页面居中即可 */
.simple-login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.login-card {
  width: 350px;
  padding: 30px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.login-card h3 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.login-btn {
  width: 100%;
}
</style>