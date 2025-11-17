export interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  isBanned?: boolean;
  createdAt?: string;
}

export interface Item {
  id: number;
  userId: number;
  title: string;
  description: string;
  location: string;
  dateReported: string;
  type: 'LOST' | 'FOUND';
  status: 'UNDER' | 'APPROVED' | 'REJECTED';
  reportedBy?: string;
  imagePath?: string;
}

export interface Claim {
  id: number;
  itemId: number;
  claimantId: number;
  claimDate: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  description: string;
  item?: Item;
}

export interface ChatSession {
  id: number;
  claimId: number;
  createdAt: string;
  isClosed: boolean;
}

export interface ChatMessage {
  id: number;
  sessionId: number;
  senderId: number;
  message: string;
  sentAt: string;
  sender?: User;
}

export interface Notification {
  id: number;
  userId: number;
  type: 'CLAIM' | 'CHAT' | 'SYSTEM' | 'OTP';
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  expiresAt?: string;
}

export interface AuditLog {
  id: number;
  userId: number;
  action: string;
  itemId?: number;
  claimId?: number;
  details?: string;
  timestamp: string;
}

export interface DashboardStats {
  totalUsers: number;
  totalItems: number;
  totalClaims: number;
  totalChats: number;
}

export interface LoginResponse {
  success: boolean;
  message?: string;
  user?: User;
  token?: string;
}


