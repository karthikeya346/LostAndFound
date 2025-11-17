import { get } from './api';
import { DashboardStats } from '../types';

export const analyticsService = {
  async getDashboardData(): Promise<{ success: boolean; data: DashboardStats }> {
    return get('/analytics/dashboard');
  },

  async getItemsOverTime(days: number): Promise<{ success: boolean; data: any }> {
    return get(`/analytics/items-over-time?days=${days}`);
  },

  async getClaimStatusBreakdown(): Promise<{ success: boolean; data: any }> {
    return get('/analytics/claim-status-breakdown');
  },

  async getUserActivity(): Promise<{ success: boolean; data: any }> {
    return get('/analytics/user-activity');
  },

  async getChatActivity(): Promise<{ success: boolean; data: any }> {
    return get('/analytics/chat-activity');
  },

  async exportAnalytics(format: string): Promise<{ success: boolean; message: string }> {
    return get(`/analytics/export?format=${format}`);
  },
};


