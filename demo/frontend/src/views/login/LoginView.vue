<template>
  <div class="login-container">
    <div class="login-card">
      <h3>时间管理系统</h3>

      <!-- 登录/注册 切换 -->
      <div class="tab-switch">
        <span :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</span>
        <span :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</span>
      </div>

      <el-form :model="form" label-width="60px">
        <el-form-item label="账号">
          <el-input v-model="form.userId" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>

        <!-- 注册模式：确认密码 -->
        <el-form-item v-if="mode === 'register'" label="确认密码">
          <el-input v-model="confirmPassword" type="password" placeholder="请再次输入密码" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="loading">
            {{ mode === 'login' ? '登录' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();

const mode = ref('login');
const loading = ref(false);
const confirmPassword = ref('');

const form = ref({
  userId: '',
  password: ''
});

const switchMode = (m) => {
  mode.value = m;
  form.value.userId = '';
  form.value.password = '';
  confirmPassword.value = '';
};

const handleLogin = async () => {
  if (!form.value.userId.trim() || !form.value.password.trim()) {
    ElMessage.warning('请输入账号和密码');
    return;
  }
  try {
    const res = await axios.post('http://localhost:8080/api/users/login', {
      userId: form.value.userId.trim(),
      password: form.value.password
    });
    if (res.data.code === 200) {
      localStorage.setItem('isLogin', 'true');
      localStorage.setItem('username', res.data.data.userId);
      localStorage.setItem('role', res.data.data.role);
      ElMessage.success('登录成功！');
      router.push('/calendar');
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch {
    ElMessage.error('无法连接服务器');
  }
};

const handleRegister = async () => {
  if (!form.value.userId.trim() || !form.value.password.trim()) {
    ElMessage.warning('请输入账号和密码');
    return;
  }
  if (form.value.password !== confirmPassword.value) {
    ElMessage.warning('两次密码输入不一致');
    return;
  }
  try {
    const res = await axios.post('http://localhost:8080/api/users/register', {
      userId: form.value.userId.trim(),
      password: form.value.password
    });
    if (res.data.code === 200) {
      ElMessage.success('注册成功，请登录');
      switchMode('login');
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch {
    ElMessage.error('无法连接服务器');
  }
};

const handleSubmit = () => {
  if (mode.value === 'login') {
    handleLogin();
  } else {
    handleRegister();
  }
};
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.login-card {
  width: 380px;
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

.tab-switch {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 2px solid #eee;
}

.tab-switch span {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  cursor: pointer;
  color: #999;
  font-size: 14px;
  transition: color 0.2s;
}

.tab-switch span.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
  margin-bottom: -2px;
}

.submit-btn {
  width: 100%;
}
</style>
