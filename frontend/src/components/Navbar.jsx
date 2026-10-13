import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, Menu, Building2, User, Sparkles } from 'lucide-react';

export default function Navbar({ onMenuToggle }) {
  const { user, tenant, subscription, setShowUpgradeModal, logout } = useAuth();

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-xs">
      <div className="px-4 sm:px-6 py-3 flex items-center justify-between">
        {/* Left: Mobile hamburger & Dairy Brand */}
        <div className="flex items-center space-x-3">
          <button
            onClick={onMenuToggle}
            className="md:hidden p-1.5 rounded-lg text-slate-500 hover:text-slate-700 hover:bg-slate-100 transition"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex items-center space-x-2">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-sky-500 to-indigo-600 flex items-center justify-center text-white shadow-sm">
              <Building2 className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-sm sm:text-base font-bold text-slate-900 leading-tight">
                {tenant?.businessName || 'MilkSync Dairy'}
              </h1>
              <div className="flex items-center space-x-2 text-[11px] text-slate-500">
                <span className="font-medium text-sky-700 bg-sky-50 px-1.5 py-0.2 rounded border border-sky-100">
                  Tenant: {tenant?.id || 'default'}
                </span>
                <span className="text-slate-300">•</span>
                <span className="capitalize font-semibold text-emerald-600">
                  {subscription?.status || 'TRIAL'} ({subscription?.plan || 'Free Trial'})
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Right: Quick actions & User badge */}
        <div className="flex items-center space-x-3">
          {subscription?.status === 'TRIAL' && (
            <button
              onClick={() => setShowUpgradeModal(true)}
              className="hidden sm:flex items-center space-x-1.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-white text-xs font-bold px-3 py-1.5 rounded-lg shadow-sm transition"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>Upgrade</span>
            </button>
          )}

          <div className="flex items-center space-x-2 pl-2 sm:border-l sm:border-slate-200">
            <div className="hidden sm:block text-right">
              <div className="text-xs font-bold text-slate-800">{user?.fullName || user?.username}</div>
              <div className="text-[10px] text-slate-400 font-medium tracking-wide uppercase">
                {user?.role?.replace('ROLE_', '') || 'ADMIN'}
              </div>
            </div>
            <div className="w-8 h-8 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600">
              <User className="w-4 h-4" />
            </div>
            <button
              onClick={logout}
              title="Sign Out"
              className="text-slate-400 hover:text-red-600 p-1.5 rounded-lg hover:bg-red-50 transition ml-1"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
