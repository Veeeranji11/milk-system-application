import React from 'react';
import { useAuth } from '../context/AuthContext';
import { AlertTriangle, Sparkles, Clock } from 'lucide-react';

export default function TrialBanner() {
  const { subscription, setShowUpgradeModal } = useAuth();

  if (!subscription) return null;

  const isTrial = subscription.status === 'TRIAL';
  const isExpired = subscription.status === 'EXPIRED' || (!subscription.isAccessAllowed && isTrial);
  const days = subscription.daysRemaining ?? 0;

  if (!isTrial && !isExpired) return null;

  if (isExpired) {
    return (
      <div className="bg-gradient-to-r from-red-600 to-rose-700 text-white px-4 py-3 shadow-md flex flex-col sm:flex-row items-center justify-between gap-3 animate-pulse">
        <div className="flex items-center space-x-3">
          <AlertTriangle className="w-5 h-5 flex-shrink-0 text-amber-300" />
          <div className="text-sm font-medium">
            <span className="font-bold">Subscription Expired:</span> Your 15-day free trial has expired. New collections and writes are temporarily restricted until renewal.
          </div>
        </div>
        <button
          onClick={() => setShowUpgradeModal(true)}
          className="bg-white text-red-700 hover:bg-red-50 text-xs sm:text-sm font-bold px-4 py-1.5 rounded-lg shadow transition whitespace-nowrap"
        >
          Activate Plan Now
        </button>
      </div>
    );
  }

  return (
    <div className="bg-gradient-to-r from-blue-700 via-indigo-700 to-sky-700 text-white px-4 py-2.5 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-2">
      <div className="flex items-center space-x-2.5">
        <Clock className="w-4 h-4 text-sky-200" />
        <div className="text-xs sm:text-sm">
          <span className="font-bold bg-sky-500/30 px-2 py-0.5 rounded text-sky-100 mr-2">15-Day Free Trial</span>
          You have <span className="font-extrabold text-amber-300 underline">{days} {days === 1 ? 'day' : 'days'} remaining</span>. Enjoy full access to all multi-tenant features.
        </div>
      </div>
      <button
        onClick={() => setShowUpgradeModal(true)}
        className="bg-amber-400 hover:bg-amber-300 text-slate-900 text-xs sm:text-sm font-bold px-3.5 py-1 rounded-md shadow flex items-center space-x-1.5 transition whitespace-nowrap"
      >
        <Sparkles className="w-3.5 h-3.5 text-amber-900" />
        <span>Upgrade Plan</span>
      </button>
    </div>
  );
}
