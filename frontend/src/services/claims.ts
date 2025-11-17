import api from './api';

export type Claim = {
  id: number;
  item_id: number;
  user_id: number;
  claim_date?: string;
  status?: string;
  matched_item_id?: number | null;
};

export type ClaimsResponse = {
  success: boolean;
  claims?: Claim[];
  message?: string;
};

export async function getPendingClaims(): Promise<Claim[]> {
  const res = await api.get<{ success: boolean; claims: Claim[] }>(`/claims/pending`);
  return res.claims ?? [];
}

export async function markReturned(claimId: number): Promise<boolean> {
  const res = await api.post<{ success: boolean; message?: string }>(`/claims/${claimId}/mark-returned`, {});
  return !!res.success;
}

export async function getClaimsByUser(userId: number): Promise<Claim[]> {
  const res = await api.get<ClaimsResponse>(`/claims/user/${userId}`);
  return res.claims ?? [];
}

export async function createClaim(itemId: number, userId: number): Promise<{ success: boolean; message?: string }> {
  return api.post<{ success: boolean; message?: string }>(`/claims`, { itemId, userId });
}

export default { getPendingClaims, markReturned, getClaimsByUser, createClaim };
