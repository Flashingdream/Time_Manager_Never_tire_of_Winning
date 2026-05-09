<template>
  <div class="event-container">
    <ContentField>
      <div class="event-content">
        <h3 class="page-title">添加备忘录</h3>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="event-form">
          <el-form-item label="备忘录内容" prop="content">
            <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入备忘录内容..." />
          </el-form-item>

          <el-form-item label="标签" prop="tag">
            <el-select v-model="form.tag" placeholder="选择标签" style="width: 100%;">
              <el-option label="生活" value="生活" />
              <el-option label="学习" value="学习" />
              <el-option label="工作" value="工作" />
              <el-option label="娱乐" value="娱乐" />
              <el-option label="社交" value="社交" />
            </el-select>
          </el-form-item>

          <el-form-item class="btn-group">
            <el-button type="primary" class="action-btn" @click="submitMemo">添加</el-button>
            <el-button class="action-btn" @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </ContentField>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import ContentField from '@/components/ContentField.vue';

axios.defaults.baseURL = 'http://localhost:8080/api';

const form = ref({ content: '', tag: '生活' });

const rules = ref({
  content: [
    { required: true, message: '请输入备忘录内容', trigger: 'blur' },
    { max: 500, message: '不超过500字', trigger: 'blur' }
  ]
});

const formRef = ref(null);

const submitMemo = async () => {
  try { await formRef.value.validate(); } catch { return; }
  try {
    const res = await axios.post('/memos', {
      content: form.value.content,
      tag: form.value.tag
    });
    if (res.data.code === 200) {
      ElMessage.success('添加成功！');
      resetForm();
    } else {
      ElMessage.error(res.data.msg || '添加失败');
    }
  } catch {
    ElMessage.error('网络异常');
  }
};

const resetForm = () => {
  formRef.value.resetFields();
  form.value = { content: '', tag: '生活' };
};
</script>

<style scoped>
.event-container { width: 100%; max-width: 700px; margin: 20px auto; padding: 0 16px; box-sizing: border-box; }
.event-content { padding: 24px; }
.page-title { text-align: center; margin-bottom: 28px; color: #333; font-weight: 600; }
.event-form { width: 100%; }

.btn-group { display: flex; justify-content: center; gap: 16px; margin-top: 20px; }
.action-btn { border-radius: 10px; min-width: 100px; }

/* 全局圆角动画 */
:deep(.el-button) { border-radius: 10px; transition: all 0.2s; }
:deep(.el-button:hover) { transform: translateY(-1px); }
:deep(.el-button:active) { transform: scale(0.97); }
:deep(.el-input__wrapper) { border-radius: 10px; }
:deep(.el-textarea__inner) { border-radius: 10px; }
:deep(.el-select .el-input__wrapper) { border-radius: 10px; }
</style>
