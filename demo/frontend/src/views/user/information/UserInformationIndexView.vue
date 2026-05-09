<template>
  <ContentField>
    <div class="admin-container">
      <h2>管理员控制台</h2>

      <!-- 当前管理员信息卡片 -->
      <el-card class="admin-card" shadow="hover">
        <template #header>
          <span>当前管理员</span>
        </template>
        <div class="admin-info">
          <el-tag type="danger" size="large">管理员ID：{{ adminId }}</el-tag>
          <span class="admin-date">创建日期：{{ adminCreatedAt }}</span>
        </div>
      </el-card>

      <!-- 用户列表 -->
      <el-card class="user-table-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>用户列表（共 {{ users.length }} 人）</span>
            <el-button type="primary" size="small" @click="fetchUsers">刷新</el-button>
          </div>
        </template>
        <el-table :data="users" stripe border style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="数据库ID" width="120" />
          <el-table-column prop="userId" label="用户ID" min-width="180" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
                {{ row.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建日期" min-width="200" />
        </el-table>
      </el-card>
    </div>
  </ContentField>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import ContentField from '@/components/ContentField.vue';

const adminId = ref(localStorage.getItem('username') || '未知');
const adminCreatedAt = ref('');
const users = ref([]);
const loading = ref(false);

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/api/users');
    if (res.data.code === 200) {
      users.value = res.data.data;
      // 查找当前管理员的创建日期
      const me = users.value.find(u => u.userId === adminId.value);
      if (me) {
        adminCreatedAt.value = me.createdAt;
      }
    }
  } catch {
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.admin-container {
  padding: 20px;
}

.admin-container h2 {
  margin-bottom: 20px;
  color: #333;
}

.admin-card {
  margin-bottom: 20px;
}

.admin-info {
  display: flex;
  gap: 30px;
  align-items: center;
}

.admin-date {
  color: #666;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
