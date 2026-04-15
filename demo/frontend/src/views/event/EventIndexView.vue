<template>
  <div class="memo-add-container">
    <!-- 复用公共卡片组件，保持风格统一 -->
    <ContentField>
      <div class="memo-add-content">
        <!-- 页面标题 -->
        <h3 class="page-title">添加备忘录</h3>
        
        <!-- 备忘录表单 -->
        <el-form 
          ref="memoFormRef" 
          :model="memoForm" 
          :rules="memoRules" 
          label-width="100px"
          class="memo-form"
        >
          <!-- 备忘录内容输入框 -->
          <el-form-item label="备忘录内容：" prop="content">
            <el-input
              v-model="memoForm.content"
              type="textarea"
              :rows="5"
              placeholder="请输入备忘录内容（如：买牛奶、整理桌面）"
              class="input-content"
            />
          </el-form-item>

          <!-- 提交/重置按钮 -->
          <el-form-item class="btn-group">
            <el-button type="primary" @click="submitMemo">添加</el-button>
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
// 仅引入用到的ElMessage，避免ESLint报错
import { ElMessage } from 'element-plus';
import axios from 'axios';
// 复用公共卡片组件
import ContentField from '@/components/ContentField.vue';

// axios基础路径（与其他页面保持一致）
axios.defaults.baseURL = 'http://localhost:8080/api';

// 1. 表单数据（仅content，无date字段）
const memoForm = ref({
  content: '' // 备忘录内容
});

// 2. 表单校验规则
const memoRules = ref({
  content: [
    { required: true, message: '请输入备忘录内容', trigger: 'blur' },
    { max: 500, message: '备忘录内容不能超过500个字', trigger: 'blur' }
  ]
});

// 3. 表单引用
const memoFormRef = ref(null);

// 4. 提交备忘录（核心：调用后端新增接口）
const submitMemo = async () => {
  // 第一步：前端表单校验
  try {
    await memoFormRef.value.validate();
  } catch (error) {
    ElMessage.warning('请完善备忘录内容');
    return;
  }

  // 第二步：调用后端接口
  try {
    const res = await axios.post('/memos', {
      content: memoForm.value.content // 仅传内容，无日期
    });

    // 后端响应处理
    if (res.data.code === 200) {
      ElMessage.success('备忘录添加成功！');
      resetForm(); // 清空表单
      // 可选：添加成功后返回日历页面
      // const router = useRouter();
      // router.push('/calendar');
    } else {
      ElMessage.error(res.data.msg || '添加失败，请重试');
    }
  } catch (err) {
    // axios 1.x 错误处理
    console.error('添加备忘录失败：', err.response?.data || err.message);
    ElMessage.error('网络异常或服务器错误，请稍后重试');
  }
};

// 5. 重置表单
const resetForm = () => {
  memoFormRef.value.resetFields();
  memoForm.value = { content: '' };
};
</script>

<style scoped>
/* 页面容器：居中+限制宽度 */
.memo-add-container {
  width: 100%;
  max-width: 800px;
  margin: 20px auto;
  padding: 0 15px;
  box-sizing: border-box;
}

/* 表单内容区域 */
.memo-add-content {
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
.memo-form {
  width: 100%;
}

/* 输入框样式 */
.input-content {
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