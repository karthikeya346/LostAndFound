import api from './api';

export type AnalyticsDashboardResponse = {
  success: boolean;
  data?: {
    totalItems?: number;
    totalUsers?: number;
    totalClaims?: number;
    totalChats?: number;
    activeClaims?: number;
    activeChats?: number;
    [key: string]: any;
  };
  message?: string;
};

export type DashboardMetrics = {
  totalItems: number;
  totalUsers: number;
  activeClaims: number;
  activeChats: number;
};

export async function getDashboard(): Promise<DashboardMetrics> {
  const res = await api.get<AnalyticsDashboardResponse>('/analytics/dashboard');
  const data = res?.data || {};
  return {
    totalItems: data.totalItems ?? 0,
    totalUsers: data.totalUsers ?? 0,
    // Map totalClaims -> activeClaims for UI naming
    activeClaims: (data.activeClaims ?? data.totalClaims) ?? 0,
    activeChats: (data.activeChats ?? data.totalChats) ?? 0,
  };
}

export type ItemsOverTime = { labels: string[]; items: number[] };
export async function getItemsOverTime(days = 7): Promise<ItemsOverTime> {
  const res = await api.get<{ success: boolean; data?: { labels?: string[]; items?: number[] } }>(`/analytics/items-over-time?days=${days}`);
  const d = res.data || {};
  return { labels: d.labels ?? [], items: d.items ?? [] };
}

export type ClaimStatus = { [status: string]: number };
export async function getClaimStatusBreakdown(): Promise<ClaimStatus> {
  const res = await api.get<{ success: boolean; data?: Record<string, number> }>(`/analytics/claim-status`);
  return res.data ?? {};
}

export type UserActivity = { active?: number; banned?: number };
export async function getUserActivity(): Promise<UserActivity> {
  const res = await api.get<{ success: boolean; data?: UserActivity }>(`/analytics/user-activity`);
  return res.data ?? { active: 0, banned: 0 };
}

export type ChatActivity = { open?: number; closed?: number };
export async function getChatActivity(): Promise<ChatActivity> {
  const res = await api.get<{ success: boolean; data?: ChatActivity }>(`/analytics/chat-activity`);
  return res.data ?? { open: 0, closed: 0 };
}

export type ItemTypeBreakdown = { [type: string]: number };
export async function getItemTypeBreakdown(): Promise<ItemTypeBreakdown> {
  const res = await api.get<{ success: boolean; data?: Record<string, number> }>(`/analytics/item-type-breakdown`);
  return res.data ?? {};
}

export default { 
  getDashboard,
  getItemsOverTime,
  getClaimStatusBreakdown,
  getUserActivity,
  getChatActivity,
  getItemTypeBreakdown,
};
