<template>
  <!-- 全屏专注模式 -->
  <div v-if="inSession" class="fullscreen-overlay" @keydown.esc="exitFocus" @click="exitFocus">
    <div class="focus-clock">
      <div class="current-time">{{ currentTime }}</div>
      <div class="focus-timer">
        <span class="timer-label">专注时长</span>
        <span class="timer-value">{{ sessionHours }}:{{ pad(sessionMins) }}:{{ pad(sessionSecs) }}</span>
      </div>
      <p class="exit-hint">按 Esc 或点击屏幕退出专注</p>
    </div>
  </div>

  <!-- 专注主页面 -->
  <div v-else class="focus-page">
    <div class="focus-container">
      <h2 class="page-title">专注模式</h2>

      <!-- 本月统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon month-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">本月累计专注</span>
            <span class="stat-value">{{ monthHours }} 小时 {{ monthMins }} 分钟</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon point-icon">
            <el-icon><StarFilled /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">累计专注积分</span>
            <span class="stat-value point-value">{{ totalPoints }} 分</span>
          </div>
        </div>
      </div>

      <!-- 各月份统计柱状图 -->
      <ContentField>
        <div class="chart-section">
          <h4>各月份专注时间统计</h4>
          <div v-if="monthKeys.length === 0" class="empty-chart">暂无专注记录</div>
          <div v-else class="bar-chart">
            <div v-for="m in monthKeys" :key="m" class="bar-row">
              <span class="bar-label">{{ m }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: barWidth(m) + '%' }">
                  <span class="bar-text" v-if="barWidth(m) > 15">{{ fmtMinutes(monthData[m].minutes) }}</span>
                </div>
                <span class="bar-text outside" v-if="barWidth(m) <= 15">{{ fmtMinutes(monthData[m].minutes) }}</span>
              </div>
            </div>
          </div>
        </div>
      </ContentField>

      <!-- 本月专注排行榜 -->
      <ContentField>
        <div class="chart-section">
          <h4>本月专注积分排行 TOP 3</h4>
          <div v-if="ranking.length === 0" class="empty-chart">本月暂无专注记录</div>
          <div v-else class="ranking-list">
            <div v-for="(r, idx) in ranking" :key="r.userId" class="ranking-item">
              <span :class="['rank-badge', 'rank-' + (idx + 1)]">{{ idx + 1 }}</span>
              <span class="rank-name">{{ r.userId }}</span>
              <span class="rank-points">{{ r.points }} 分</span>
              <span class="rank-time">{{ fmtMinutes(r.minutes) }}</span>
            </div>
          </div>
        </div>
      </ContentField>

      <!-- 积分规则说明 -->
      <div class="rule-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>积分规则：每专注 30 分钟 = 1 积分</span>
      </div>

      <!-- 开始专注按钮 -->
      <button class="start-focus-btn" @click="startFocus">
        <el-icon class="btn-icon"><VideoPlay /></el-icon>
        <span>开始专注</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { Timer, StarFilled, InfoFilled, VideoPlay } from '@element-plus/icons-vue';
import ContentField from '@/components/ContentField.vue';
import axios from 'axios';

const inSession = ref(false);
const sessionStart = ref(null);
const sessionHours = ref(0);
const sessionMins = ref(0);
const sessionSecs = ref(0);
const currentTime = ref('');

const monthData = ref({});
const totalPoints = ref(0);
const ranking = ref([]);

let clockTimer = null;
let sessionTimer = null;

const pad = (n) => String(n).padStart(2, '0');

const thisMonthKey = computed(() => {
  const d = new Date();
  return d.getFullYear() + '-' + pad(d.getMonth() + 1);
});

const monthKeys = computed(() => Object.keys(monthData.value).sort().reverse());

const monthTotal = computed(() => monthData.value[thisMonthKey.value] || { minutes: 0 });

const monthHours = computed(() => Math.floor(monthTotal.value.minutes / 60));
const monthMins = computed(() => monthTotal.value.minutes % 60);

const maxMinutes = computed(() => {
  const vals = Object.values(monthData.value).map(v => v.minutes);
  return vals.length ? Math.max(...vals, 60) : 60;
});

const barWidth = (key) => Math.round((monthData.value[key].minutes / maxMinutes.value) * 100);

const fmtMinutes = (mins) => {
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return h > 0 ? h + 'h' + m + 'm' : m + 'm';
};

const loadData = () => {
  const raw = localStorage.getItem('focusData');
  monthData.value = raw ? JSON.parse(raw) : {};
  totalPoints.value = parseInt(localStorage.getItem('focusPoints') || '0');
};

const fetchRanking = async () => {
  try {
    const res = await axios.get('/api/focus/ranking', {
      params: { yearMonth: thisMonthKey.value }
    });
    if (res.data.code === 200) ranking.value = res.data.data || [];
  } catch { /* offline */ }
};

const saveData = () => {
  localStorage.setItem('focusData', JSON.stringify(monthData.value));
  localStorage.setItem('focusPoints', totalPoints.value.toString());
};

const updateClock = () => {
  const d = new Date();
  currentTime.value = pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
};

const startFocus = async () => {
  try {
    if (document.documentElement.requestFullscreen) {
      await document.documentElement.requestFullscreen();
    }
  } catch { /* not supported */ }
  sessionStart.value = Date.now();
  sessionHours.value = 0;
  sessionMins.value = 0;
  sessionSecs.value = 0;
  inSession.value = true;
  updateClock();
  clockTimer = setInterval(updateClock, 1000);
  sessionTimer = setInterval(() => {
    const elapsed = Math.floor((Date.now() - sessionStart.value) / 1000);
    sessionHours.value = Math.floor(elapsed / 3600);
    sessionMins.value = Math.floor((elapsed % 3600) / 60);
    sessionSecs.value = elapsed % 60;
  }, 1000);
  document.addEventListener('keydown', onKeyDown);
  document.addEventListener('fullscreenchange', onFullscreenChange);
};

const exitFocus = () => {
  clearInterval(clockTimer);
  clearInterval(sessionTimer);
  document.removeEventListener('keydown', onKeyDown);
  document.removeEventListener('fullscreenchange', onFullscreenChange);

  const elapsedMin = Math.floor((Date.now() - sessionStart.value) / 60000);
  if (elapsedMin > 0) {
    const key = thisMonthKey.value;
    const cur = monthData.value[key] || { minutes: 0 };
    cur.minutes += elapsedMin;
    monthData.value[key] = cur;

    const earnedPoints = Math.floor(elapsedMin / 30);
    if (earnedPoints > 0) {
      totalPoints.value += earnedPoints;
    }
    saveData();

    // 同步到后端
    const username = localStorage.getItem('username') || 'anonymous';
    axios.post('/api/focus/save', {
      userId: username,
      yearMonth: key,
      minutes: elapsedMin
    }).then(() => fetchRanking()).catch(() => {});
  }

  inSession.value = false;
  try {
    if (document.fullscreenElement) {
      document.exitFullscreen();
    }
  } catch { /* ignore */ }
};

const onKeyDown = (e) => {
  if (e.key === 'Escape') {
    e.preventDefault();
    exitFocus();
  }
};

const onFullscreenChange = () => {
  if (!document.fullscreenElement && inSession.value) {
    exitFocus();
  }
};

onMounted(() => {
  loadData();
  fetchRanking();
});
onUnmounted(() => {
  clearInterval(clockTimer);
  clearInterval(sessionTimer);
  document.removeEventListener('keydown', onKeyDown);
  document.removeEventListener('fullscreenchange', onFullscreenChange);
});
</script>

<style scoped>
.focus-page { width: 100%; max-width: 800px; margin: 24px auto; padding: 0 16px; box-sizing: border-box; }
.focus-container { display: flex; flex-direction: column; gap: 20px; }
.page-title { text-align: center; margin: 0 0 4px; color: #333; font-weight: 600; }

/* === 统计卡片 === */
.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; display: flex; align-items: center; gap: 16px; padding: 20px; border-radius: 16px; background: rgba(255,255,255,0.7); backdrop-filter: blur(8px); box-shadow: 0 2px 12px rgba(0,0,0,0.05); transition: box-shadow 0.2s, transform 0.2s; }
.stat-card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.08); transform: translateY(-1px); }
.stat-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 22px; color: #fff; flex-shrink: 0; }
.month-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.point-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-info { display: flex; flex-direction: column; gap: 4px; }
.stat-label { font-size: 13px; color: #888; }
.stat-value { font-size: 20px; font-weight: 700; color: #333; }
.point-value { color: #e65100; }

/* === 柱状图 === */
.chart-section { padding: 16px; }
.chart-section h4 { margin: 0 0 16px; color: #444; font-size: 15px; }
.empty-chart { text-align: center; color: #bbb; padding: 30px 0; font-size: 14px; }
.bar-chart { display: flex; flex-direction: column; gap: 10px; }
.bar-row { display: flex; align-items: center; gap: 12px; }
.bar-label { width: 65px; font-size: 13px; color: #666; text-align: right; flex-shrink: 0; }
.bar-track { flex: 1; height: 28px; background: rgba(0,0,0,0.04); border-radius: 14px; overflow: hidden; display: flex; align-items: center; position: relative; }
.bar-fill { height: 100%; background: linear-gradient(90deg, #667eea, #764ba2); border-radius: 14px; display: flex; align-items: center; justify-content: flex-end; padding-right: 10px; transition: width 0.5s ease; min-width: 2px; }
.bar-text { font-size: 11px; color: #fff; font-weight: 500; white-space: nowrap; }
.bar-text.outside { margin-left: 8px; color: #888; flex-shrink: 0; }

/* === 排行榜 === */
.ranking-list { display: flex; flex-direction: column; gap: 8px; }
.ranking-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; }
.rank-badge { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; color: #fff; flex-shrink: 0; }
.rank-1 { background: linear-gradient(135deg, #f6d365, #fda085); }
.rank-2 { background: linear-gradient(135deg, #a1c4fd, #c2e9fb); }
.rank-3 { background: linear-gradient(135deg, #d4a373, #e9c46a); }
.rank-name { flex: 1; font-size: 14px; color: #333; font-weight: 500; }
.rank-points { font-size: 14px; color: #e65100; font-weight: 600; }
.rank-time { font-size: 12px; color: #999; }

/* === 积分规则 === */
.rule-tip { display: flex; align-items: center; gap: 8px; justify-content: center; color: #999; font-size: 13px; }

/* === 开始按钮 === */
.start-focus-btn { display: flex; align-items: center; justify-content: center; gap: 12px; width: 100%; max-width: 320px; margin: 0 auto; height: 56px; border: none; border-radius: 28px; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; font-size: 20px; font-weight: 600; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4); }
.start-focus-btn:hover { transform: scale(1.03); box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5); }
.start-focus-btn:active { transform: scale(0.98); }
.btn-icon { font-size: 24px; }

/* === 全屏专注模式 === */
.fullscreen-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: #0f0f1a; z-index: 9999; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.focus-clock { text-align: center; color: #fff; user-select: none; }
.current-time { font-size: 96px; font-weight: 300; letter-spacing: 6px; color: rgba(255,255,255,0.9); margin-bottom: 24px; font-variant-numeric: tabular-nums; }
.timer-label { display: block; font-size: 14px; color: rgba(255,255,255,0.4); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 2px; }
.timer-value { font-size: 48px; font-weight: 200; color: rgba(255,255,255,0.7); font-variant-numeric: tabular-nums; }
.exit-hint { margin-top: 60px; font-size: 13px; color: rgba(255,255,255,0.2); }
</style>
