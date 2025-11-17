import { get, post, put, del } from './api';
import { Claim } from '../types';

export const claimService = {
  async getPendingClaims(): Promise<{ success: boolean; claims: Claim[] }> {
    return get('/claims/pending');
  },

  async getClaimsByUser(userId: number): Promise<{ success: boolean; claims: Claim[] }> {
    return get(`/claims/user?userId=${userId}`);
  },

  async getClaimById(id: number): Promise<{ success: boolean; claim: Claim }> {
    return get(`/claims/${id}`);
  },

  async createClaim(itemId: number, claimantId: number, description: string): Promise<{ success: boolean; claim: Claim; message?: string }> {
    return post('/claims', { itemId, claimantId, description });
  },

  async approveClaim(id: number): Promise<{ success: boolean; message: string }> {
    return put(`/claims/${id}/approve`);
  },

  async rejectClaim(id: number): Promise<{ success: boolean; message: string }> {
    return put(`/claims/${id}/reject`);
  },

  async cancelClaim(id: number): Promise<{ success: boolean; message: string }> {
    return del(`/claims/${id}`);
  },
};


