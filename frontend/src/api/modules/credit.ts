import { get, post } from '@/utils/request';

export interface SignStatusVO {
  signedToday: boolean;
  continuousDays: number;
  totalDays: number;
}

export interface SignResultVO {
  addScore: number;
  newScore: number;
  continuousDays: number;
}

export interface TaskVO {
  taskCode: string;
  taskName: string;
  description: string;
  reward: number;
  taskType: number;
  sort: number;
  status: number | null;   // 0-未完成 1-已完成待领取 2-已领取 null-未开始
  completeTime: string | null;
  rewardTime: string | null;
}

export interface ClaimResultVO {
  reward: number;
  newScore: number;
}

export function getSignStatus() {
  return get<SignStatusVO>('/user/sign/status');
}

export function doSign() {
  return post<SignResultVO>('/user/sign', {});
}

export function getDailyTasks() {
  return get<TaskVO[]>('/user/tasks/daily');
}

export function getAchievements() {
  return get<TaskVO[]>('/user/tasks/achievements');
}

export function claimTaskReward(taskCode: string, taskDate: string) {
  return post<ClaimResultVO>('/user/tasks/claim', { taskCode, taskDate });
}
