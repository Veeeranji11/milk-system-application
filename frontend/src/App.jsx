import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import LandingPage from './pages/LandingPage';
import RegisterPage from './pages/RegisterPage';
import UpgradeModal from './components/UpgradeModal';

// Simple hash-based page router (no react-router dependency needed)
function AppRoutes() {
  const [page, setPage] = useState('landing'); // 'landing' | 'register'
  const { user, showUpgradeModal, setShowUpgradeModal } = useAuth();

  // If user is authenticated, redirect to the main dashboard (Thymeleaf)
  if (user) {
    window.location.href = '/dashboard';
    return null;
  }

  return (
    <>
      {page === 'landing' && (
        <LandingPage
          onGoToLogin={() => { window.location.href = '/login'; }}
          onGoToRegister={() => setPage('register')}
        />
      )}

      {page === 'register' && (
        <RegisterPage
          onGoToLogin={() => { window.location.href = '/login'; }}
          onGoToLanding={() => setPage('landing')}
        />
      )}

      {showUpgradeModal && (
        <UpgradeModal onClose={() => setShowUpgradeModal(false)} />
      )}
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}
