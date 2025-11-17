import api from './api';

export type User = {
  id: number;
  username: string;
  email: string;
  role: string;
  status?: string;
  created_at?: string;
};

export type UsersResponse = {
  success: boolean;
  users?: User[];
  message?: string;
};

export async function getAllUsers(): Promise<User[]> {
  const res = await api.get<UsersResponse>('/users');
  return res.users ?? [];
}

export async function getUserById(userId: number): Promise<User | null> {
  try {
    const res = await api.get<{ success: boolean; user?: User; message?: string }>(`/users/${userId}`);
    if ((res as any).user) return (res as any).user as User;
    // Some backends may return the user object directly
    if ((res as any).id && (res as any).username) return res as any as User;
  } catch (e) {
    // ignore
  }
  return null;
}

export async function banUser(userId: number, adminId: number): Promise<boolean> {
  const res = await api.post<{ success: boolean; message?: string }>(`/users/${userId}/ban?adminId=${adminId}`);
  return !!res.success;
}

export async function unbanUser(userId: number, adminId: number): Promise<boolean> {
  const res = await api.post<{ success: boolean; message?: string }>(`/users/${userId}/unban?adminId=${adminId}`);
  return !!res.success;
}

export default { getAllUsers, getUserById, banUser, unbanUser };
