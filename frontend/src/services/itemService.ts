import { get, post, put, del } from './api';
import { Item } from '../types';

export const itemService = {
  async getAllItems(): Promise<{ success: boolean; items: Item[] }> {
    return get('/items');
  },

  async getItemsByUser(userId: number): Promise<{ success: boolean; items: Item[] }> {
    return get(`/items/user?userId=${userId}`);
  },

  async getItemById(id: number): Promise<{ success: boolean; item: Item }> {
    return get(`/items/${id}`);
  },

  async addItem(item: Partial<Item>): Promise<{ success: boolean; item: Item; message?: string }> {
    return post('/items', item);
  },

  async updateItemStatus(id: number, status: string): Promise<{ success: boolean; message: string }> {
    return put(`/items/${id}/status`, { status });
  },

  async deleteItem(id: number): Promise<{ success: boolean; message: string }> {
    return del(`/items/${id}`);
  },
};


