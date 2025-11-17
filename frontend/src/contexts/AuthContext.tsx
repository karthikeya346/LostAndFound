import React, { createContext, useContext, useState, useEffect } from 'react';

interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

interface LoginResult {
  success: boolean;
  user?: User;
  otp?: string;
  message?: string;
}

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string) => Promise<LoginResult>;
  verifyOtp: (otp: string) => Promise<{ success: boolean; message?: string }>;
  logout: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string): Promise<LoginResult> => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      const data = await response.json();
      if (data && data.success) {
        setUser(data.user);
        localStorage.setItem('user', JSON.stringify(data.user));
        localStorage.setItem('pendingUsername', data.user.username); // Store username for OTP verify
        return { success: true, user: data.user, otp: data.otp, message: data.message };
      }
      return { success: false, message: data?.message };
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, message: 'Network or server error' };
    }
  };

  const verifyOtp = async (otp: string): Promise<{ success: boolean; message?: string }> => {
    try {
      const pendingUsername = localStorage.getItem('pendingUsername') || '';
      const response = await fetch('http://localhost:8080/api/auth/verify-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: pendingUsername, otp }),
      });

      const data = await response.json();
      if (data && data.success) {
        localStorage.removeItem('pendingUsername'); // Clean up after success
        return { success: true };
      }
      return { success: false, message: data?.message };
    } catch (e) {
      return { success: false, message: 'Network or server error' };
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('pendingUsername'); // Clean up on logout
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        verifyOtp,
        logout,
        isAuthenticated: !!user,
        isLoading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};