import { useState, useEffect } from 'react';
import { claimService } from '../services/claimService';
import { Claim } from '../types';

export function useClaims(userId?: number) {
  const [claims, setClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchClaims();
  }, []);

  const fetchClaims = async () => {
    try {
      setLoading(true);
      const response = userId
        ? await claimService.getClaimsByUser(userId)
        : await claimService.getPendingClaims();
      
      if (response.success) {
        setClaims(response.claims);
      } else {
        setError('Failed to fetch claims');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  return { claims, loading, error, refetch: fetchClaims };
}


