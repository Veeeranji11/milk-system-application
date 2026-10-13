import React from 'react';
import {
  LayoutDashboard,
  Droplet,
  Users,
  ShoppingBag,
  Receipt,
  CreditCard,
  BarChart3,
  Crown,
  Settings,
  X
} from 'lucide-react';

export default function Sidebar({ activeTab, setActiveTab, isOpen, onClose }) {
  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'collections', label: 'Milk Collections', icon: Droplet },
    { id: 'farmers', label: 'Farmers', icon: Users },
    { id: 'customers', label: 'Customers & Sales', icon: ShoppingBag },
    { id: 'billing', label: 'Billing & Invoices', icon: Receipt },
    { id: 'payments', label: 'Farmer Payments', icon: CreditCard },
    { id: 'reports', label: 'Reports & Analytics', icon: BarChart3 },
    { id: 'subscription', label: 'SaaS Subscription', icon: Crown, highlight: true },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          onClick={onClose}
          className="fixed inset-0 bg-slate-900/50 z-40 md:hidden backdrop-blur-xs"
        />
      )}

      {/* Sidebar container */}
      <aside
        className={`fixed md:static inset-y-0 left-0 z-50 w-64 bg-slate-900 text-slate-300 flex flex-col justify-between transition-transform duration-200 ease-in-out border-r border-slate-800 ${
          isOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        }`}
      >
        <div>
          {/* Header */}
          <div className="p-4 flex items-center justify-between border-b border-slate-800">
            <div className="flex items-center space-x-2.5">
              <div className="w-8 h-8 rounded-lg bg-sky-500 flex items-center justify-center text-white font-black text-lg shadow-sm">
                M
              </div>
              <div>
                <span className="font-extrabold text-white text-base tracking-tight">MilkSync</span>
                <span className="text-[10px] uppercase font-bold text-sky-400 block -mt-1 tracking-wider">Cloud SaaS</span>
              </div>
            </div>
            <button
              onClick={onClose}
              className="md:hidden text-slate-400 hover:text-white p-1 rounded"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Navigation Links */}
          <nav className="p-3 space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => {
                    setActiveTab(item.id);
                    if (onClose) onClose();
                  }}
                  className={`w-full flex items-center space-x-3 px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold transition-all ${
                    isActive
                      ? 'bg-sky-600 text-white shadow-md shadow-sky-950/40'
                      : item.highlight
                      ? 'text-amber-400 hover:bg-slate-800/80 hover:text-amber-300'
                      : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                  }`}
                >
                  <Icon className={`w-4 h-4 flex-shrink-0 ${isActive ? 'text-white' : item.highlight ? 'text-amber-400' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>
        </div>

        {/* Bottom Info Card */}
        <div className="p-4 border-t border-slate-800/80">
          <div className="bg-slate-800/50 rounded-xl p-3 border border-slate-700/50">
            <div className="text-[11px] font-bold text-slate-300">Cloud Multi-Tenant SaaS</div>
            <p className="text-[10px] text-slate-400 mt-0.5">Strict Tenant Isolation & SSL HTTPS Active</p>
            <div className="mt-2 flex items-center space-x-1.5 text-[10px] text-emerald-400 font-medium">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>All Cloud Systems Operational</span>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
