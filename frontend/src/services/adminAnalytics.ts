import api from './api';

export type AdminDashboardData = {
  totalUsers: number;
  totalItems: number;
  totalClaims: number;
  totalChats: number;
  totalLogs: number;
  claimStats: Record<string, number>;
  itemsByMonth: Record<string, number>;
  topLocations: Array<{ location: string; count: number }>;
  userGrowth: Record<string, number>;
};

export async function getAdminDashboard(): Promise<AdminDashboardData> {
  const res = await api.get<AdminDashboardData>('/admin/dashboard');
  return res as AdminDashboardData;
}

export default { getAdminDashboard };
