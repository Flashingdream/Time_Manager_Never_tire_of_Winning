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
          <el-table-column prop="content" label="内容" width="400"></el-table-column>
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
        <el-dialog :title="dialogTitle" v-model="showAddDialog" width="500px">
          <el-form :model="memoForm" :rules="memoRules" ref="memoFormRef" label-width="100px">
            <el-form-item label="内容" prop="content">
              <el-input
                v-model="memoForm.content"
                type="textarea"
                :rows="4"
                placeholder="请输入备忘录内容"
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
  content: ''
});

// 表单校验规则
const memoRules = ref({
  content: [
    { required: true, message: '请输入备忘录内容', trigger: 'blur' },
    { max: 500, message: '备忘录内容不能超过500个字', trigger: 'blur' }
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
    let res;
    if (isEdit.value) {
      res = await axios.put(`/memos/${currentMemoId.value}`, { content: memoForm.value.content });
    } else {
      res = await axios.post('/memos', { content: memoForm.value.content });
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
  memoForm.value.content = memo.content;
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
  memoForm.value = { content: '' };
  isEdit.value = false;
  currentMemoId.value = null;
  dialogTitle.value = '添加备忘录';
};

// 格式化日期
const formatDate = (row, column, cellValue) => {
  if (!cellValue) return '';
  return new Date(cellValue).toLocaleString();
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