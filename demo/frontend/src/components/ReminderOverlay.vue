<template>
  <Teleport to="body">
    <div v-if="visible" class="reminder-overlay" @click="dismiss" @keydown.esc="dismiss">
      <div class="reminder-card">
        <div class="reminder-icon">🔔</div>
        <h2 class="reminder-title">{{ event.title || '事件提醒' }}</h2>
        <p class="reminder-content">{{ event.content }}</p>
        <div class="reminder-meta">
          <span v-if="event.tag" :class="['tag-badge', tagClass(event.tag)]">{{ event.tag }}</span>
          <span v-if="event.location" class="reminder-location">
            <el-icon><Location /></el-icon> {{ event.location }}
          </span>
          <span v-if="event.startTime" class="reminder-time">{{ event.startTime }}</span>
        </div>
        <div class="countdown-bar">
          <div class="countdown-fill" :style="{ width: (countdown / 15 * 100) + '%' }"></div>
        </div>
        <p class="dismiss-hint">{{ countdown }} 秒后自动关闭 · 点击或按 Esc 退出</p>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { Location } from '@element-plus/icons-vue';

const visible = ref(false);
const event = ref({});
const countdown = ref(15);
let countdownTimer = null;

const tagClass = (tag) => {
  const map = { '生活': 'tag-life', '学习': 'tag-study', '工作': 'tag-work', '娱乐': 'tag-fun', '社交': 'tag-social' };
  return map[tag] || 'tag-default';
};

const showReminder = (data) => {
  event.value = data;
  countdown.value = 15;
  visible.value = true;

  // 尝试全屏
  try {
    if (document.documentElement.requestFullscreen) {
      document.documentElement.requestFullscreen();
    }
  } catch { /* noop */ }

  // 倒计时
  countdownTimer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      dismiss();
    }
  }, 1000);
};

const dismiss = () => {
  clearInterval(countdownTimer);
  visible.value = false;
  try {
    if (document.fullscreenElement) {
      document.exitFullscreen();
    }
  } catch { /* noop */ }
};

const onKeyDown = (e) => {
  if (e.key === 'Escape' && visible.value) {
    e.preventDefault();
    dismiss();
  }
};

const onReminderMessage = (e) => {
  try {
    const data = JSON.parse(e.detail);
    showReminder(data);
  } catch {
    showReminder({ title: '事件提醒', content: e.detail });
  }
};

onMounted(() => {
  document.addEventListener('keydown', onKeyDown);
  window.addEventListener('reminder', onReminderMessage);
});

onUnmounted(() => {
  clearInterval(countdownTimer);
  document.removeEventListener('keydown', onKeyDown);
  window.removeEventListener('reminder', onReminderMessage);
});
</script>

<style scoped>
.reminder-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: rgba(15, 15, 26, 0.92);
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
  cursor: pointer;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.reminder-card {
  text-align: center;
  color: #fff;
  max-width: 480px;
  padding: 40px;
  user-select: none;
}

.reminder-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.reminder-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px;
}

.reminder-content {
  font-size: 18px;
  color: rgba(255,255,255,0.7);
  margin: 0 0 20px;
  line-height: 1.5;
}

.reminder-meta {
  display: flex;
  gap: 12px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 28px;
}

.reminder-location,
.reminder-time {
  font-size: 13px;
  color: rgba(255,255,255,0.5);
  display: flex;
  align-items: center;
  gap: 4px;
}

.tag-badge {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
}

.tag-life   { background: #66bb6a; color: #fff; }
.tag-study  { background: #42a5f5; color: #fff; }
.tag-work   { background: #ff9800; color: #fff; }
.tag-fun    { background: #ef5350; color: #fff; }
.tag-social { background: #ab47bc; color: #fff; }
.tag-default{ background: #757575; color: #fff; }

/* 倒计时进度条 */
.countdown-bar {
  width: 200px;
  height: 4px;
  background: rgba(255,255,255,0.15);
  border-radius: 2px;
  margin: 0 auto 16px;
  overflow: hidden;
}

.countdown-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 2px;
  transition: width 1s linear;
}

.dismiss-hint {
  font-size: 12px;
  color: rgba(255,255,255,0.25);
  margin: 0;
}
</style>
