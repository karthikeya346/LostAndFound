import { get, post } from './api';
import { LoginResponse, User } from '../types';

export const authService = {
  async login(username: string, password: string): Promise<LoginResponse> {
    return post<LoginResponse>('/auth/login', { username, password });
  },

  async register(username: string, password: string, email: string, role: string = 'USER'): Promise<LoginResponse> {
    return post<LoginResponse>('/auth/register', { username, password, email, role });
  },

  async getCurrentUser(userId: number): Promise<{ success: boolean; user: User }> {
    return get<{ success: boolean; user: User }>(`/auth/me?userId=${userId}`);
  },

  async checkUsername(username: string): Promise<boolean> {
    const response = await get<{ exists: boolean }>(`/users/check-username?username=${username}`);
    return response.exists;
  },

  async checkEmail(email: string): Promise<boolean> {
    const response = await get<{ exists: boolean }>(`/users/check-email?email=${email}`);
    return response.exists;
  },

  async forgotPassword(email: string): Promise<{ success: boolean; message?: string; token?: string }> {
    return post<{ success: boolean; message?: string; token?: string }>(`/auth/forgot`, { email });
  },

  async resetPassword(token: string, newPassword: string): Promise<{ success: boolean; message?: string }> {
    return post<{ success: boolean; message?: string }>(`/auth/reset`, { token, newPassword });
  },
};


