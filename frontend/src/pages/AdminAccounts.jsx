import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { CreditCard, Lock, Unlock, RefreshCw, CheckCircle2 } from 'lucide-react';

export const AdminAccounts = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState(null);

  const fetchAccounts = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getAccounts();
      setAccounts(res.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const handleToggleFreeze = async (account) => {
    const isFrozen = account.status === 'FROZEN';
    const confirmMsg = isFrozen
      ? `Are you sure you want to unfreeze account ${account.accountNumber}?`
      : `Are you sure you want to FREEZE account ${account.accountNumber}? (Transfers and withdrawals will be suspended)`;

    if (!window.confirm(confirmMsg)) return;

    try {
      setActionLoadingId(account.id);
      if (isFrozen) {
        await adminApi.unfreezeAccount(account.id);
      } else {
        await adminApi.freezeAccount(account.id);
      }
      await fetchAccounts();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update account status');
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">Accounts Supervision</h2>
          <p className="text-sm text-slate-500">
            System-wide depository ledger, balances, and operational freezing controls.
          </p>
        </div>
        <button
          onClick={fetchAccounts}
          className="flex items-center space-x-2 px-3 py-2 bg-white border border-slate-300 rounded-xl text-xs font-semibold text-slate-700 hover:bg-slate-50 transition self-start sm:self-auto"
        >
          <RefreshCw size={14} />
          <span>Refresh</span>
        </button>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-400">Loading accounts supervision table...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-[11px] font-bold uppercase tracking-wider">
                <tr>
                  <th className="py-3.5 px-6">ID</th>
                  <th className="py-3.5 px-6">Account Number</th>
                  <th className="py-3.5 px-6">Owner Name</th>
                  <th className="py-3.5 px-6">Type</th>
                  <th className="py-3.5 px-6">Balance</th>
                  <th className="py-3.5 px-6">Status</th>
                  <th className="py-3.5 px-6 text-right">Administrative Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {accounts.map((acc) => {
                  const isFrozen = acc.status === 'FROZEN';
                  const isBusy = actionLoadingId === acc.id;
                  return (
                    <tr key={acc.id} className="hover:bg-slate-50/80 transition">
                      <td className="py-3.5 px-6 font-mono text-xs font-bold text-slate-500">#{acc.id}</td>
                      <td className="py-3.5 px-6 font-mono font-bold text-slate-800">{acc.accountNumber}</td>
                      <td className="py-3.5 px-6 font-semibold text-slate-900">{acc.ownerName}</td>
                      <td className="py-3.5 px-6">
                        <span className="px-2.5 py-0.5 rounded text-xs font-bold bg-slate-100 text-slate-700">
                          {acc.accountType}
                        </span>
                      </td>
                      <td className="py-3.5 px-6 font-extrabold text-slate-900">
                        ${Number(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </td>
                      <td className="py-3.5 px-6">
                        <span
                          className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-extrabold ${
                            isFrozen
                              ? 'bg-rose-100 text-rose-800'
                              : 'bg-emerald-100 text-emerald-800'
                          }`}
                        >
                          {isFrozen ? <Lock size={12} /> : <CheckCircle2 size={12} />}
                          {acc.status}
                        </span>
                      </td>
                      <td className="py-3.5 px-6 text-right">
                        <button
                          onClick={() => handleToggleFreeze(acc)}
                          disabled={isBusy}
                          className={`inline-flex items-center space-x-1 px-3 py-1.5 rounded-lg text-xs font-bold transition disabled:opacity-50 ${
                            isFrozen
                              ? 'bg-emerald-600 hover:bg-emerald-500 text-white'
                              : 'bg-rose-600 hover:bg-rose-500 text-white'
                          }`}
                        >
                          {isFrozen ? <Unlock size={14} /> : <Lock size={14} />}
                          <span>{isBusy ? 'Updating...' : isFrozen ? 'Unfreeze' : 'Freeze Account'}</span>
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
