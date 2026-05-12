<template>
  <div class="admin-page">
    <h2>管理员控制台</h2>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card total">
        <span class="stat-num">{{ stats.total }}</span>
        <span class="stat-label">总用户数</span>
      </div>
      <div class="stat-card admin">
        <span class="stat-num">{{ stats.admins }}</span>
        <span class="stat-label">管理员</span>
      </div>
      <div class="stat-card user">
        <span class="stat-num">{{ stats.users }}</span>
        <span class="stat-label">普通用户</span>
      </div>
      <div class="stat-card new">
        <span class="stat-num">{{ stats.newThisMonth }}</span>
        <span class="stat-label">本月新增</span>
      </div>
    </div>

    <!-- 当前管理员信息 -->
    <ContentField>
      <div class="admin-info-section">
        <h4>当前管理员</h4>
        <div class="admin-info">
          <el-tag type="danger" size="large">管理员ID：{{ adminId }}</el-tag>
          <span class="admin-date">创建日期：{{ adminCreatedAt || '-' }}</span>
        </div>
      </div>
    </ContentField>

    <!-- 用户列表 -->
    <ContentField>
      <div class="user-table-section">
        <div class="card-header">
          <h4>用户列表（共 {{ stats.total }} 人）</h4>
          <div class="header-right">
            <div class="search-bar">
              <el-icon><Search /></el-icon>
              <input v-model="searchKeyword" placeholder="搜索用户ID..." class="search-input" @keyup.enter="fetchData" />
            </div>
            <el-button type="primary" size="small" @click="fetchData">刷新</el-button>
          </div>
        </div>
        <el-table :data="users" stripe border style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="数据库ID" width="80" />
          <el-table-column prop="userId" label="用户ID" min-width="150" />
          <el-table-column prop="role" label="角色" width="90">
            <template #default="{ row }">
              <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
                {{ row.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.banned ? 'danger' : 'success'" size="small">
                {{ row.banned ? '已封禁' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建日期" min-width="180" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <template v-if="row.userId !== 'admin'">
                <el-button v-if="!row.banned" size="small" type="danger" @click="banUser(row)">
                  封禁
                </el-button>
                <el-button v-else size="small" type="success" @click="unbanUser(row)">
                  解封
                </el-button>
              </template>
              <span v-else class="admin-tip">—</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentField>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import axios from 'axios';
import ContentField from '@/components/ContentField.vue';

const adminId = ref(localStorage.getItem('username') || '未知');
const adminCreatedAt = ref('');
const users = ref([]);
const loading = ref(false);
const searchKeyword = ref('');
const stats = ref({ total: 0, admins: 0, users: 0, newThisMonth: 0 });

const fetchData = async () => {
  loading.value = true;
  try {
    const params = searchKeyword.value.trim() ? { keyword: searchKeyword.value.trim() } : {};
    const res = await axios.get('/api/admin/users/stats', { params });
    if (res.data.code === 200) {
      const data = res.data.data;
      stats.value.total = data.total || 0;
      stats.value.admins = data.admins || 0;
      stats.value.users = data.users || 0;
      stats.value.newThisMonth = data.newThisMonth || 0;
      users.value = data.allUsers || [];

      const me = users.value.find(u => u.userId === adminId.value);
      if (me) adminCreatedAt.value = me.createdAt;
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (err) {
    const msg = err.response?.data?.msg;
    ElMessage.error(msg || '获取数据失败');
  } finally {
    loading.value = false;
  }
};

const banUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定封禁用户 "${row.userId}" 吗？`, '确认封禁', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const res = await axios.put(`/api/admin/users/${row.userId}/ban`);
    if (res.data.code === 200) {
      row.banned = true;
      ElMessage.success('封禁成功');
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (err) {
    if (err !== 'cancel') {
      const msg = err.response?.data?.msg;
      ElMessage.error(msg || '操作失败');
    }
  }
};

const unbanUser = async (row) => {
  try {
    const res = await axios.put(`/api/admin/users/${row.userId}/unban`);
    if (res.data.code === 200) {
      row.banned = false;
      ElMessage.success('解封成功');
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (err) {
    const msg = err.response?.data?.msg;
    ElMessage.error(msg || '操作失败');
  }
};

onMounted(() => fetchData());
</script>

<style scoped>
.admin-page {
  width: 100%;
  max-width: 960px;
  margin: 20px auto;
  padding: 0 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-page h2 {
  margin: 0;
  color: #333;
  text-align: center;
}

/* 统计卡片行 */
.stats-row {
  display: flex;
  gap: 14px;
}

.stat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 18px 12px;
  border-radius: 14px;
  background: rgba(255,255,255,0.72);
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  color: #888;
}

.stat-card.total .stat-num { color: #409eff; }
.stat-card.admin .stat-num { color: #e53935; }
.stat-card.user  .stat-num { color: #43a047; }
.stat-card.new   .stat-num { color: #fb8c00; }

/* 管理员信息 */
.admin-info-section {
  padding: 16px;
}

.admin-info-section h4 {
  margin: 0 0 12px;
  color: #444;
  font-size: 15px;
}

.admin-info {
  display: flex;
  gap: 24px;
  align-items: center;
}

.admin-date {
  color: #888;
  font-size: 13px;
}

/* 用户表格 */
.user-table-section {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.card-header h4 {
  margin: 0;
  color: #444;
  font-size: 15px;
  flex-shrink: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-bar {
  display: flex;
  align-items: center;
  height: 32px;
  border-radius: 16px;
  background: rgba(0,0,0,0.04);
  padding: 0 12px;
  gap: 6px;
  transition: background 0.2s;
}
.search-bar:focus-within {
  background: rgba(0,0,0,0.07);
}
.search-bar .el-icon {
  font-size: 14px;
  color: #999;
}
.search-input {
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: #333;
  width: 140px;
}
.search-input::placeholder {
  color: #bbb;
}

.admin-tip {
  color: #ccc;
  font-size: 13px;
}
</style>
