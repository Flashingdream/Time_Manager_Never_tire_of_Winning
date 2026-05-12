<template>
  <div class="overview-container">
    <!-- 今日时间轴 -->
    <ContentField>
      <div class="overview-content">
        <h3 class="page-title">事件总览</h3>

        <div class="today-header">
          <span class="today-label">今日</span>
          <span class="today-date">{{ todayStr }}</span>
        </div>

        <!-- 今日时间轴 -->
        <div v-if="todayEvents.length === 0" class="empty-today">
          <el-icon><Sunny /></el-icon>
          <p>今天没有特别规划</p>
        </div>
        <div v-else class="timeline">
          <div v-for="event in todayEvents" :key="event.id" class="timeline-item">
            <div class="timeline-dot" :class="tagClass(event.tag)"></div>
            <div class="timeline-card">
              <div class="tl-top">
                <span class="tl-time">{{ fmtTime(event.startTime) }} — {{ fmtTime(event.endTime) || '未定' }}</span>
                <span :class="['tag-badge', tagClass(event.tag)]">{{ event.tag || '未分类' }}</span>
              </div>
              <div class="tl-body">
                <span v-if="event.title" class="tl-title">{{ event.title }}</span>
                <span class="tl-content">{{ event.content }}</span>
              </div>
              <div class="tl-bottom" v-if="event.location">
                <el-icon><Location /></el-icon>
                <span>{{ event.location }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ContentField>

    <!-- 全部事件列表 -->
    <ContentField>
      <div class="overview-content">
        <div class="toolbar">
          <div class="search-bar">
            <el-icon class="search-icon"><Search /></el-icon>
            <input v-model="searchKeyword" placeholder="搜索事件..." class="search-input" @input="onSearch" />
          </div>
          <el-button type="primary" class="add-btn" @click="openAddDialog">
            <el-icon><Plus /></el-icon>添加事件
          </el-button>
        </div>

        <el-table :data="memoList" style="width: 100%" v-loading="loading" class="memo-table" :row-class-name="rowClass">
          <el-table-column prop="title" label="事件名称" width="140" />
          <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="标签" width="110">
            <template #default="{ row }">
              <el-popover placement="bottom" :width="200" trigger="click">
                <template #reference>
                  <span :class="['tag-badge', 'tag-clickable', tagClass(row.tag)]">{{ row.tag || '未分类' }}</span>
                </template>
                <div class="tag-picker">
                  <span v-for="t in tags" :key="t" :class="['tag-option', tagClass(t)]" @click="changeTag(row, t)">{{ t }}</span>
                </div>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="地点" width="110" />
          <el-table-column prop="startTime" label="开始时间" width="150" :formatter="fmt" />
          <el-table-column prop="endTime" label="结束时间" width="150" :formatter="fmt" />
          <el-table-column label="提醒" width="100">
            <template #default="{ row }">
              <el-popover placement="bottom" :width="160" trigger="click">
                <template #reference>
                  <span class="reminder-cell">{{ fmtReminder(row) }}</span>
                </template>
                <div class="reminder-picker">
                  <span v-for="r in reminderOptions" :key="r.value" :class="['reminder-option', { active: row.reminderOffset === r.value }]" @click="changeReminder(row, r.value)">{{ r.label }}</span>
                </div>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="150" :formatter="fmt" />
          <el-table-column label="完成" width="70" fixed="right">
            <template #default="{ row }">
              <el-checkbox :model-value="row.completed" @change="toggleComplete(row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" class="action-btn" @click="editMemo(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" class="action-btn danger" @click="deleteMemo(row.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentField>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="showDialog" width="620px" class="memo-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="事件名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入事件名称" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="标签" prop="tag">
          <el-select v-model="form.tag" placeholder="选择标签" style="width: 100%;">
            <el-option v-for="t in tags" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="提醒时间" prop="reminderOffset">
          <el-select v-model="form.reminderOffset" placeholder="选择提醒时间">
            <el-option v-for="r in reminderOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="dialog-btn" @click="showDialog = false">取消</el-button>
        <el-button type="primary" class="dialog-btn" @click="submitMemo">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Plus, Edit, Delete, Sunny, Location } from '@element-plus/icons-vue';
import axios from 'axios';
import ContentField from '@/components/ContentField.vue';


const tags = ['生活', '学习', '工作', '娱乐', '社交'];
const reminderOptions = [
  { label: '准时', value: 0 },
  { label: '5 分钟前', value: 5 },
  { label: '15 分钟前', value: 15 },
  { label: '30 分钟前', value: 30 },
];

const memoList = ref([]);
const loading = ref(false);
const showDialog = ref(false);
const dialogTitle = ref('添加事件');
const isEdit = ref(false);
const currentId = ref(null);
const searchKeyword = ref('');

const form = ref({
  title: '', content: '', tag: '生活', location: '',
  startTime: null, reminderOffset: 15, endTime: null
});

const rules = ref({
  title: [{ required: true, message: '请输入事件名称', trigger: 'blur' }, { max: 100, message: '不超过100字', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }, { max: 500, message: '不超过500字', trigger: 'blur' }],
  location: [{ max: 200, message: '不超过200字', trigger: 'blur' }]
});

const formRef = ref(null);

const todayStr = computed(() => {
  const d = new Date();
  const weekNames = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  return d.getFullYear() + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日 ' + weekNames[d.getDay()];
});

const todayKey = computed(() => {
  const d = new Date();
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
});

const todayEvents = computed(() => {
  return memoList.value.filter(m => {
    if (!m.startTime) return false;
    try {
      const t = new Date(m.startTime);
      const key = t.getFullYear() + '-' + String(t.getMonth() + 1).padStart(2, '0') + '-' + String(t.getDate()).padStart(2, '0');
      return key === todayKey.value;
    } catch { return false; }
  }).sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''));
});

const fmtTime = (t) => {
  if (!t) return '';
  try { const d = new Date(t); return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0'); }
  catch { return ''; }
};

const tagClass = (tag) => {
  const map = { '生活': 'tag-life', '学习': 'tag-study', '工作': 'tag-work', '娱乐': 'tag-fun', '社交': 'tag-social' };
  return map[tag] || 'tag-default';
};

const fmt = (row) => {
  const val = row?.startTime || row?.endTime || row?.createdAt || row?.updatedAt;
  return val ? new Date(val).toLocaleString() : '';
};

const fmtReminder = (row) => {
  const v = row.reminderOffset;
  return v === 0 ? '准时' : v + ' 分钟前';
};

let searchTimer = null;
const onSearch = () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    searchKeyword.value.trim() ? searchMemos() : fetchMemos();
  }, 300);
};

const fetchMemos = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/memos');
    if (res.data.code === 200) memoList.value = res.data.data || [];
  } catch { ElMessage.error('获取失败'); }
  finally { loading.value = false; }
};

const searchMemos = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/memos/search', { params: { keyword: searchKeyword.value.trim() } });
    if (res.data.code === 200) memoList.value = res.data.data || [];
  } catch { ElMessage.error('搜索失败'); }
  finally { loading.value = false; }
};

const rowClass = ({ row }) => {
  if (row.completed) return 'row-done';
  if (row.endTime) {
    try { if (new Date(row.endTime) < new Date()) return 'row-done'; } catch { /* skip */ }
  }
  return '';
};

const toggleComplete = async (row) => {
  try {
    const res = await axios.put(`/memos/${row.id}/toggle-complete`);
    if (res.data.code === 200) {
      row.completed = res.data.data.completed;
    }
  } catch { ElMessage.error('操作失败'); }
};

const changeTag = async (row, newTag) => {
  try {
    const payload = { ...row, tag: newTag, startTime: row.startTime || null, endTime: row.endTime || null };
    await axios.put(`/api/memos/${row.id}`, payload);
    row.tag = newTag;
    ElMessage.success('标签已更新');
  } catch { ElMessage.error('更新失败'); }
};

const changeReminder = async (row, newOffset) => {
  try {
    const payload = { ...row, reminderOffset: newOffset, startTime: row.startTime || null, endTime: row.endTime || null };
    await axios.put(`/api/memos/${row.id}`, payload);
    row.reminderOffset = newOffset;
    ElMessage.success('提醒已更新');
  } catch { ElMessage.error('更新失败'); }
};

const openAddDialog = () => {
  isEdit.value = false; currentId.value = null;
  dialogTitle.value = '添加事件';
  form.value = { title: '', content: '', tag: '生活', location: '', startTime: null, reminderOffset: 15, endTime: null };
  showDialog.value = true;
};

const editMemo = (memo) => {
  isEdit.value = true; currentId.value = memo.id;
  dialogTitle.value = '编辑事件';
  form.value = {
    title: memo.title || '', content: memo.content || '', tag: memo.tag || '生活',
    location: memo.location || '', startTime: memo.startTime || null,
    reminderOffset: memo.reminderOffset ?? 15, endTime: memo.endTime || null
  };
  showDialog.value = true;
};

const submitMemo = async () => {
  try { await formRef.value.validate(); } catch { return; }
  const payload = { ...form.value, startTime: form.value.startTime || null, endTime: form.value.endTime || null };
  try {
    const res = isEdit.value
      ? await axios.put(`/api/memos/${currentId.value}`, payload)
      : await axios.post('/api/memos', payload);
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功');
      showDialog.value = false;
      fetchMemos();
    }
  } catch { ElMessage.error('操作失败'); }
};

const deleteMemo = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此事件吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await axios.delete(`/api/memos/${id}`);
    ElMessage.success('删除成功');
    fetchMemos();
  } catch (err) { if (err !== 'cancel') ElMessage.error('删除失败'); }
};

onMounted(() => fetchMemos());
</script>

<style scoped>
.overview-container { width: 100%; max-width: 1100px; margin: 16px auto; padding: 0 16px; box-sizing: border-box; display: flex; flex-direction: column; gap: 16px; }

.overview-content { padding: 20px; }
.page-title { text-align: center; margin-bottom: 18px; color: #333; font-weight: 600; }

/* 今日头部 */
.today-header { display: flex; align-items: baseline; gap: 10px; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid rgba(0,0,0,0.06); }
.today-label { font-size: 22px; font-weight: 700; color: #333; }
.today-date { font-size: 14px; color: #888; }

/* 空状态 */
.empty-today { text-align: center; padding: 36px 0; color: #bbb; }
.empty-today .el-icon { font-size: 42px; margin-bottom: 10px; color: #ffc107; }
.empty-today p { font-size: 15px; color: #999; margin: 0; }

/* 时间轴 */
.timeline { position: relative; padding-left: 24px; }
.timeline::before { content: ''; position: absolute; left: 7px; top: 0; bottom: 0; width: 2px; background: rgba(0,0,0,0.06); border-radius: 1px; }
.timeline-item { position: relative; margin-bottom: 16px; display: flex; gap: 14px; }
.timeline-dot { width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; margin-top: 6px; position: absolute; left: -30px; border: 2px solid #fff; box-shadow: 0 0 0 2px currentColor; }
.timeline-dot.tag-life   { background: #66bb6a; color: #66bb6a; }
.timeline-dot.tag-study  { background: #42a5f5; color: #42a5f5; }
.timeline-dot.tag-work   { background: #ff9800; color: #ff9800; }
.timeline-dot.tag-fun    { background: #ef5350; color: #ef5350; }
.timeline-dot.tag-social { background: #ab47bc; color: #ab47bc; }
.timeline-dot.tag-default{ background: #bdbdbd; color: #bdbdbd; }

.timeline-card { flex: 1; background: rgba(255,255,255,0.6); border-radius: 12px; padding: 12px 15px; transition: background 0.2s, box-shadow 0.2s; }
.timeline-card:hover { background: rgba(255,255,255,0.8); box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.tl-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.tl-time { font-size: 13px; color: #666; font-weight: 500; }
.tl-body { display: flex; flex-direction: column; gap: 3px; }
.tl-title { font-size: 15px; font-weight: 600; color: #333; }
.tl-content { font-size: 13px; color: #555; line-height: 1.4; }
.tl-bottom { display: flex; align-items: center; gap: 4px; margin-top: 6px; font-size: 12px; color: #999; }

/* 工具栏 */
.toolbar { display: flex; gap: 14px; margin-bottom: 20px; align-items: center; }
.search-bar { display: flex; align-items: center; flex: 1; height: 40px; border-radius: 20px; background: rgba(241,243,244,0.7); padding: 0 16px; transition: background 0.2s, box-shadow 0.2s; }
.search-bar:focus-within { background: rgba(255,255,255,0.85); box-shadow: 0 1px 6px rgba(32,33,36,0.18); }
.search-icon { color: #999; margin-right: 8px; flex-shrink: 0; }
.search-input { flex: 1; border: none; outline: none; background: transparent; font-size: 14px; color: #333; }
.search-input::placeholder { color: #999; }
.add-btn { border-radius: 20px; display: flex; align-items: center; gap: 6px; flex-shrink: 0; }

/* 标签 */
.tag-badge { display: inline-block; padding: 3px 12px; border-radius: 10px; font-size: 12px; font-weight: 500; }
.tag-clickable { cursor: pointer; transition: transform 0.15s; }
.tag-clickable:hover { transform: scale(1.08); }
.tag-life   { background: #e8f5e9; color: #2e7d32; }
.tag-study  { background: #e3f2fd; color: #1565c0; }
.tag-work   { background: #fff3e0; color: #e65100; }
.tag-fun    { background: #fce4ec; color: #c62828; }
.tag-social { background: #f3e5f5; color: #7b1fa2; }
.tag-default{ background: #f5f5f5; color: #888; }
.tag-picker { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-option { padding: 4px 14px; border-radius: 10px; font-size: 13px; cursor: pointer; transition: transform 0.15s; }
.tag-option:hover { transform: scale(1.06); }

/* 提醒 */
.reminder-cell { padding: 2px 10px; border-radius: 8px; font-size: 12px; color: #555; background: rgba(0,0,0,0.03); cursor: pointer; transition: background 0.2s; }
.reminder-cell:hover { background: rgba(0,0,0,0.07); }
.reminder-picker { display: flex; flex-direction: column; gap: 4px; }
.reminder-option { padding: 6px 12px; border-radius: 8px; font-size: 13px; cursor: pointer; transition: background 0.15s; }
.reminder-option:hover { background: #f0f4ff; }
.reminder-option.active { background: #e8f0fe; color: #1a73e8; font-weight: 500; }

.action-btn { border-radius: 8px; width: 34px; height: 34px; padding: 0; }
.action-btn.danger { color: #f56c6c; }

/* 已完成/过期行 */
:deep(.row-done) { background: rgba(180,180,180,0.25); }
:deep(.row-done td) { color: #999; }
:deep(.row-done .cell) { text-decoration: line-through; }

.dialog-btn { border-radius: 10px; }

:deep(.el-button) { border-radius: 10px; transition: all 0.2s; }
:deep(.el-button:hover) { transform: translateY(-1px); }
:deep(.el-button:active) { transform: scale(0.97); }
:deep(.el-table) { border-radius: 12px; overflow: hidden; background: rgba(255,255,255,0.5); }
:deep(.el-table th) { background: rgba(245,247,250,0.6); }
:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-select .el-input__wrapper) { border-radius: 10px; }
:deep(.el-input__wrapper) { border-radius: 10px; }
:deep(.el-textarea__inner) { border-radius: 10px; }
:deep(.el-date-editor.el-input) { border-radius: 10px; }
</style>
