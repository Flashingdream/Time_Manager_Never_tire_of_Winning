<template>
  <div class="navbar">
    <div class="navbar-left">
      <span>时间管理系统</span>
      <router-link to="/calendar">日历</router-link>
      <router-link to="/memorandum">事件总览</router-link>
      <router-link to="/event">添加备忘录</router-link>
      <router-link to="/focus">专注</router-link>
      <router-link v-if="isAdmin" to="/user/information">用户管理</router-link>

      <!-- 考试倒计时 -->
      <span v-if="examCountdown" class="exam-badge">
        <el-icon><AlarmClock /></el-icon>
        {{ examCountdown }}
      </span>
    </div>

    <div class="navbar-right">
      <!-- 设置按钮 -->
      <el-popover placement="bottom-end" :width="360" trigger="click" v-model:visible="settingVisible">
        <template #reference>
          <el-button class="settings-btn" circle>
            <el-icon><Setting /></el-icon>
          </el-button>
        </template>

        <div class="settings-panel">
          <!-- 头像 -->
          <div class="setting-section">
            <span class="setting-label">用户头像</span>
            <div class="avatar-row">
              <img :src="avatarUrl" class="avatar-preview" @click="triggerAvatar" />
              <input type="file" ref="avatarInput" accept="image/*" style="display:none" @change="onAvatarChange" />
              <el-button size="small" class="setting-action" @click="triggerAvatar">选择图片</el-button>
              <el-button v-if="avatarUrl !== defaultAvatar" size="small" class="setting-action" @click="resetAvatar">重置</el-button>
            </div>
          </div>

          <!-- 背景图 -->
          <div class="setting-section">
            <span class="setting-label">背景图片</span>
            <div class="bg-row">
              <input type="file" ref="bgInput" accept="image/*" style="display:none" @change="onBgChange" />
              <el-button size="small" class="setting-action" @click="triggerBg">选择背景</el-button>
              <el-button v-if="bgUrl" size="small" class="setting-action" @click="resetBg">重置背景</el-button>
            </div>
          </div>

          <!-- 透明度 -->
          <div class="setting-section">
            <span class="setting-label">背景透明度</span>
            <el-slider v-model="bgOpacity" :min="10" :max="100" :step="5" show-input size="small" @input="onOpacityChange" />
          </div>
        </div>
      </el-popover>

      <!-- 用户头像小图标 -->
      <img :src="avatarUrl" class="user-avatar" />

      <span class="username">{{ username }}</span>
      <el-button type="text" class="logout-btn" @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Setting, AlarmClock } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const username = ref('');
const isAdmin = ref(false);
const settingVisible = ref(false);

const avatarInput = ref(null);
const bgInput = ref(null);
const examCountdown = ref('');

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#c8d6e5"/><circle cx="20" cy="15" r="7" fill="#fff"/><ellipse cx="20" cy="32" rx="13" ry="9" fill="#fff"/></svg>');
const avatarUrl = ref(defaultAvatar);
const bgUrl = ref('');
const bgOpacity = ref(70);

const applyBackground = () => {
  const overlay = document.querySelector('.bg-overlay');
  if (!overlay) return;
  if (bgUrl.value) {
    overlay.style.backgroundImage = `url(${bgUrl.value})`;
  } else {
    overlay.style.backgroundImage = '';
  }
  overlay.style.opacity = (bgOpacity.value / 100).toString();
};

const triggerAvatar = () => {
  avatarInput.value?.click();
};

const triggerBg = () => {
  bgInput.value?.click();
};

const onAvatarChange = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (ev) => {
    avatarUrl.value = ev.target.result;
    localStorage.setItem('avatar', ev.target.result);
  };
  reader.readAsDataURL(file);
};

const resetAvatar = () => {
  avatarUrl.value = defaultAvatar;
  localStorage.removeItem('avatar');
};

const onBgChange = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (ev) => {
    bgUrl.value = ev.target.result;
    localStorage.setItem('bgImage', ev.target.result);
    applyBackground();
  };
  reader.readAsDataURL(file);
};

const resetBg = () => {
  bgUrl.value = '';
  localStorage.removeItem('bgImage');
  const overlay = document.querySelector('.bg-overlay');
  if (overlay) overlay.style.backgroundImage = '';
};

const onOpacityChange = (val) => {
  localStorage.setItem('bgOpacity', val.toString());
  applyBackground();
};

onMounted(() => {
  username.value = localStorage.getItem('username') || '用户';
  isAdmin.value = localStorage.getItem('role') === 'admin';

  const savedAvatar = localStorage.getItem('avatar');
  if (savedAvatar) avatarUrl.value = savedAvatar;

  const savedBg = localStorage.getItem('bgImage');
  if (savedBg) {
    bgUrl.value = savedBg;
    applyBackground();
  }

  const savedOpacity = localStorage.getItem('bgOpacity');
  if (savedOpacity) {
    bgOpacity.value = parseInt(savedOpacity);
    applyBackground();
  }

  fetchExamCountdown();
});

const fetchExamCountdown = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/memos');
    if (res.data.code === 200) {
      const memos = res.data.data || [];
      const now = new Date();
      const exams = memos.filter(m => {
        const text = (m.title || '') + ' ' + (m.content || '');
        const match = /考试|test/i.test(text);
        if (!match) return false;
        try {
          const d = m.startTime ? new Date(m.startTime) : null;
          return d && d > now;
        } catch { return false; }
      }).sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''));
      if (exams.length > 0) {
        const first = exams[0];
        const d = new Date(first.startTime);
        const days = Math.ceil((d - now) / (1000 * 60 * 60 * 24));
        examCountdown.value = (first.title || '考试') + ' 还有 ' + days + ' 天';
      }
    }
  } catch { /* network error ignored */ }
};

const logout = () => {
  localStorage.removeItem('isLogin');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
  ElMessage.success('退出成功！');
  router.push('/login');
};
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background: rgba(44, 62, 80, 0.92);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #fff;
  padding: 0 20px;
  position: relative;
  z-index: 100;
}

.navbar-left {
  display: flex;
  gap: 20px;
  align-items: center;
}

.navbar-left a {
  color: #fff;
  text-decoration: none;
  transition: opacity 0.2s;
}
.navbar-left a:hover {
  opacity: 0.8;
}

/* 考试倒计时 */
.exam-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  background: rgba(255, 82, 82, 0.3);
  color: #ffcdd2;
  font-weight: 500;
  animation: examPulse 2s infinite;
}
@keyframes examPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.navbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.username {
  font-size: 14px;
  opacity: 0.9;
}

.settings-btn {
  width: 34px;
  height: 34px;
  padding: 0;
  color: #fff;
  border-color: rgba(255,255,255,0.3);
  background: transparent;
}
.settings-btn:hover {
  border-color: rgba(255,255,255,0.7);
  background: rgba(255,255,255,0.1);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255,255,255,0.4);
  cursor: pointer;
  transition: border-color 0.2s;
}
.user-avatar:hover {
  border-color: rgba(255,255,255,0.8);
}

.logout-btn {
  color: rgba(255,255,255,0.8);
}
.logout-btn:hover {
  color: #fff;
}

/* 设置面板 */
.settings-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.setting-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.setting-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-preview {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e4e7ed;
  cursor: pointer;
  transition: border-color 0.2s;
}
.avatar-preview:hover {
  border-color: #409eff;
}

.bg-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.setting-action {
  border-radius: 8px;
}

/* 全局 input 圆角 */
:deep(.el-input__wrapper) {
  border-radius: 10px;
}
</style>
