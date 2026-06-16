<template>
  <div class="page-container">
    <PageHeader title="积分中心" />

    <div class="credit-layout">
      <div class="credit-card">
        <div class="credit-info">
          <div class="credit-main">
            <span class="credit-label">我的信用积分</span>
            <span class="credit-value">{{ creditScore }}</span>
          </div>
          <div class="credit-sub">
            <span>连续签到 <strong>{{ signStatus.continuousDays }}</strong> 天</span>
            <span>累计签到 <strong>{{ signStatus.totalDays }}</strong> 天</span>
          </div>
        </div>
        <div class="credit-level">
          <div class="level-badge" :class="levelClass">{{ levelName }}</div>
          <el-rate :model-value="levelRate" disabled size="small" />
        </div>
      </div>

      <div class="tabs-card">
        <el-tabs v-model="activeTab">

          <el-tab-pane label="每日签到" name="sign">
            <div class="sign-section">
              <div class="sign-calendar">
                <div class="calendar-header">
                  <span class="calendar-title">{{ currentMonthLabel }}</span>
                  <div class="weekday-labels">
                    <span v-for="d in ['一','二','三','四','五','六','日']" :key="d">{{ d }}</span>
                  </div>
                </div>
                <div class="calendar-grid">
                  <div
                    v-for="(day, idx) in calendarDays"
                    :key="idx"
                    class="calendar-day"
                    :class="{ 'is-signed': day.signed, 'is-today': day.isToday, 'is-other': !day.currentMonth }"
                  >
                    {{ day.dayNum }}
                    <span v-if="day.signed" class="day-check">✓</span>
                  </div>
                </div>
              </div>

              <div class="sign-action">
                <div class="streak-title">连续签到奖励</div>
                <div class="sign-streak">
                  <div class="streak-item" v-for="n in 7" :key="n">
                    <div class="streak-dot" :class="{ active: n <= signStatus.continuousDays }">
                      <span>{{ n === 7 ? '🎁' : '+1' }}</span>
                    </div>
                    <div class="streak-label">第{{ n }}天</div>
                  </div>
                </div>
                <el-button
                  type="primary"
                  size="large"
                  :disabled="signStatus.signedToday"
                  :loading="signing"
                  class="sign-btn"
                  @click="handleSign"
                >
                  {{ signStatus.signedToday ? '今日已签到 ✓' : '立即签到 +1分' }}
                </el-button>
                <div v-if="signStatus.signedToday" class="sign-tip">
                  明天继续坚持，连续7天可获额外奖励！
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="每日任务" name="daily">
            <div v-if="dailyTasksLoading" class="loading">
              <el-skeleton :rows="4" animated />
            </div>
            <div v-else class="task-list">
              <div
                v-for="task in dailyTasks"
                :key="task.taskCode"
                class="task-item"
                :class="{ 'is-done': task.status === 2 }"
              >
                <div class="task-icon">{{ taskIcon(task.taskCode) }}</div>
                <div class="task-body">
                  <div class="task-name">{{ task.taskName }}</div>
                  <div class="task-desc">{{ task.description }}</div>
                </div>
                <span class="reward-badge">+{{ task.reward }}分</span>
                <div class="task-action">
                  <el-button v-if="task.status === 1" type="warning" size="small" @click="handleClaim(task)">领取</el-button>
                  <el-tag v-else-if="task.status === 2" type="success" size="small">已领取</el-tag>
                  <el-tag v-else size="small" type="info">未完成</el-tag>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="成就" name="achievements">
            <div v-if="achievementsLoading" class="loading">
              <el-skeleton :rows="4" animated />
            </div>
            <div v-else class="task-list">
              <div
                v-for="task in achievements"
                :key="task.taskCode"
                class="task-item"
                :class="{ 'is-done': task.status === 2 }"
              >
                <div class="task-icon">{{ taskIcon(task.taskCode) }}</div>
                <div class="task-body">
                  <div class="task-name">{{ task.taskName }}</div>
                  <div class="task-desc">{{ task.description }}</div>
                </div>
                <span class="reward-badge reward-big">+{{ task.reward }}分</span>
                <div class="task-action">
                  <el-button v-if="task.status === 1" type="warning" size="small" @click="handleClaim(task)">领取</el-button>
                  <el-tag v-else-if="task.status === 2" type="success" size="small">已领取</el-tag>
                  <el-tag v-else size="small" type="info">未完成</el-tag>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="积分说明" name="rules">
            <div class="rules-section">
              <div class="rules-block">
                <h3>积分获取途径</h3>
                <ul>
                  <li>每日签到：<strong>+1分</strong></li>
                  <li>连续签到7天：<strong>当天共+3分</strong></li>
                  <li>连续签到30天：<strong>当天共+5分</strong></li>
                  <li>完成每日任务（浏览/收藏/聊天）：<strong>+1~2分</strong></li>
                  <li>成就——首次发布商品：<strong>+5分</strong></li>
                  <li>成就——首次完成交易：<strong>+10分</strong></li>
                  <li>成就——坚持签到7天：<strong>+10分</strong></li>
                  <li>成就——累计签到30天：<strong>+20分</strong></li>
                </ul>
              </div>
              <div class="rules-block">
                <h3>积分等级</h3>
                <div class="level-table">
                  <div class="level-row" v-for="lv in levelRules" :key="lv.name">
                    <span class="lv-badge" :class="lv.cls">{{ lv.name }}</span>
                    <span class="lv-range">{{ lv.range }}</span>
                    <span class="lv-desc">{{ lv.desc }}</span>
                  </div>
                </div>
              </div>
              <div class="rules-block">
                <h3>积分扣减情形</h3>
                <ul>
                  <li>交易违规被举报处理：<strong>-5~20分</strong></li>
                  <li>买家差评：<strong>-2分</strong></li>
                  <li>积分降至0时账号将被封禁</li>
                </ul>
              </div>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import PageHeader from '@/components/common/PageHeader.vue';
import {
  getSignStatus, doSign, getDailyTasks, getAchievements, claimTaskReward,
  type SignStatusVO, type TaskVO,
} from '@/api/modules/credit';
import { getUserInfo } from '@/api/modules/user';
import { useAuthStore } from '@/stores/modules/auth';

const router = useRouter();
const authStore = useAuthStore();

const activeTab = ref('sign');
const creditScore = ref(100);
const signing = ref(false);
const dailyTasksLoading = ref(false);
const achievementsLoading = ref(false);

const signStatus = ref<SignStatusVO>({ signedToday: false, continuousDays: 0, totalDays: 0 });
const dailyTasks = ref<TaskVO[]>([]);
const achievements = ref<TaskVO[]>([]);

const levelName = computed(() => {
  const s = creditScore.value;
  if (s >= 150) return '信誉达人';
  if (s >= 120) return '诚信好友';
  if (s >= 90)  return '普通用户';
  if (s >= 60)  return '需要注意';
  return '信誉较差';
});
const levelClass = computed(() => {
  const s = creditScore.value;
  if (s >= 150) return 'lv-gold';
  if (s >= 120) return 'lv-blue';
  if (s >= 90)  return 'lv-green';
  if (s >= 60)  return 'lv-orange';
  return 'lv-red';
});
const levelRate = computed(() => {
  const s = creditScore.value;
  if (s >= 150) return 5;
  if (s >= 120) return 4;
  if (s >= 90)  return 3;
  if (s >= 60)  return 2;
  return 1;
});

const levelRules = [
  { name: '信誉达人', cls: 'lv-gold',   range: '>=150分', desc: '交易优先展示，获得信任徽章' },
  { name: '诚信好友', cls: 'lv-blue',   range: '120~149', desc: '享受平台推荐加权' },
  { name: '普通用户', cls: 'lv-green',  range: '90~119',  desc: '正常使用所有功能' },
  { name: '需要注意', cls: 'lv-orange', range: '60~89',   desc: '交易功能受限提醒' },
  { name: '信誉较差', cls: 'lv-red',    range: '<60',     desc: '功能受限，请规范行为' },
];

const currentMonthLabel = computed(() => dayjs().format('YYYY年MM月'));

type CalDay = { date: string; dayNum: number; signed: boolean; isToday: boolean; currentMonth: boolean };
const calendarDays = ref<CalDay[]>([]);

function buildCalendar() {
  const today = dayjs();
  const som = today.startOf('month');
  const dim = today.daysInMonth();
  const days: CalDay[] = [];
  const sw = (som.day() + 6) % 7;
  for (let i = 0; i < sw; i++) {
    const d = som.subtract(sw - i, 'day');
    days.push({ date: d.format('YYYY-MM-DD'), dayNum: d.date(), signed: false, isToday: false, currentMonth: false });
  }
  for (let i = 1; i <= dim; i++) {
    const d = som.add(i - 1, 'day');
    days.push({ date: d.format('YYYY-MM-DD'), dayNum: i, signed: false, isToday: d.isSame(today, 'day'), currentMonth: true });
  }
  calendarDays.value = days;
}

function markSignedDays(continuousDays: number) {
  const today = dayjs();
  const s = new Set<string>();
  for (let i = 0; i < continuousDays; i++) s.add(today.subtract(i, 'day').format('YYYY-MM-DD'));
  calendarDays.value = calendarDays.value.map(d => ({ ...d, signed: s.has(d.date) }));
}

function taskIcon(code: string): string {
  const m: Record<string, string> = {
    DAILY_SIGN: '📅', DAILY_BROWSE: '👀', DAILY_COLLECT: '⭐', DAILY_CHAT: '💬',
    ACH_FIRST_ITEM: '📦', ACH_FIRST_ORDER: '🤝', ACH_SIGN_7: '🔥', ACH_SIGN_30: '🏆',
  };
  return m[code] ?? '✅';
}

async function loadCreditScore() {
  try { const r = await getUserInfo(); if (r.data) creditScore.value = r.data.creditScore ?? 100; } catch { /* ignore */ }
}
async function loadSignStatus() {
  try {
    const r = await getSignStatus();
    if (r.data) { signStatus.value = r.data; markSignedDays(r.data.continuousDays); }
  } catch { /* ignore */ }
}
async function loadDailyTasks() {
  dailyTasksLoading.value = true;
  try { const r = await getDailyTasks(); dailyTasks.value = r.data ?? []; } finally { dailyTasksLoading.value = false; }
}
async function loadAchievements() {
  achievementsLoading.value = true;
  try { const r = await getAchievements(); achievements.value = r.data ?? []; } finally { achievementsLoading.value = false; }
}

async function handleSign() {
  signing.value = true;
  try {
    const r = await doSign();
    if (r.data) {
      ElMessage.success(`签到成功！+${r.data.addScore} 积分，连续签到 ${r.data.continuousDays} 天`);
      creditScore.value = r.data.newScore;
      signStatus.value.signedToday = true;
      signStatus.value.continuousDays = r.data.continuousDays;
      signStatus.value.totalDays += 1;
      markSignedDays(r.data.continuousDays);
      await loadDailyTasks();
    }
  } catch { ElMessage.warning('今日已签到'); } finally { signing.value = false; }
}

async function handleClaim(task: TaskVO) {
  try {
    const date = task.taskType === 2 ? '1970-01-01' : dayjs().format('YYYY-MM-DD');
    const r = await claimTaskReward(task.taskCode, date);
    if (r.data) { ElMessage.success(`领取成功！+${r.data.reward} 积分`); creditScore.value = r.data.newScore; task.status = 2; }
  } catch { ElMessage.error('领取失败，请稍后重试'); }
}

onMounted(async () => {
  if (!authStore.isLoggedIn) { router.push('/login'); return; }
  buildCalendar();
  await Promise.all([loadCreditScore(), loadSignStatus(), loadDailyTasks(), loadAchievements()]);
});
</script>

<style scoped>
.credit-layout { display: flex; flex-direction: column; gap: 20px; }
.credit-card { background: var(--color-primary); border: var(--border-pixel); border-radius: var(--radius-pixel); box-shadow: var(--shadow-hard); padding: 28px 32px; display: flex; align-items: center; justify-content: space-between; }
.credit-info { display: flex; flex-direction: column; gap: 10px; }
.credit-main { display: flex; align-items: baseline; gap: 14px; }
.credit-label { font-size: 15px; font-weight: 700; color: var(--color-text); }
.credit-value { font-size: 56px; font-weight: 900; color: var(--color-text); line-height: 1; letter-spacing: -2px; }
.credit-sub { display: flex; gap: 20px; font-size: 13px; font-weight: 600; color: var(--color-text); opacity: 0.75; }
.credit-sub strong { font-size: 16px; opacity: 1; }
.credit-level { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.level-badge { padding: 6px 20px; border: 2px solid var(--color-border); border-radius: var(--radius-pixel); font-size: 14px; font-weight: 800; box-shadow: var(--shadow-hard-sm); }
.lv-gold   { background: #ffd700; color: #5a3e00; }
.lv-blue   { background: #6c9ae0; color: #fff; }
.lv-green  { background: #7ecb8f; color: #fff; }
.lv-orange { background: #f4a460; color: #fff; }
.lv-red    { background: #e06c6c; color: #fff; }
.tabs-card { background: var(--color-surface); border: var(--border-pixel); border-radius: var(--radius-pixel); box-shadow: var(--shadow-hard); padding: 20px; }
.sign-section { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; padding-top: 12px; }
.calendar-header { margin-bottom: 8px; }
.calendar-title { font-size: 15px; font-weight: 800; color: var(--color-text); }
.weekday-labels { display: grid; grid-template-columns: repeat(7, 1fr); margin-top: 8px; gap: 4px; }
.weekday-labels span { text-align: center; font-size: 11px; font-weight: 700; color: var(--color-text-sub); }
.calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }
.calendar-day { position: relative; aspect-ratio: 1; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; border: 1.5px solid transparent; border-radius: var(--radius-pixel); background: #f0ede6; color: var(--color-text-sub); }
.calendar-day.is-today { border-color: var(--color-border); background: var(--color-secondary); color: var(--color-text); font-weight: 800; }
.calendar-day.is-signed { background: var(--color-primary); border-color: var(--color-border); color: var(--color-text); }
.calendar-day.is-other { opacity: 0.3; }
.day-check { position: absolute; top: 1px; right: 2px; font-size: 9px; }
.sign-action { display: flex; flex-direction: column; gap: 18px; justify-content: center; }
.streak-title { font-size: 13px; font-weight: 800; color: var(--color-text-sub); text-transform: uppercase; letter-spacing: 0.5px; }
.sign-streak { display: flex; justify-content: space-between; gap: 4px; }
.streak-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.streak-dot { width: 40px; height: 40px; border: 2px solid var(--color-border); border-radius: var(--radius-pixel); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; background: #f0ede6; color: var(--color-text-sub); }
.streak-dot.active { background: var(--color-primary); color: var(--color-text); box-shadow: var(--shadow-hard-sm); }
.streak-label { font-size: 10px; font-weight: 600; color: var(--color-text-sub); }
.sign-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 800; }
.sign-tip { font-size: 13px; font-weight: 600; color: var(--color-text-sub); text-align: center; }
.task-list { display: flex; flex-direction: column; gap: 10px; padding-top: 8px; }
.task-item { display: flex; align-items: center; gap: 14px; padding: 14px 16px; background: #f9f7f1; border: var(--border-pixel); border-radius: var(--radius-pixel); box-shadow: var(--shadow-hard-sm); }
.task-item.is-done { opacity: 0.55; }
.task-icon { font-size: 26px; flex-shrink: 0; width: 36px; text-align: center; }
.task-body { flex: 1; }
.task-name { font-size: 14px; font-weight: 800; color: var(--color-text); }
.task-desc { font-size: 12px; font-weight: 600; color: var(--color-text-sub); margin-top: 2px; }
.reward-badge { flex-shrink: 0; display: inline-block; padding: 2px 10px; background: var(--color-secondary); border: 1.5px solid var(--color-border); border-radius: var(--radius-pixel); font-size: 13px; font-weight: 800; color: var(--color-text); }
.reward-big { background: var(--color-primary); font-size: 14px; }
.task-action { flex-shrink: 0; min-width: 72px; text-align: right; }
.loading { padding: 16px 0; }
.rules-section { display: flex; flex-direction: column; gap: 16px; padding-top: 8px; }
.rules-block { background: #f9f7f1; border: var(--border-pixel); border-radius: var(--radius-pixel); padding: 16px 20px; }
.rules-block h3 { margin: 0 0 10px; font-size: 14px; font-weight: 800; color: var(--color-text); border-bottom: 1.5px solid var(--color-border); padding-bottom: 8px; }
.rules-block ul { margin: 0; padding-left: 18px; display: flex; flex-direction: column; gap: 6px; }
.rules-block li { font-size: 13px; font-weight: 600; color: var(--color-text); line-height: 1.6; }
.rules-block strong { color: var(--color-danger); }
.level-table { display: flex; flex-direction: column; gap: 8px; }
.level-row { display: flex; align-items: center; gap: 12px; }
.lv-badge { padding: 3px 12px; border: 1.5px solid var(--color-border); border-radius: var(--radius-pixel); font-size: 12px; font-weight: 800; min-width: 70px; text-align: center; }
.lv-range { font-size: 13px; font-weight: 600; color: var(--color-text-sub); min-width: 72px; }
.lv-desc { font-size: 13px; font-weight: 600; color: var(--color-text); }
</style>
