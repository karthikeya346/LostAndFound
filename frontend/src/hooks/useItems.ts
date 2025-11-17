import { useState, useEffect } from 'react';
import { itemService } from '../services/itemService';
import { Item } from '../types';

export function useItems(userId?: number) {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchItems();
  }, []);

  const fetchItems = async () => {
    try {
      setLoading(true);
      const response = userId 
        ? await itemService.getItemsByUser(userId)
        : await itemService.getAllItems();
      
      if (response.success) {
        setItems(response.items);
      } else {
        setError('Failed to fetch items');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  return { items, loading, error, refetch: fetchItems };
}


