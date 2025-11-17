import React, { createContext, useContext, useState } from 'react';

interface PreviewModeContextType {
  isPreviewMode: boolean;
  togglePreviewMode: () => void;
  currentRole: 'USER' | 'ADMIN';
  switchRole: (role: 'USER' | 'ADMIN') => void;
}

const PreviewModeContext = createContext<PreviewModeContextType | undefined>(undefined);

export const usePreviewMode = () => {
  const context = useContext(PreviewModeContext);
  if (context === undefined) {
    throw new Error('usePreviewMode must be used within a PreviewModeProvider');
  }
  return context;
};

export const PreviewModeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isPreviewMode, setIsPreviewMode] = useState(false);
  const [currentRole, setCurrentRole] = useState<'USER' | 'ADMIN'>('USER');

  const togglePreviewMode = () => {
    setIsPreviewMode(!isPreviewMode);
  };

  const switchRole = (role: 'USER' | 'ADMIN') => {
    setCurrentRole(role);
  };

  return (
    <PreviewModeContext.Provider value={{
      isPreviewMode,
      togglePreviewMode,
      currentRole,
      switchRole
    }}>
      {children}
    </PreviewModeContext.Provider>
  );
};
