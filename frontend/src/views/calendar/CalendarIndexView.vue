<template>
  <div class="calendar-grid-container">
    <!-- 左侧区域：日历列表 + 当日事件 -->
    <div class="calendar-left-section">
      <!-- 1. 普通日历列表（带日期选择） -->
      <ContentField>
        <div class="card-content">
          <h5>普通日历列表</h5>
          <!-- Element Plus 日期选择器（Vue3 版本） -->
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            @change="getDailyEvents"
            style="margin-bottom: 15px; width: 100%;"
          />
          <!-- 日历简单展示 -->
          <div class="calendar-tip">当前选中：{{ selectedDate || '请选择日期' }}</div>
        </div>
      </ContentField>

      <!-- 2. 当日事件（绑定选中日期，可删除） -->
      <ContentField>
        <div class="card-content">
          <h5>当日事件：</h5>
          <!-- 空状态提示 -->
          <div v-if="dailyEvents.length === 0" class="empty-tip">暂无当日事件</div>
          <!-- 事件列表：逐行展示 + 删除按钮 -->
          <div v-else class="event-list">
            <div 
              v-for="(event, index) in dailyEvents" 
              :key="event.id || index" 
              class="event-item"
            >
              <span class="event-text">{{ event.content }}</span>
              <el-button 
                type="text" 
                icon="el-icon-delete" 
                class="delete-btn"
                @click="deleteDailyEvent(event.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </ContentField>
    </div>

    <!-- 右侧区域：备忘录（独立，可删除） -->
    <div class="calendar-right-section">
      <ContentField>
        <div class="card-content">
          <h5>备忘录：</h5>
          <!-- 空状态提示 -->
          <div v-if="memos.length === 0" class="empty-tip">暂无备忘录</div>
          <!-- 备忘录列表：逐行展示 + 删除按钮 -->
          <div v-else class="memo-list">
            <div 
              v-for="(memo, index) in memos" 
              :key="memo.id || index" 
              class="memo-item"
            >
              <span class="memo-text">{{ memo.content }}</span>
              <el-button 
                type="text" 
                icon="el-icon-delete" 
                class="delete-btn"
                @click="deleteMemo(memo.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </ContentField>
    </div>
  </div>
</template>

<script setup>
// Vue3 组合式API：setup 语法糖
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus'; // Vue3 用 Element Plus 的 ElMessage
import ContentField from '@/components/ContentField.vue';
import axios from 'axios';

// 配置axios基础路径（替换为你的后端接口地址）
axios.defaults.baseURL = 'http://localhost:8080/api';

// 响应式数据（Vue3 用 ref 替代 data）
const selectedDate = ref('');
const dailyEvents = ref([]);
const memos = ref([]);

// 页面加载时初始化（替代 created 钩子）
onMounted(() => {
  // 设置默认日期为今日（格式：YYYY-MM-DD）
  selectedDate.value = new Date().toISOString().split('T')[0];
  getDailyEvents();
  getMemos();
});

/**
 * 1. 获取当日事件（修复 axios 错误处理 + Vue3 响应式）
 */
const getDailyEvents = async () => {
  if (!selectedDate.value) return;
  try {
    const res = await axios.get('/daily-events', {
      params: { date: selectedDate.value }
    });
    dailyEvents.value = res.data.data || [];
  } catch (err) {
    // axios 1.x 错误处理：兼容网络错误/接口错误
    console.error('获取当日事件失败：', err.response?.data || err.message);
    ElMessage.error('获取事件失败，请重试'); // Vue3 用 ElMessage 替代 this.$message
  }
};

/**
 * 2. 删除当日事件
 */
const deleteDailyEvent = async (eventId) => {
  if (!eventId) return;
  try {
    await axios.delete(`/daily-events/${eventId}`);
    // 过滤删除的事件
    dailyEvents.value = dailyEvents.value.filter(item => item.id !== eventId);
    ElMessage.success('事件删除成功');
  } catch (err) {
    console.error('删除当日事件失败：', err.response?.data || err.message);
    ElMessage.error('删除事件失败，请重试');
  }
};

/**
 * 3. 获取所有备忘录
 */
const getMemos = async () => {
  try {
    const res = await axios.get('/memos');
    memos.value = res.data.data || [];
  } catch (err) {
    console.error('获取备忘录失败：', err.response?.data || err.message);
    ElMessage.error('获取备忘录失败，请重试');
  }
};

/**
 * 4. 删除备忘录
 */
const deleteMemo = async (memoId) => {
  if (!memoId) return;
  try {
    await axios.delete(`/memos/${memoId}`);
    // 过滤删除的备忘录
    memos.value = memos.value.filter(item => item.id !== memoId);
    ElMessage.success('备忘录删除成功');
  } catch (err) {
    console.error('删除备忘录失败：', err.response?.data || err.message);
    ElMessage.error('删除备忘录失败，请重试');
  }
};
</script>

<style scoped>
/* 核心布局（保留） */
.calendar-grid-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 15px;
  height: calc(100vh - 80px);
  box-sizing: border-box;
}
.calendar-left-section {
  display: grid;
  grid-template-rows: 1fr 1fr;
  gap: 20px;
  height: 100%;
}
.calendar-right-section {
  height: 100%;
}
.card-content {
  padding: 10px;
  color: #333;
  height: 100%;
  box-sizing: border-box;
}
.card-content h5 {
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

/* 列表样式（逐行展示） */
.event-list, .memo-list {
  height: calc(100% - 40px);
  overflow-y: auto;
}
.event-item, .memo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #eee;
}
.event-text, .memo-text {
  flex: 1;
  word-break: break-all;
}
.delete-btn {
  color: #ff4949;
  padding: 0;
}
.delete-btn:hover {
  color: #ff1f1f;
}

/* 空状态提示 */
.empty-tip {
  color: #999;
  text-align: center;
  padding: 20px 0;
}
.calendar-tip {
  color: #666;
  font-size: 14px;
}
</style>