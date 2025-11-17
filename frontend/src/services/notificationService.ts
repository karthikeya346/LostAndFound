import { get, post, put, del } from './api';
import { Notification } from '../types';

export const notificationService = {
  async getNotificationsByUser(userId: number): Promise<{ success: boolean; notifications: Notification[] }> {
    return get(`/notifications/user?userId=${userId}`);
  },

  async getAllNotifications(): Promise<{ success: boolean; notifications: Notification[] }> {
    return get('/notifications/all');
  },

  async markAsRead(notificationId: number): Promise<{ success: boolean; message: string }> {
    return put(`/notifications/${notificationId}/read`);
  },

  async deleteNotification(notificationId: number): Promise<{ success: boolean; message: string }> {
    return del(`/notifications/${notificationId}`);
  },

  async clearExpiredOtps(): Promise<{ success: boolean; message: string }> {
    return del('/notifications/clear-expired');
  },
};


