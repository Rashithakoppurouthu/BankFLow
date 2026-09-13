import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { Shield, Users, CreditCard, History, ArrowDownLeft, ArrowUpRight, ArrowLeftRight, CheckCircle2, Lock } from 'lucide-react';

export const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        const res = await adminApi.getDashboardStats();
        setStats(res.data.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) {
    return <div className="p-12 text-center text-slate-400">Loading system metrics...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 p-6 md:p-8 rounded-2xl text-white shadow-xl border border-slate-800">
        <div className="flex items-center space-x-2 text-amber-400 text-xs font-bold uppercase tracking-widest">
          <Shield size={16} />
          <span>System Administration Portal</span>
        </div>
        <h2 className="text-2xl md:text-3xl font-black mt-1">BankFlow Core Control Center</h2>
        <p className="text-slate-400 text-sm mt-1">
          Monitor system-wide financial volumes, oversee active/frozen accounts, and inspect transaction ledgers.
        </p>
      </div>

      {/* Grid of Statistical Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase">Total Customers</span>
            <div className="w-9 h-9 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
              <Users size={18} />
            </div>
          </div>
          <p className="text-3xl font-black text-slate-900 mt-3">{stats?.totalCustomers || 0}</p>
          <span className="text-xs text-slate-400 mt-1 block">Registered user base</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase">Total Accounts</span>
            <div className="w-9 h-9 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold">
              <CreditCard size={18} />
            </div>
          </div>
          <p className="text-3xl font-black text-slate-900 mt-3">{stats?.totalAccounts || 0}</p>
          <span className="text-xs text-slate-400 mt-1 block">
            {stats?.activeAccounts || 0} active &bull; {stats?.frozenAccounts || 0} frozen
          </span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase">Total Transactions</span>
            <div className="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center font-bold">
              <History size={18} />
            </div>
          </div>
          <p className="text-3xl font-black text-slate-900 mt-3">{stats?.totalTransactions || 0}</p>
          <span className="text-xs text-slate-400 mt-1 block">Historical ledger entries</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase">Frozen Accounts</span>
            <div className="w-9 h-9 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center font-bold">
              <Lock size={18} />
            </div>
          </div>
          <p className="text-3xl font-black text-rose-600 mt-3">{stats?.frozenAccounts || 0}</p>
          <span className="text-xs text-slate-400 mt-1 block">Suspended accounts</span>
        </div>
      </div>

      {/* Volume Summary Cards */}
      <h3 className="text-lg font-bold text-slate-900 pt-2">System Financial Volumes</h3>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex items-center space-x-2 text-emerald-600 mb-2">
            <ArrowDownLeft size={18} />
            <span className="text-xs font-bold uppercase tracking-wider">Gross Deposits</span>
          </div>
          <p className="text-2xl font-black text-slate-900">
            ${Number(stats?.totalDeposits || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-slate-400 mt-1">Cumulative funds deposited</p>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex items-center space-x-2 text-amber-600 mb-2">
            <ArrowUpRight size={18} />
            <span className="text-xs font-bold uppercase tracking-wider">Gross Withdrawals</span>
          </div>
          <p className="text-2xl font-black text-slate-900">
            ${Number(stats?.totalWithdrawals || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-slate-400 mt-1">Cumulative funds withdrawn</p>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex items-center space-x-2 text-blue-600 mb-2">
            <ArrowLeftRight size={18} />
            <span className="text-xs font-bold uppercase tracking-wider">Gross Transfers</span>
          </div>
          <p className="text-2xl font-black text-slate-900">
            ${Number(stats?.totalTransfers || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-slate-400 mt-1">Cumulative inter-account transfers</p>
        </div>
      </div>
    </div>
  );
};
