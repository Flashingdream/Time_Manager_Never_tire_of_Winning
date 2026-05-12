<template>
  <div class="calendar-page" @wheel.passive="onWheel">
    <!-- 顶部搜索栏 -->
    <div class="search-bar-wrapper">
      <div class="search-bar">
        <el-icon class="search-icon"><Search /></el-icon>
        <input v-model="searchKeyword" placeholder="搜索备忘录标题或内容..." class="search-input" @input="onSearch" />
      </div>
    </div>

    <div class="calendar-grid-container">
      <!-- 左侧：缩小版日历 -->
      <div class="calendar-left-section">
        <ContentField>
          <div class="card-content cal-card">
            <h5>日历</h5>
            <el-calendar v-model="selectedDate" class="small-calendar">
              <template #date-cell="{ data }">
                <div class="date-cell" :class="{ today: isToday(data.date), selected: isSelected(data.date) }">
                  <span class="day-num">{{ data.date.getDate() }}</span>
                  <!-- 节假日标记 -->
                  <span v-if="getHoliday(data.date)" class="holiday-dot" :class="getHoliday(data.date).type">
                    {{ getHoliday(data.date).name }}
                  </span>
                  <!-- 事件圆点 -->
                  <span v-if="hasEvents(data.date)" class="event-dot"></span>
                </div>
              </template>
            </el-calendar>
            <div class="calendar-tip">当前选中：{{ formatDateStr(selectedDate) }}</div>
          </div>
        </ContentField>
      </div>

      <!-- 右侧：备忘录列表 -->
      <div class="calendar-right-section">
        <ContentField>
          <div class="card-content memo-panel">
            <h5>备忘录</h5>
            <div v-if="filteredMemos.length === 0" class="empty-tip">暂无备忘录</div>
            <div v-else class="memo-list">
              <div v-for="memo in filteredMemos" :key="memo.id" :class="['memo-item', { 'is-done': isCompletedOrExpired(memo) }]">
                <div class="memo-main">
                  <span class="memo-text">{{ memo.content }}</span>
                  <span v-if="memo.title" class="memo-title-tag">{{ memo.title }}</span>
                </div>
                <div class="memo-meta">
                  <el-popover placement="bottom" :width="200" trigger="click">
                    <template #reference>
                      <span :class="['tag-badge', 'tag-clickable', tagClass(memo.tag)]">{{ memo.tag || '未分类' }}</span>
                    </template>
                    <div class="tag-picker">
                      <span v-for="t in tags" :key="t" :class="['tag-option', tagClass(t)]" @click="changeTag(memo, t)">{{ t }}</span>
                    </div>
                  </el-popover>
                  <el-popover placement="bottom" :width="160" trigger="click">
                    <template #reference>
                      <span class="reminder-badge">{{ fmtReminderInline(memo.reminderOffset) }}</span>
                    </template>
                    <div class="reminder-picker">
                      <span v-for="r in reminderOptions" :key="r.value" :class="['reminder-option', { active: memo.reminderOffset === r.value }]" @click="changeReminder(memo, r.value)">{{ r.label }}</span>
                    </div>
                  </el-popover>
                  <span class="memo-time" v-if="memo.createdAt">{{ memo.createdAt }}</span>
                </div>
                <el-checkbox
                  class="memo-checkbox"
                  :model-value="memo.completed"
                  @change="toggleComplete(memo)"
                />
                <el-button class="memo-del-btn" @click="deleteMemo(memo.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </ContentField>
      </div>
    </div>

    <!-- 底部快捷添加栏 -->
    <div class="quick-add-bar">
      <el-select v-model="quickTag" class="tag-select" size="large">
        <el-option v-for="t in tags" :key="t" :label="t" :value="t" />
      </el-select>
      <input v-model="quickContent" placeholder="输入备忘录内容，按回车添加..." class="quick-input" @keyup.enter="quickAddMemo" />
      <el-button class="quick-add-btn" @click="quickAddMemo" :disabled="!quickContent.trim()">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, Delete, Plus } from '@element-plus/icons-vue';
import ContentField from '@/components/ContentField.vue';
import axios from 'axios';


const tags = ['生活', '学习', '工作', '娱乐', '社交'];
const reminderOptions = [
  { label: '准时', value: 0 },
  { label: '5 分钟前', value: 5 },
  { label: '15 分钟前', value: 15 },
  { label: '30 分钟前', value: 30 },
];

// 2026年中国法定节假日
const holidays = {
  '2026-01-01': { name: '元旦', type: 'holiday' },
  '2026-01-02': { name: '元旦', type: 'holiday' },
  '2026-01-03': { name: '元旦', type: 'holiday' },
  '2026-02-16': { name: '除夕', type: 'holiday' },
  '2026-02-17': { name: '春节', type: 'holiday' },
  '2026-02-18': { name: '春节', type: 'holiday' },
  '2026-02-19': { name: '春节', type: 'holiday' },
  '2026-02-20': { name: '春节', type: 'holiday' },
  '2026-02-21': { name: '春节', type: 'holiday' },
  '2026-02-22': { name: '春节', type: 'holiday' },
  '2026-02-23': { name: '春节', type: 'holiday' },
  '2026-04-04': { name: '清明', type: 'holiday' },
  '2026-04-05': { name: '清明', type: 'holiday' },
  '2026-04-06': { name: '清明', type: 'holiday' },
  '2026-05-01': { name: '劳动节', type: 'holiday' },
  '2026-05-02': { name: '劳动节', type: 'holiday' },
  '2026-05-03': { name: '劳动节', type: 'holiday' },
  '2026-05-04': { name: '劳动节', type: 'holiday' },
  '2026-05-05': { name: '劳动节', type: 'holiday' },
  '2026-06-19': { name: '端午', type: 'holiday' },
  '2026-06-20': { name: '端午', type: 'holiday' },
  '2026-06-21': { name: '端午', type: 'holiday' },
  '2026-09-25': { name: '中秋', type: 'holiday' },
  '2026-09-26': { name: '中秋', type: 'holiday' },
  '2026-09-27': { name: '中秋', type: 'holiday' },
  '2026-10-01': { name: '国庆', type: 'holiday' },
  '2026-10-02': { name: '国庆', type: 'holiday' },
  '2026-10-03': { name: '国庆', type: 'holiday' },
  '2026-10-04': { name: '国庆', type: 'holiday' },
  '2026-10-05': { name: '国庆', type: 'holiday' },
  '2026-10-06': { name: '国庆', type: 'holiday' },
  '2026-10-07': { name: '国庆', type: 'holiday' },
};

const selectedDate = ref(new Date());
const memos = ref([]);
const searchKeyword = ref('');
const quickContent = ref('');
const quickTag = ref('生活');

const formatDateStr = (d) => {
  if (!d) return '';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return y + '年' + m + '月' + day + '日';
};

const toDateKey = (d) => {
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
};

const isToday = (d) => toDateKey(d) === toDateKey(new Date());
const isSelected = (d) => toDateKey(d) === toDateKey(selectedDate.value);

const getHoliday = (d) => holidays[toDateKey(d)] || null;

const eventDates = computed(() => {
  const set = new Set();
  memos.value.forEach(m => {
    if (m.startTime) {
      try {
        const t = new Date(m.startTime);
        set.add(toDateKey(t));
      } catch { /* skip */ }
    }
  });
  return set;
});

const hasEvents = (d) => eventDates.value.has(toDateKey(d));

const tagClass = (tag) => {
  const map = { '生活': 'tag-life', '学习': 'tag-study', '工作': 'tag-work', '娱乐': 'tag-fun', '社交': 'tag-social' };
  return map[tag] || 'tag-default';
};

const fmtReminderInline = (v) => v === 0 ? '准时' : '提前' + v + '分钟';

const filteredMemos = computed(() => {
  if (!searchKeyword.value.trim()) return memos.value;
  const kw = searchKeyword.value.trim().toLowerCase();
  return memos.value.filter(m =>
    (m.title && m.title.toLowerCase().includes(kw)) ||
    (m.content && m.content.toLowerCase().includes(kw)) ||
    (m.tag && m.tag.toLowerCase().includes(kw))
  );
});

let searchTimer = null;
const onSearch = () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    searchKeyword.value.trim() ? fetchSearchResults() : fetchMemos();
  }, 300);
};

// 鼠标滚轮切换月份
let wheelTimer = null;
const onWheel = (e) => {
  if (!selectedDate.value) return;
  clearTimeout(wheelTimer);
  wheelTimer = setTimeout(() => {
    const d = new Date(selectedDate.value);
    if (e.deltaY > 0) {
      d.setMonth(d.getMonth() + 1);
    } else {
      d.setMonth(d.getMonth() - 1);
    }
    selectedDate.value = d;
  }, 80);
};

const fetchMemos = async () => {
  try {
    const res = await axios.get('/api/memos');
    if (res.data.code === 200) memos.value = res.data.data || [];
  } catch { /* offline */ }
};

const fetchSearchResults = async () => {
  try {
    const res = await axios.get('/api/memos/search', { params: { keyword: searchKeyword.value.trim() } });
    if (res.data.code === 200) memos.value = res.data.data || [];
  } catch { /* offline */ }
};

const changeTag = async (memo, newTag) => {
  try {
    await axios.put(`/api/memos/${memo.id}`, { ...memo, tag: newTag, startTime: memo.startTime || null, endTime: memo.endTime || null });
    memo.tag = newTag;
    ElMessage.success('标签已更新');
  } catch { ElMessage.error('更新失败'); }
};

const changeReminder = async (memo, newOffset) => {
  try {
    await axios.put(`/api/memos/${memo.id}`, { ...memo, reminderOffset: newOffset, startTime: memo.startTime || null, endTime: memo.endTime || null });
    memo.reminderOffset = newOffset;
    ElMessage.success('提醒已更新');
  } catch { ElMessage.error('更新失败'); }
};

const isCompletedOrExpired = (memo) => {
  if (memo.completed) return true;
  if (memo.endTime) {
    try { return new Date(memo.endTime) < new Date(); } catch { return false; }
  }
  return false;
};

const toggleComplete = async (memo) => {
  try {
    const res = await axios.put(`/api/memos/${memo.id}/toggle-complete`);
    if (res.data.code === 200) {
      memo.completed = res.data.data.completed;
    }
  } catch { ElMessage.error('操作失败'); }
};

const deleteMemo = async (id) => {
  if (!id) return;
  try {
    await axios.delete(`/api/memos/${id}`);
    memos.value = memos.value.filter(m => m.id !== id);
    ElMessage.success('删除成功');
  } catch { ElMessage.error('删除失败'); }
};

const quickAddMemo = async () => {
  if (!quickContent.value.trim()) return;
  try {
    const res = await axios.post('/api/memos', { content: quickContent.value.trim(), tag: quickTag.value });
    if (res.data.code === 200) {
      memos.value.unshift(res.data.data);
      quickContent.value = '';
      ElMessage.success('添加成功');
    }
  } catch { ElMessage.error('添加失败'); }
};

onMounted(() => fetchMemos());
</script>

<style scoped>
.calendar-page { display: flex; flex-direction: column; height: calc(100vh - 60px); overflow: hidden; }

.search-bar-wrapper { padding: 10px 20px; display: flex; justify-content: center; }
.search-bar { display: flex; align-items: center; width: 100%; max-width: 640px; height: 40px; border-radius: 20px; background: rgba(241,243,244,0.8); padding: 0 16px; transition: background 0.2s, box-shadow 0.2s; }
.search-bar:focus-within { background: rgba(255,255,255,0.9); box-shadow: 0 1px 6px rgba(32,33,36,0.18); }
.search-icon { color: #999; font-size: 17px; margin-right: 8px; flex-shrink: 0; }
.search-input { flex: 1; border: none; outline: none; background: transparent; font-size: 14px; color: #333; }
.search-input::placeholder { color: #999; }

/* 主体网格：左侧缩小 */
.calendar-grid-container { display: grid; grid-template-columns: 420px 1fr; gap: 16px; padding: 0 20px; flex: 1; min-height: 0; }
.calendar-left-section { min-height: 0; overflow: hidden; max-width: 420px; }
.calendar-right-section { min-height: 0; overflow: hidden; }
.card-content { padding: 10px; height: 100%; box-sizing: border-box; display: flex; flex-direction: column; }
.card-content h5 { margin-bottom: 10px; padding-bottom: 6px; border-bottom: 1px solid rgba(0,0,0,0.06); flex-shrink: 0; color: #444; font-size: 14px; }
.memo-panel { overflow: hidden; }

/* 缩小日历 */
.small-calendar { --el-calendar-cell-width: 42px; }
:deep(.small-calendar .el-calendar__header) { padding: 6px 8px; }
:deep(.small-calendar .el-calendar__title) { font-size: 14px; }
:deep(.small-calendar .el-calendar__body) { padding: 4px; }
:deep(.small-calendar .el-calendar-table) { table-layout: fixed; }
:deep(.small-calendar .el-calendar-table td) { padding: 1px; }
:deep(.small-calendar .el-calendar-table th) { padding: 4px 0; font-size: 12px; }

/* 日期单元格 */
.date-cell { position: relative; display: flex; flex-direction: column; align-items: center; padding: 4px 2px; min-height: 42px; }
.date-cell.today .day-num { background: #409eff; color: #fff; border-radius: 50%; width: 24px; height: 24px; line-height: 24px; text-align: center; font-size: 13px; }
.date-cell.selected:not(.today) .day-num { background: rgba(64,158,255,0.15); border-radius: 50%; width: 24px; height: 24px; line-height: 24px; text-align: center; }
.day-num { font-size: 13px; color: #555; }

/* 事件圆点 */
.event-dot { width: 5px; height: 5px; border-radius: 50%; background: #409eff; margin-top: 1px; flex-shrink: 0; }

/* 节假日 */
.holiday-dot { font-size: 9px; padding: 0 2px; border-radius: 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 36px; line-height: 1.2; }
.holiday-dot.holiday { color: #e53935; }

/* 备忘录列表 */
.memo-list { flex: 1; overflow-y: auto; min-height: 0; }
.memo-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 12px; margin-bottom: 6px; background: rgba(255,255,255,0.55); transition: background 0.2s, transform 0.15s, box-shadow 0.2s; }
.memo-item:hover { background: rgba(240,244,255,0.7); transform: translateY(-1px); box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.memo-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.memo-text { font-size: 14px; color: #333; word-break: break-all; line-height: 1.4; }
.memo-title-tag { font-size: 11px; color: #888; }
.memo-meta { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }
.memo-time { font-size: 11px; color: #aaa; white-space: nowrap; }
.memo-checkbox { flex-shrink: 0; }
.memo-del-btn { flex-shrink: 0; opacity: 0; transition: opacity 0.2s; border-radius: 8px; width: 28px; height: 28px; padding: 0; }
.memo-item:hover .memo-del-btn { opacity: 1; }

/* 已完成/过期 */
.memo-item.is-done { background: rgba(180,180,180,0.3); }
.memo-item.is-done .memo-text { text-decoration: line-through; color: #999; }
.memo-item.is-done .memo-title-tag { text-decoration: line-through; }

/* 标签 */
.tag-badge { display: inline-block; padding: 2px 9px; border-radius: 10px; font-size: 11px; font-weight: 500; white-space: nowrap; }
.tag-clickable { cursor: pointer; transition: transform 0.15s; }
.tag-clickable:hover { transform: scale(1.08); }
.tag-life   { background: #e8f5e9; color: #2e7d32; }
.tag-study  { background: #e3f2fd; color: #1565c0; }
.tag-work   { background: #fff3e0; color: #e65100; }
.tag-fun    { background: #fce4ec; color: #c62828; }
.tag-social { background: #f3e5f5; color: #7b1fa2; }
.tag-default{ background: #f5f5f5; color: #888; }
.tag-picker { display: flex; flex-wrap: wrap; gap: 6px; }
.tag-option { padding: 3px 12px; border-radius: 10px; font-size: 12px; cursor: pointer; transition: transform 0.15s; }
.tag-option:hover { transform: scale(1.06); }

/* 提醒 */
.reminder-badge { display: inline-block; padding: 2px 7px; border-radius: 6px; font-size: 11px; color: #555; background: rgba(0,0,0,0.04); cursor: pointer; white-space: nowrap; transition: background 0.2s; }
.reminder-badge:hover { background: rgba(0,0,0,0.08); }
.reminder-picker { display: flex; flex-direction: column; gap: 3px; }
.reminder-option { padding: 5px 10px; border-radius: 8px; font-size: 12px; cursor: pointer; transition: background 0.15s; }
.reminder-option:hover { background: #f0f4ff; }
.reminder-option.active { background: #e8f0fe; color: #1a73e8; font-weight: 500; }

/* 日历卡片 */
.calendar-tip { color: #999; font-size: 12px; margin-top: 6px; text-align: center; flex-shrink: 0; }
:deep(.small-calendar) { border: 1px solid rgba(0,0,0,0.05); border-radius: 12px; background: rgba(255,255,255,0.6); }

/* 底部栏 */
.quick-add-bar { display: flex; align-items: center; gap: 10px; padding: 10px 20px; border-top: 1px solid rgba(0,0,0,0.05); background: rgba(255,255,255,0.55); backdrop-filter: blur(8px); flex-shrink: 0; }
.tag-select { width: 90px; flex-shrink: 0; }
.quick-input { flex: 1; height: 38px; border: 1px solid rgba(0,0,0,0.08); border-radius: 19px; padding: 0 16px; font-size: 14px; outline: none; background: rgba(255,255,255,0.6); transition: border-color 0.2s, box-shadow 0.2s; }
.quick-input:focus { border-color: #409eff; box-shadow: 0 0 0 2px rgba(64,158,255,0.12); }
.quick-add-btn { width: 38px; height: 38px; border-radius: 19px; padding: 0; flex-shrink: 0; }

.empty-tip { color: #999; text-align: center; padding: 40px 0; }

:deep(.el-button) { border-radius: 10px; transition: all 0.2s; }
:deep(.el-button:hover) { transform: translateY(-1px); }
:deep(.el-button:active) { transform: scale(0.97); }
</style>
