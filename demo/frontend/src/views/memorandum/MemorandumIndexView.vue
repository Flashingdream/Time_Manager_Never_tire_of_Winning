<template>
  <div class="memo-container">
    <!-- 复用公共卡片组件，保持风格统一 -->
    <ContentField>
      <div class="memo-content">
        <!-- 页面标题 -->
        <h3 class="page-title">备忘录管理</h3>

        <!-- 添加备忘录按钮 -->
        <div class="add-btn-container">
          <el-button type="primary" @click="showAddDialog = true">添加备忘录</el-button>
        </div>

        <!-- 备忘录列表 -->
        <el-table :data="memoList" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="事件名称" width="150"></el-table-column>
          <el-table-column prop="content" label="内容" width="300"></el-table-column>
          <el-table-column prop="location" label="地点" width="150"></el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="180" :formatter="formatDate"></el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180" :formatter="formatDate"></el-table-column>
          <el-table-column label="提醒时间" width="120" :formatter="formatReminderOffset"></el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" :formatter="formatDate"></el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180" :formatter="formatDate"></el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="mini" @click="editMemo(scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="deleteMemo(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 添加/编辑对话框 -->
        <el-dialog :title="dialogTitle" v-model="showAddDialog" width="600px">
          <el-form :model="memoForm" :rules="memoRules" ref="memoFormRef" label-width="100px">
            <el-form-item label="事件名称" prop="title">
              <el-input
                v-model="memoForm.title"
                placeholder="请输入事件名称"
              />
            </el-form-item>
            <el-form-item label="内容" prop="content">
              <el-input
                v-model="memoForm.content"
                type="textarea"
                :rows="4"
                placeholder="请输入备忘录内容"
              />
            </el-form-item>
            <el-form-item label="地点" prop="location">
              <el-input
                v-model="memoForm.location"
                placeholder="请输入事件地点"
              />
            </el-form-item>
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="memoForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="提醒时间" prop="reminderOffset">
              <el-select v-model="memoForm.reminderOffset" placeholder="请选择提醒时间">
                <el-option label="0 分钟前" value="0" />
                <el-option label="5 分钟前" value="5" />
                <el-option label="15 分钟前" value="15" />
                <el-option label="30 分钟前" value="30" />
              </el-select>
            </el-form-item>
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="memoForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="showAddDialog = false">取消</el-button>
              <el-button type="primary" @click="submitMemo">确定</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </ContentField>
  </div>
</template>

<script setup>
// Vue3 组合式API核心依赖
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';
// 复用公共卡片组件
import ContentField from '@/components/ContentField.vue';

// axios基础路径
axios.defaults.baseURL = 'http://localhost:8080/api';

// 响应式数据
const memoList = ref([]);
const loading = ref(false);
const showAddDialog = ref(false);
const dialogTitle = ref('添加备忘录');
const isEdit = ref(false);
const currentMemoId = ref(null);

// 表单数据
const memoForm = ref({
  title: '',
  content: '',
  location: '',
  startTime: null,
  reminderOffset: 5,
  endTime: null
});

// 表单校验规则
const memoRules = ref({
  title: [
    { required: true, message: '请输入事件名称', trigger: 'blur' },
    { max: 100, message: '事件名称不能超过100个字', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入备忘录内容', trigger: 'blur' },
    { max: 500, message: '备忘录内容不能超过500个字', trigger: 'blur' }
  ],
  location: [
    { max: 200, message: '地点不能超过200个字', trigger: 'blur' }
  ]
});

// 表单引用
const memoFormRef = ref(null);

// 获取备忘录列表
const fetchMemos = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/memos');
    if (res.data.code === 200) {
      memoList.value = res.data.data;
    } else {
      ElMessage.error(res.data.msg || '获取备忘录失败');
    }
  } catch (err) {
    console.error('获取备忘录失败：', err);
    ElMessage.error('网络异常，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 提交备忘录（添加或编辑）
const submitMemo = async () => {
  try {
    await memoFormRef.value.validate();
  } catch (error) {
    return;
  }

  try {
    const payload = {
      title: memoForm.value.title,
      content: memoForm.value.content,
      location: memoForm.value.location,
      startTime: memoForm.value.startTime || null,
      reminderOffset: memoForm.value.reminderOffset,
      endTime: memoForm.value.endTime || null
    };

    let res;
    if (isEdit.value) {
      res = await axios.put(`/memos/${currentMemoId.value}`, payload);
    } else {
      res = await axios.post('/memos', payload);
    }

    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功');
      showAddDialog.value = false;
      resetForm();
      fetchMemos(); // 刷新列表
    } else {
      ElMessage.error(res.data.msg || '操作失败');
    }
  } catch (err) {
    console.error('操作失败：', err);
    ElMessage.error('网络异常，请稍后重试');
  }
};

// 编辑备忘录
const editMemo = (memo) => {
  isEdit.value = true;
  currentMemoId.value = memo.id;
  memoForm.value = {
    title: memo.title || '',
    content: memo.content || '',
    location: memo.location || '',
    startTime: memo.startTime || null,
    reminderOffset: memo.reminderOffset != null ? memo.reminderOffset : 5,
    endTime: memo.endTime || null
  };
  dialogTitle.value = '编辑备忘录';
  showAddDialog.value = true;
};

// 删除备忘录
const deleteMemo = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此备忘录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    const res = await axios.delete(`/memos/${id}`);
    if (res.data.code === 200) {
      ElMessage.success('删除成功');
      fetchMemos(); // 刷新列表
    } else {
      ElMessage.error(res.data.msg || '删除失败');
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败：', err);
      ElMessage.error('网络异常，请稍后重试');
    }
  }
};

// 重置表单
const resetForm = () => {
  memoFormRef.value?.resetFields();
  memoForm.value = { title: '', content: '', location: '', startTime: null, reminderOffset: 5, endTime: null };
  isEdit.value = false;
  currentMemoId.value = null;
  dialogTitle.value = '添加备忘录';
};

// 格式化日期

const formatDate = (row) => {
  if (!row) return '';
  // 兼容 el-table formatter 传参，row 为当前行对象
  // 假设 formatter 用于 startTime、endTime、createdAt、updatedAt 字段
  // 直接取对应字段值
  // 这里假设 formatter 绑定在 el-table-column 上，row 为当前行对象
  // 需要根据实际绑定字段名获取值
  // 这里以 startTime 为例
  // 可根据实际情况调整
  // 由于 formatter 绑定多列，需判断字段
  // 这里假设 formatter 只用于时间字段
  // 可根据实际情况调整
  // 这里只做简单处理
  // 若 row 为字符串则直接格式化
  if (typeof row === 'string' || typeof row === 'number') {
    return new Date(row).toLocaleString();
  }
  // 若 row 为对象，尝试取常见时间字段
  const timeFields = ['startTime', 'endTime', 'createdAt', 'updatedAt'];
  for (const key of timeFields) {
    if (row[key]) {
      return new Date(row[key]).toLocaleString();
    }
  }
  return '';
};

const formatReminderOffset = (row) => {
  const offset = row.reminderOffset;
  if (offset === 0) {
    return '0 分钟前';
  }
  if (offset === 5) {
    return '5 分钟前';
  }
  if (offset === 15) {
    return '15 分钟前';
  }
  if (offset === 30) {
    return '30 分钟前';
  }
  return '默认 5 分钟前';
};

// 组件挂载时获取数据
onMounted(() => {
  fetchMemos();
});
</script>

<style scoped>
/* 页面容器：居中+适配高度 */
.memo-container {
  width: 100%;
  max-width: 1000px; /* 限制最大宽度 */
  margin: 20px auto; /* 水平居中 */
  padding: 0 15px;
  box-sizing: border-box;
}

/* 表单内容区域 */
.memo-content {
  padding: 20px;
}

/* 页面标题 */
.page-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-weight: 600;
}

/* 添加按钮容器 */
.add-btn-container {
  margin-bottom: 20px;
  text-align: left;
}

/* 对话框样式 */
.dialog-footer {
  text-align: right;
}
</style>