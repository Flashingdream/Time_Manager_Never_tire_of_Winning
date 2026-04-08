<template>
  <div class="event-add-container">
    <!-- 外层卡片容器（复用ContentField组件保持风格统一） -->
    <ContentField>
      <div class="event-add-content">
        <!-- 页面标题 -->
        <h3 class="page-title">添加当日事件</h3>
        
        <!-- 表单区域 -->
        <el-form 
          ref="eventFormRef" 
          :model="eventForm" 
          :rules="eventRules" 
          label-width="100px"
          class="event-form"
        >
          <!-- 事件内容输入框 -->
          <el-form-item label="添加事件：" prop="content">
            <el-input
              v-model="eventForm.content"
              type="textarea"
              :rows="3"
              placeholder="请输入事件内容（如：上午9点开项目会议）"
              class="input-content"
            />
          </el-form-item>

          <!-- 日期选择框 -->
          <el-form-item label="添加日期：" prop="date">
            <el-date-picker
              v-model="eventForm.date"
              type="date"
              placeholder="请选择事件日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="input-date"
            />
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item class="btn-group">
            <el-button type="primary" @click="submitEvent">添加</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </ContentField>
  </div>
</template>

<script setup>
// Vue3 组合式API核心依赖
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
// 复用公共卡片组件
import ContentField from '@/components/ContentField.vue';

// axios基础路径（与CalendarIndexView.vue保持一致）
axios.defaults.baseURL = 'http://localhost:8080/api';

// 1. 表单数据（与后端接收参数对齐）
const eventForm = ref({
  content: '', // 事件内容
  date: ''     // 事件日期（格式：YYYY-MM-DD）
});

// 2. 表单校验规则（前端兜底校验，减少后端无效请求）
const eventRules = ref({
  content: [
    { required: true, message: '请输入事件内容', trigger: 'blur' },
    { max: 500, message: '事件内容不能超过500个字', trigger: 'blur' }
  ],
  date: [
    { required: true, message: '请选择事件日期', trigger: 'change' }
  ]
});

// 3. 表单引用（用于校验/重置）
const eventFormRef = ref(null);

// 4. 提交事件（核心：调用后端添加接口）
const submitEvent = async () => {
  // 第一步：前端表单校验
  try {
    await eventFormRef.value.validate();
  } catch (error) {
    ElMessage.warning('请完善表单必填项');
    return;
  }

  // 第二步：调用后端接口提交数据
  try {
    const res = await axios.post('/daily-events', {
      content: eventForm.value.content,
      date: eventForm.value.date
    });

    // 后端响应处理（按统一格式判断）
    if (res.data.code === 200) {
      ElMessage.success('事件添加成功！');
      resetForm(); // 清空表单
      // 可选：添加成功后返回日历页面（如需路由跳转，需引入useRouter）
      // const router = useRouter();
      // router.push('/calendar');
    } else {
      ElMessage.error(res.data.msg || '添加失败，请重试');
    }
  } catch (err) {
    // axios 1.x 错误处理（兼容网络/接口错误）
    console.error('添加事件失败：', err.response?.data || err.message);
    ElMessage.error('网络异常或服务器错误，请稍后重试');
  }
};

// 5. 重置表单
const resetForm = () => {
  eventFormRef.value.resetFields(); // 清空表单+重置校验
  eventForm.value = { content: '', date: '' }; // 重置数据
};
</script>

<style scoped>
/* 页面容器：居中+适配高度 */
.event-add-container {
  width: 100%;
  max-width: 800px; /* 限制最大宽度，避免太宽 */
  margin: 20px auto; /* 水平居中 */
  padding: 0 15px;
  box-sizing: border-box;
}

/* 表单内容区域 */
.event-add-content {
  padding: 20px;
}

/* 页面标题 */
.page-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-weight: 600;
}

/* 表单样式 */
.event-form {
  width: 100%;
}

/* 输入框样式 */
.input-content, .input-date {
  width: 100%;
}

/* 按钮组 */
.btn-group {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.btn-group .el-button {
  margin: 0 10px;
  width: 120px;
}
</style>