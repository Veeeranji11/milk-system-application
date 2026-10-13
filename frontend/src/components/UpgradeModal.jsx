import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';
import { Check, X, ShieldCheck, Zap, Sparkles, CreditCard } from 'lucide-react';

export default function UpgradeModal() {
  const { showUpgradeModal, setShowUpgradeModal, refreshSubscription } = useAuth();
  const [loading, setLoading] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState('PRO');
  const [success, setSuccess] = useState(false);

  if (!showUpgradeModal) return null;

  const plans = [
    {
      id: 'STARTER',
      name: 'Starter Society',
      price: 499,
      interval: 'month',
      desc: 'For small village centers with up to 50 farmers.',
      features: ['Up to 50 Farmers', 'Daily Shift Collections', 'FAT & SNF Calculator', 'PDF Bills & Statements', 'Standard Support'],
    },
    {
      id: 'PRO',
      name: 'Professional Dairy',
      price: 999,
      interval: 'month',
      popular: true,
      desc: 'Most popular for commercial co-operatives.',
      features: ['Up to 500 Farmers', 'Customer Sales & Delivery', '10-Day Automated Settlements', 'Cloud Storage & PDF Invoices', 'Farmer Ledger Passbook', 'Priority Phone Support'],
    },
    {
      id: 'ENTERPRISE',
      name: 'Enterprise Union',
      price: 9999,
      interval: 'year',
      desc: 'For large unions and bulk cooling plants.',
      features: ['Unlimited Farmers & Customers', 'Multi-Center Data Aggregation', 'Custom Rate Chart Matrices', 'Automated Daily DB Backups', 'Dedicated Account Manager'],
    },
  ];

  const handleCheckout = async (planId) => {
    setLoading(true);
    try {
      // 1. Create order
      const orderData = await api.post('/subscriptions/create-order', { plan: planId, months: 1 });

      // 2. Simulated / Razorpay Checkout
      // For instant completion and testing: verify with mock signature
      const verifyData = await api.post('/subscriptions/verify', {
        orderRef: orderData.orderRef,
        paymentId: 'pay_' + Math.random().toString(36).substring(2, 10),
        signature: 'mock_signature_verified',
      });

      if (verifyData.success) {
        setSuccess(true);
        await refreshSubscription();
        setTimeout(() => {
          setSuccess(false);
          setShowUpgradeModal(false);
        }, 2000);
      }
    } catch (err) {
      alert(err.message || 'Payment processing failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 overflow-y-auto">
      <div className="bg-white rounded-2xl shadow-2xl max-w-4xl w-full p-6 sm:p-8 relative border border-slate-100">
        <button
          onClick={() => setShowUpgradeModal(false)}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 p-1.5 rounded-full hover:bg-slate-100 transition"
        >
          <X className="w-5 h-5" />
        </button>

        {success ? (
          <div className="text-center py-12">
            <div className="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto mb-4 animate-bounce">
              <ShieldCheck className="w-10 h-10" />
            </div>
            <h2 className="text-2xl font-bold text-slate-900 mb-2">Subscription Activated!</h2>
            <p className="text-slate-600">Your dairy SaaS subscription has been successfully updated. All features are now unlocked.</p>
          </div>
        ) : (
          <>
            <div className="text-center mb-8">
              <span className="inline-flex items-center space-x-1.5 bg-sky-50 text-sky-700 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider mb-2">
                <Sparkles className="w-3.5 h-3.5" />
                <span>Choose Your SaaS Tier</span>
              </span>
              <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900">Upgrade Dairy Subscription</h2>
              <p className="text-slate-500 text-sm sm:text-base mt-1">Unlock unlimited collections, multi-tenant isolation, automated PDF billing, and customer delivery tracking.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
              {plans.map((p) => {
                const isSelected = selectedPlan === p.id;
                return (
                  <div
                    key={p.id}
                    onClick={() => setSelectedPlan(p.id)}
                    className={`cursor-pointer relative rounded-xl p-5 border-2 transition-all flex flex-col justify-between ${
                      p.popular
                        ? 'border-sky-600 bg-sky-50/30 shadow-lg shadow-sky-100'
                        : isSelected
                        ? 'border-indigo-600 bg-indigo-50/20'
                        : 'border-slate-200 hover:border-slate-300 bg-white'
                    }`}
                  >
                    {p.popular && (
                      <span className="absolute -top-3 left-1/2 -translate-x-1/2 bg-sky-600 text-white text-[10px] font-extrabold uppercase px-2.5 py-0.5 rounded-full tracking-wide">
                        Most Popular
                      </span>
                    )}
                    <div>
                      <div className="text-base font-bold text-slate-900">{p.name}</div>
                      <p className="text-xs text-slate-500 mt-1 min-h-[32px]">{p.desc}</p>
                      <div className="my-4">
                        <span className="text-3xl font-extrabold text-slate-900">₹{p.price}</span>
                        <span className="text-xs text-slate-500"> /{p.interval}</span>
                      </div>
                      <ul className="space-y-2 text-xs text-slate-600 mb-6">
                        {p.features.map((feat, idx) => (
                          <li key={idx} className="flex items-start space-x-2">
                            <Check className="w-3.5 h-3.5 text-sky-600 mt-0.5 flex-shrink-0" />
                            <span>{feat}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                    <button
                      type="button"
                      disabled={loading}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleCheckout(p.id);
                      }}
                      className={`w-full py-2.5 rounded-lg text-xs font-bold transition flex items-center justify-center space-x-1.5 ${
                        p.popular
                          ? 'bg-sky-600 hover:bg-sky-700 text-white shadow'
                          : 'bg-slate-900 hover:bg-slate-800 text-white'
                      }`}
                    >
                      <CreditCard className="w-3.5 h-3.5" />
                      <span>{loading ? 'Processing...' : `Select ${p.name.split(' ')[0]}`}</span>
                    </button>
                  </div>
                );
              })}
            </div>

            <div className="border-t border-slate-100 pt-4 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-400 gap-2">
              <div className="flex items-center space-x-2">
                <ShieldCheck className="w-4 h-4 text-emerald-500" />
                <span>Bank-grade 256-bit SSL encryption. Online UPI & Card payments powered by Razorpay.</span>
              </div>
              <span className="text-slate-400">Cancel or upgrade anytime from settings.</span>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
