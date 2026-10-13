import React, { useState } from 'react';
import {
  ShieldCheck,
  CheckCircle2,
  Sparkles,
  Zap,
  ArrowRight,
  Calculator,
  Users,
  Receipt,
  Cloud,
  Check,
  Building2
} from 'lucide-react';

export default function LandingPage({ onGoToLogin, onGoToRegister }) {
  const [fat, setFat] = useState(6.5);
  const [litres, setLitres] = useState(12.0);
  const rateFactor = 7.5;
  const calculatedRate = (fat * rateFactor).toFixed(2);
  const totalAmount = (litres * calculatedRate).toFixed(2);

  return (
    <div className="min-h-screen bg-white">
      {/* Top Navigation */}
      <header className="border-b border-slate-100 sticky top-0 bg-white/90 backdrop-blur-md z-30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-sky-600 to-indigo-600 flex items-center justify-center text-white font-extrabold text-lg shadow-md shadow-sky-500/20">
              M
            </div>
            <div>
              <span className="text-xl font-extrabold tracking-tight text-slate-900">MilkSync</span>
              <span className="text-[10px] font-bold text-sky-600 bg-sky-50 px-1.5 py-0.5 rounded-sm ml-1.5 border border-sky-200">SaaS</span>
            </div>
          </div>

          <div className="flex items-center space-x-3 sm:space-x-4">
            <button
              onClick={onGoToLogin}
              className="text-slate-600 hover:text-slate-900 font-semibold text-xs sm:text-sm px-3 py-2 rounded-lg hover:bg-slate-50 transition"
            >
              Sign In
            </button>
            <button
              onClick={onGoToRegister}
              className="bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs sm:text-sm px-4 py-2 rounded-lg shadow-sm shadow-sky-600/30 transition flex items-center space-x-1.5"
            >
              <span>15-Day Free Trial</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 bg-gradient-to-b from-sky-50/50 via-white to-slate-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
          <div className="inline-flex items-center space-x-2 bg-sky-100/80 border border-sky-200 text-sky-800 text-xs font-bold px-3.5 py-1.5 rounded-full mb-6 shadow-xs">
            <Sparkles className="w-3.5 h-3.5 text-sky-600" />
            <span>Multi-Tenant Cloud Platform for Dairy Co-operatives</span>
          </div>

          <h1 className="text-3xl sm:text-5xl lg:text-6xl font-black text-slate-900 tracking-tight max-w-4xl mx-auto leading-tight">
            The Production-Ready SaaS for <span className="text-transparent bg-clip-text bg-gradient-to-r from-sky-600 to-indigo-600">Dairy Societies & Milk Centers</span>
          </h1>

          <p className="mt-5 text-base sm:text-lg text-slate-600 max-w-2xl mx-auto leading-relaxed">
            Manage farmers, retail customers, morning/evening collections, automated FAT pricing, cloud PDF billing, and payouts with strict tenant isolation.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3 sm:gap-4">
            <button
              onClick={onGoToRegister}
              className="w-full sm:w-auto bg-sky-600 hover:bg-sky-700 text-white text-sm sm:text-base font-bold px-8 py-3.5 rounded-xl shadow-lg shadow-sky-600/25 transition flex items-center justify-center space-x-2"
            >
              <span>Start 15-Day Free Trial</span>
              <ArrowRight className="w-4 h-4" />
            </button>
            <button
              onClick={onGoToLogin}
              className="w-full sm:w-auto bg-white border border-slate-300 hover:border-slate-400 text-slate-700 text-sm sm:text-base font-semibold px-6 py-3.5 rounded-xl shadow-xs transition"
            >
              Access Demo Center
            </button>
          </div>

          <div className="mt-8 flex flex-wrap items-center justify-center gap-6 text-xs text-slate-500">
            <div className="flex items-center space-x-1.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-500" />
              <span>No credit card required</span>
            </div>
            <div className="flex items-center space-x-1.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-500" />
              <span>15-day full access trial</span>
            </div>
            <div className="flex items-center space-x-1.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-500" />
              <span>100% cloud-hosted & mobile friendly</span>
            </div>
          </div>
        </div>
      </section>

      {/* Interactive Rate Calculator Demo */}
      <section className="py-16 bg-white border-y border-slate-100">
        <div className="max-w-4xl mx-auto px-4 sm:px-6">
          <div className="bg-gradient-to-br from-slate-900 to-slate-800 rounded-2xl p-6 sm:p-8 text-white shadow-xl">
            <div className="flex items-center space-x-2 text-sky-400 text-xs font-bold uppercase tracking-wider mb-2">
              <Calculator className="w-4 h-4" />
              <span>Interactive FAT Pricing Engine</span>
            </div>
            <h2 className="text-xl sm:text-2xl font-bold">Instant Milk Rate & Payout Calculator</h2>
            <p className="text-xs sm:text-sm text-slate-300 mt-1 mb-6">See how the system automatically computes per-litre rates from FAT% and generates farmer settlement amounts.</p>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
              <div className="space-y-4">
                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span>Milk Volume (Litres)</span>
                    <span className="text-sky-300 font-bold">{litres} L</span>
                  </div>
                  <input
                    type="range"
                    min="1"
                    max="50"
                    step="0.5"
                    value={litres}
                    onChange={(e) => setLitres(parseFloat(e.target.value))}
                    className="w-full h-2 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-sky-500"
                  />
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span>FAT Percentage (%)</span>
                    <span className="text-sky-300 font-bold">{fat}%</span>
                  </div>
                  <input
                    type="range"
                    min="3.0"
                    max="10.0"
                    step="0.1"
                    value={fat}
                    onChange={(e) => setFat(parseFloat(e.target.value))}
                    className="w-full h-2 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-sky-500"
                  />
                </div>
              </div>

              <div className="bg-slate-800/80 rounded-xl p-5 border border-slate-700 flex flex-col justify-between">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-[11px] text-slate-400">Rate / Litre</div>
                    <div className="text-2xl font-black text-sky-400">₹{calculatedRate}</div>
                    <div className="text-[10px] text-slate-500">(@ ₹{rateFactor}/FAT point)</div>
                  </div>
                  <div>
                    <div className="text-[11px] text-slate-400">Total Farmer Payout</div>
                    <div className="text-2xl font-black text-emerald-400">₹{totalAmount}</div>
                    <div className="text-[10px] text-slate-500">for {litres} litres</div>
                  </div>
                </div>
                <div className="mt-4 pt-3 border-t border-slate-700/80 text-[11px] text-slate-400 flex items-center justify-between">
                  <span>Settlement Cycle: 10-Day / Monthly</span>
                  <span className="text-emerald-400 font-semibold">PDF Invoice Ready</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Feature Grid */}
      <section className="py-20 bg-slate-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-2xl sm:text-4xl font-extrabold text-slate-900">Engineered for Scale, Security & Simplicity</h2>
            <p className="mt-3 text-slate-600 text-sm sm:text-base">Everything a modern dairy business needs to operate flawlessly in the cloud.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
              <div className="w-12 h-12 rounded-xl bg-sky-100 text-sky-600 flex items-center justify-center mb-4">
                <ShieldCheck className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Strict Multi-Tenant Isolation</h3>
              <p className="text-sm text-slate-600">Every dairy business operates in a secure, isolated workspace. No data leaks between neighboring co-operatives.</p>
            </div>

            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
              <div className="w-12 h-12 rounded-xl bg-indigo-100 text-indigo-600 flex items-center justify-center mb-4">
                <Receipt className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Automated PDF Billing</h3>
              <p className="text-sm text-slate-600">Generate 10-day settlement sheets in 1 click. Includes daily pour breakdown, deductions, and bank payout slips.</p>
            </div>

            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
              <div className="w-12 h-12 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center mb-4">
                <Cloud className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Cloud Storage & Backups</h3>
              <p className="text-sm text-slate-600">Bills and statements are stored in cloud object storage with automated daily database backups and disaster recovery.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section className="py-20 bg-white border-t border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <span className="text-xs font-bold text-sky-600 uppercase tracking-wider">Transparent Cloud Pricing</span>
            <h2 className="text-2xl sm:text-4xl font-extrabold text-slate-900 mt-1">Start Free, Scale as Your Dairy Grows</h2>
            <p className="mt-3 text-slate-600 text-sm sm:text-base">15-day free trial on all plans. No setup fees or hidden charges.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            {/* Starter */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200 flex flex-col justify-between">
              <div>
                <h3 className="text-lg font-bold text-slate-900">Starter Society</h3>
                <p className="text-xs text-slate-500 mt-1">For small village collection centers.</p>
                <div className="my-6">
                  <span className="text-4xl font-extrabold text-slate-900">₹499</span>
                  <span className="text-xs text-slate-500"> /month</span>
                </div>
                <ul className="space-y-2.5 text-xs text-slate-600">
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Up to 50 Farmers</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Morning & Evening Entry</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>FAT Rate Calculator</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>PDF Settlement Invoices</span></li>
                </ul>
              </div>
              <button
                onClick={onGoToRegister}
                className="mt-8 w-full py-2.5 rounded-xl border border-slate-300 hover:border-slate-400 font-bold text-xs text-slate-800 transition"
              >
                Start 15-Day Trial
              </button>
            </div>

            {/* Pro */}
            <div className="bg-sky-50/50 rounded-2xl p-6 border-2 border-sky-600 shadow-lg shadow-sky-100 flex flex-col justify-between relative">
              <span className="absolute -top-3 left-1/2 -translate-x-1/2 bg-sky-600 text-white text-[10px] font-bold px-3 py-0.5 rounded-full uppercase">
                Most Popular
              </span>
              <div>
                <h3 className="text-lg font-bold text-slate-900">Professional Dairy</h3>
                <p className="text-xs text-slate-500 mt-1">For commercial societies & collection points.</p>
                <div className="my-6">
                  <span className="text-4xl font-extrabold text-slate-900">₹999</span>
                  <span className="text-xs text-slate-500"> /month</span>
                </div>
                <ul className="space-y-2.5 text-xs text-slate-600">
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Up to 500 Farmers</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Customer Milk Sales & Delivery</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>10-Day Automated Batch Billing</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Cloud Document Storage</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Farmer Passbook Self-Service</span></li>
                </ul>
              </div>
              <button
                onClick={onGoToRegister}
                className="mt-8 w-full py-2.5 rounded-xl bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs shadow-sm transition"
              >
                Start 15-Day Trial
              </button>
            </div>

            {/* Enterprise */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200 flex flex-col justify-between">
              <div>
                <h3 className="text-lg font-bold text-slate-900">Enterprise Union</h3>
                <p className="text-xs text-slate-500 mt-1">For multi-village unions & milk plants.</p>
                <div className="my-6">
                  <span className="text-4xl font-extrabold text-slate-900">₹9,999</span>
                  <span className="text-xs text-slate-500"> /year</span>
                </div>
                <ul className="space-y-2.5 text-xs text-slate-600">
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Unlimited Farmers & Customers</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Multi-Center Branch Management</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Automated Daily DB Backups</span></li>
                  <li className="flex items-center space-x-2"><Check className="w-4 h-4 text-sky-600" /><span>Dedicated 24/7 SLA Support</span></li>
                </ul>
              </div>
              <button
                onClick={onGoToRegister}
                className="mt-8 w-full py-2.5 rounded-xl border border-slate-300 hover:border-slate-400 font-bold text-xs text-slate-800 transition"
              >
                Start 15-Day Trial
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-slate-900 text-slate-400 py-10 border-t border-slate-800 text-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-2 text-white font-bold">
            <Building2 className="w-4 h-4 text-sky-400" />
            <span>MilkSync Cloud SaaS Platform</span>
          </div>
          <div className="text-slate-500">
            © 2026 MilkSync. Cloud-first Multi-Tenant Architecture. Built with React & Spring Boot.
          </div>
        </div>
      </footer>
    </div>
  );
}
