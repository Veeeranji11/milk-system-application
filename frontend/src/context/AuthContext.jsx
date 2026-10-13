import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [subscription, setSubscription] = useState({
    status: 'TRIAL',
    plan: 'FREE_TRIAL',
    daysRemaining: 15,
    isAccessAllowed: true,
  });
  const [loading, setLoading] = useState(true);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);

  useEffect(() => {
    // Check saved session
    const savedToken = localStorage.getItem('milk_jwt_token');
    const savedUser = localStorage.getItem('milk_user_data');
    if (savedToken && savedUser) {
      try {
        const parsed = JSON.parse(savedUser);
        setUser(parsed);
        setTenant({
          id: parsed.tenantId,
          businessName: parsed.businessName,
        });
        setSubscription({
          status: parsed.subscriptionStatus || 'TRIAL',
          plan: parsed.subscriptionPlan || 'FREE_TRIAL',
          daysRemaining: parsed.daysRemaining ?? 15,
          isAccessAllowed: parsed.isAccessAllowed ?? true,
          trialEndsAt: parsed.trialEndsAt,
          subscriptionEndsAt: parsed.subscriptionEndsAt,
        });
        // Fetch fresh subscription info
        refreshSubscription();
      } catch (e) {
        logout();
      }
    }
    setLoading(false);

    // Listen to subscription expired event from api client
    const handleSubscriptionExpired = () => {
      setShowUpgradeModal(true);
    };
    window.addEventListener('subscription:expired', handleSubscriptionExpired);

    return () => {
      window.removeEventListener('subscription:expired', handleSubscriptionExpired);
    };
  }, []);

  const refreshSubscription = async () => {
    try {
      const status = await api.get('/subscriptions/status');
      if (status) {
        setSubscription({
          status: status.status,
          plan: status.plan,
          daysRemaining: status.daysRemaining,
          isAccessAllowed: status.isAccessAllowed,
          trialEndsAt: status.trialEndsAt,
          subscriptionEndsAt: status.subscriptionEndsAt,
        });
      }
    } catch (err) {
      // Ignore if unauthenticated
    }
  };

  const login = async (username, password) => {
    const data = await api.post('/auth/login', { username, password });
    localStorage.setItem('milk_jwt_token', data.token);
    localStorage.setItem('milk_tenant_id', data.tenantId);
    localStorage.setItem('milk_user_data', JSON.stringify(data));

    setUser(data);
    setTenant({
      id: data.tenantId,
      businessName: data.businessName,
    });
    setSubscription({
      status: data.subscriptionStatus,
      plan: data.subscriptionPlan,
      daysRemaining: data.daysRemaining,
      isAccessAllowed: data.isAccessAllowed,
      trialEndsAt: data.trialEndsAt,
      subscriptionEndsAt: data.subscriptionEndsAt,
    });
    return data;
  };

  const register = async (registerData) => {
    const data = await api.post('/auth/register', registerData);
    localStorage.setItem('milk_jwt_token', data.token);
    localStorage.setItem('milk_tenant_id', data.tenantId);
    localStorage.setItem('milk_user_data', JSON.stringify(data));

    setUser(data);
    setTenant({
      id: data.tenantId,
      businessName: data.businessName,
    });
    setSubscription({
      status: data.subscriptionStatus,
      plan: data.subscriptionPlan,
      daysRemaining: data.daysRemaining,
      isAccessAllowed: data.isAccessAllowed,
      trialEndsAt: data.trialEndsAt,
      subscriptionEndsAt: data.subscriptionEndsAt,
    });
    return data;
  };

  const logout = () => {
    localStorage.removeItem('milk_jwt_token');
    localStorage.removeItem('milk_tenant_id');
    localStorage.removeItem('milk_user_data');
    setUser(null);
    setTenant(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        tenant,
        subscription,
        loading,
        showUpgradeModal,
        setShowUpgradeModal,
        login,
        register,
        logout,
        refreshSubscription,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
