import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './contexts/ThemeContext';
import { AuthProvider } from './contexts/AuthContext';
import { PreviewModeProvider } from './contexts/PreviewModeContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import LoginPage from './pages/LoginPage';
import UserDashboard from './pages/UserDashboard';
import AdminDashboard from './pages/AdminDashboard';
import { Toaster } from './components/ui/toaster';
import { ProtectedRoute } from './components/ProtectedRoute';
import { usePreviewMode } from './contexts/PreviewModeContext';
import './App.css';

function AppRoutes() {
  const { isPreviewMode, currentRole } = usePreviewMode();
  return (
    <Router>
      <div className="min-h-screen relative z-[1]">
        <Routes>
          <Route
            path="/login"
            element={
              isPreviewMode ? (
                <Navigate to={currentRole === 'ADMIN' ? '/admin' : '/user'} replace />
              ) : (
                <LoginPage />
              )
            }
          />
          <Route
            path="/user/*"
            element={
              isPreviewMode ? (
                <UserDashboard />
              ) : (
                <ProtectedRoute requiredRole="USER">
                  <UserDashboard />
                </ProtectedRoute>
              )
            }
          />
          <Route
            path="/admin/*"
            element={
              isPreviewMode ? (
                <AdminDashboard />
              ) : (
                <ProtectedRoute requiredRole="ADMIN">
                  <AdminDashboard />
                </ProtectedRoute>
              )
            }
          />
          <Route
            path="/"
            element={
              <Navigate
                to={
                  isPreviewMode
                    ? currentRole === 'ADMIN'
                      ? '/admin'
                      : '/user'
                    : '/login'
                }
                replace
              />
            }
          />
        </Routes>
        <Toaster />
      </div>
    </Router>
  );
}

function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <AuthProvider>
          <PreviewModeProvider>
            <AppRoutes />
          </PreviewModeProvider>
        </AuthProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}

export default App;
