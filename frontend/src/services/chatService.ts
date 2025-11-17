import { get, post, del } from './api';
import { ChatSession, ChatMessage } from '../types';

export const chatService = {
  async createChatSession(claimId: number, ownerId: number, claimantId: number): Promise<{ success: boolean; sessionId: number }> {
    return post('/chats/sessions', { claimId, ownerId, claimantId });
  },

  async getSessionsByUser(userId: number): Promise<{ success: boolean; sessions: ChatSession[] }> {
    return get(`/chats/sessions/user?userId=${userId}`);
  },

  async getSessionMessages(sessionId: number): Promise<{ success: boolean; messages: ChatMessage[] }> {
    return get(`/chats/sessions/${sessionId}/messages`);
  },

  async sendMessage(sessionId: number, senderId: number, message: string): Promise<{ success: boolean; message: ChatMessage }> {
    return post('/chats/messages', { sessionId, senderId, message });
  },

  async closeSession(sessionId: number): Promise<{ success: boolean; message: string }> {
    return del(`/chats/sessions/${sessionId}`);
  },

  async deleteMessage(messageId: number): Promise<{ success: boolean; message: string }> {
    return del(`/chats/messages/${messageId}`);
  },
};


