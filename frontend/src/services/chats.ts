import api from './api';

export type ChatSession = {
  id: number;
  itemId?: number;
  claimId?: number;
  startedBy?: number;
  status?: string;
  startedAt?: string;
  closedAt?: string | null;
};

export type ChatMessage = {
  id: number;
  chat_id: number;
  sender_user_id: number;
  message: string;
  sent_at?: string;
};

export type ChatParticipant = {
  id: number;
  chat_id: number;
  user_id: number;
  alias?: string;
  role?: string;
};

type RawChatSession = {
  id: number;
  item_id?: number;
  claim_id?: number;
  started_by?: number;
  status?: string;
  started_at?: string;
  closed_at?: string | null;
};

export type ChatsListResponse = {
  success: boolean;
  sessions?: (ChatSession | RawChatSession)[];
  message?: string;
};

export type ChatDetailResponse = {
  success: boolean;
  messages?: ChatMessage[];
  participants?: ChatParticipant[];
  message?: string;
};

function toCamelSession(s: ChatSession | RawChatSession): ChatSession {
  // If already camelCase, return as ChatSession
  const anyS: any = s as any;
  if (typeof anyS.itemId !== 'undefined' || typeof anyS.claimId !== 'undefined') {
    return anyS as ChatSession;
  }
  const raw = s as RawChatSession;
  return {
    id: raw.id,
    itemId: raw.item_id,
    claimId: raw.claim_id,
    startedBy: raw.started_by,
    status: raw.status,
    startedAt: raw.started_at,
    closedAt: raw.closed_at,
  };
}

export async function getAllChats(adminId: number): Promise<ChatSession[]> {
  const res = await api.get<ChatsListResponse>(`/chats?adminId=${adminId}`);
  const list = res.sessions ?? [];
  return list.map(toCamelSession);
}

export async function getUserChats(userId: number): Promise<ChatSession[]> {
  const res = await api.get<ChatsListResponse>(`/chats/user/${userId}`);
  const list = res.sessions ?? [];
  return list.map(toCamelSession);
}

export async function getChatDetail(chatId: number): Promise<{messages: ChatMessage[]; participants: ChatParticipant[]}> {
  const res = await api.get<ChatDetailResponse>(`/chats/${chatId}`);
  return { messages: res.messages ?? [], participants: res.participants ?? [] };
}

export async function createChat(params: { claimId: number; startedBy: number; itemId?: number }): Promise<number | null> {
  const payload: any = { claimId: params.claimId, startedBy: params.startedBy };
  if (typeof params.itemId === 'number' && !Number.isNaN(params.itemId)) payload.itemId = params.itemId;
  const res = await api.post<{ success: boolean; chatId?: number; message?: string }>(`/chats`, payload);
  return res.success ? (res.chatId ?? null) : null;
}

export async function sendMessage(chatId: number, senderId: number, message: string): Promise<boolean> {
  const res = await api.post<{ success: boolean; message?: string }>(`/chats/${chatId}/messages`, { senderId, message });
  return !!res.success;
}

export async function closeChat(chatId: number, adminId: number): Promise<boolean> {
  const res = await api.post<{ success: boolean; message?: string }>(`/chats/${chatId}/close?adminId=${adminId}`, {});
  return !!res.success;
}

export async function closeChatByUser(chatId: number, userId: number): Promise<boolean> {
  const res = await api.post<{ success: boolean; message?: string }>(`/chats/${chatId}/close-by-user?userId=${userId}`, {});
  return !!res.success;
}

export async function deleteChat(chatId: number, adminId: number): Promise<boolean> {
  const res = await api.delete<{ success: boolean; message?: string }>(`/chats/${chatId}?adminId=${adminId}`);
  return !!res.success;
}

export default { getAllChats, getUserChats, getChatDetail, createChat, sendMessage, closeChat, closeChatByUser, deleteChat };
