import api, { postFormData } from './api';

export type Item = {
  id: number;
  userId?: number;
  title: string;
  description?: string;
  location?: string;
  date_reported?: string;
  type?: string;
  status?: string;
  image_path?: string;
  username?: string; // for admin view
};

export type ItemsResponse = {
  success: boolean;
  items?: Item[];
  message?: string;
};

export async function getAdminItems(): Promise<Item[]> {
  const res = await api.get<ItemsResponse>('/items/admin');
  return res.items ?? [];
}

export async function getUserItems(): Promise<Item[]> {
  const res = await api.get<ItemsResponse>('/items/user');
  return res.items ?? [];
}

export async function reportItem(params: { userId: number; title: string; description?: string; location?: string; type: string; imagePath?: string; }): Promise<boolean> {
  const payload = {
    userId: params.userId,
    title: params.title,
    description: params.description ?? '',
    location: params.location ?? '',
    type: params.type,
    imagePath: params.imagePath ?? ''
  };
  const res = await api.post<{ success: boolean; message?: string }>('/items', payload);
  return !!res.success;
}

export async function reportItemWithUpload(params: { userId: number; title: string; description?: string; location?: string; type: string; file?: File | null; }): Promise<{ success: boolean; imagePath?: string; message?: string; }> {
  const fd = new FormData();
  fd.append('userId', String(params.userId));
  fd.append('title', params.title);
  fd.append('description', params.description ?? '');
  fd.append('location', params.location ?? '');
  fd.append('type', params.type);
  if (params.file) fd.append('image', params.file);
  const res = await postFormData<{ success: boolean; imagePath?: string; message?: string }>('/items/upload', fd);
  return res;
}

export async function updateStatus(id: number, status: 'APPROVED' | 'REJECTED'): Promise<boolean> {
  const res = await api.put<{ success: boolean; message?: string }>(`/items/${id}/status`, { status });
  return !!res.success;
}

export default { getAdminItems, getUserItems, reportItem, reportItemWithUpload, updateStatus };
