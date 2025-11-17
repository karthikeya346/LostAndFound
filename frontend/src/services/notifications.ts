import api from './api';

export type Notification = {
  id: number;
  user_id: number;
  type?: string;
  message: string;
  is_read?: boolean;
  created_at?: string;
};

export type NotificationsResponse = {
  success: boolean;
  notifications?: Notification[];
  message?: string;
};

export async function getAllNotifications(): Promise<Notification[]> {
  const res = await api.get<NotificationsResponse>('/notifications');
  return res.notifications ?? [];
}

export async function getNotificationsByUser(userId: number): Promise<Notification[]> {
  const res = await api.get<NotificationsResponse>(`/notifications/user/${userId}`);
  return res.notifications ?? [];
}

export async function markAsRead(id: number): Promise<boolean> {
  const res = await api.put<{ success: boolean; message?: string }>(`/notifications/${id}/read`);
  return !!res.success;
}

export async function deleteNotification(id: number, userId: number): Promise<boolean> {
  const res = await api.delete<{ success: boolean; message?: string }>(`/notifications/${id}?userId=${userId}`);
  return !!res.success;
}

export async function adminDeleteNotification(id: number, adminId: number): Promise<boolean> {
  const res = await api.delete<{ success: boolean; message?: string }>(`/notifications/${id}/admin?adminId=${adminId}`);
  return !!res.success;
}

export default { getAllNotifications, getNotificationsByUser, markAsRead, deleteNotification, adminDeleteNotification };
