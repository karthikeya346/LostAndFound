import { get, put } from './api';
import { User } from '../types';

export const userService = {
  async getAllUsers(): Promise<{ success: boolean; users: User[] }> {
    return get('/users/all');
  },

  async getUserById(id: number): Promise<{ success: boolean; user: User }> {
    return get(`/users/${id}`);
  },

  async banUser(id: number): Promise<{ success: boolean; message: string }> {
    return put(`/users/${id}/ban`);
  },

  async unbanUser(id: number): Promise<{ success: boolean; message: string }> {
    return put(`/users/${id}/unban`);
  },
};


