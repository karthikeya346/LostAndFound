import api from './api';

export type AuditLog = {
  id: number;
  user_id?: number | null;
  action: string;
  item_id?: number | null;
  claim_id?: number | null;
  details?: string | null;
  created_at?: string;
};

export type AuditLogsResponse = {
  success: boolean;
  logs?: AuditLog[];
  message?: string;
};

export async function getRecentLogs(limit = 50): Promise<AuditLog[]> {
  const res = await api.get<AuditLogsResponse>(`/audit/logs?limit=${limit}`);
  return res.logs ?? [];
}

export async function deleteAllLogs(): Promise<{ success: boolean; deleted?: number }> {
  const res = await api.delete<{ success: boolean; deleted?: number; message?: string }>(`/audit/logs`);
  return { success: !!res.success, deleted: (res as any).deleted };
}

export async function deleteLogById(id: number): Promise<boolean> {
  const res = await api.delete<{ success: boolean; message?: string }>(`/audit/logs/${id}`);
  return !!res.success;
}

export default { getRecentLogs, deleteAllLogs, deleteLogById };
