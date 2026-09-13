import React, { useState, useEffect } from 'react';
import { accountApi, beneficiaryApi, transferApi } from '../services/api';
import { ArrowLeftRight, CheckCircle2, AlertCircle, Users, Shield, RefreshCw } from 'lucide-react';

export const Transfer = () => {
  const [accounts, setAccounts] = useState([]);
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [fromAccountId, setFromAccountId] = useState('');
  const [toAccountId, setToAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [idempotencyKey, setIdempotencyKey] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successResult, setSuccessResult] = useState(null);

  const generateNewIdempotencyKey = () => {
    const key = typeof crypto !== 'undefined' && crypto.randomUUID
      ? crypto.randomUUID()
      : 'key-' + Math.random().toString(36).substring(2, 15);
    setIdempotencyKey(key);
  };

  useEffect(() => {
    generateNewIdempotencyKey();
    const loadInitialData = async () => {
      try {
        const [accRes, benRes] = await Promise.all([
          accountApi.getAccounts(),
          beneficiaryApi.getBeneficiaries(),
        ]);
        setAccounts(accRes.data.data || []);
        setBeneficiaries(benRes.data.data || []);
        if (accRes.data.data?.length > 0) {
          setFromAccountId(accRes.data.data[0].id);
        }
      } catch (err) {
        console.error(err);
      }
    };
    loadInitialData();
  }, []);

  const selectedFromAccount = accounts.find((a) => String(a.id) === String(fromAccountId));

  const handleTransfer = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessResult(null);
    setLoading(true);

    try {
      const payload = {
        fromAccountId: Number(fromAccountId),
        toAccountId: Number(toAccountId),
        amount: Number(amount),
        description: description || 'Transfer',
      };

      const res = await transferApi.transfer(payload, idempotencyKey);
      setSuccessResult(res.data.data);
      setAmount('');
      setDescription('');
      generateNewIdempotencyKey();

      // Refresh balances
      const accRes = await accountApi.getAccounts();
      setAccounts(accRes.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer failed. Please check balance and account details.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">Funds Transfer</h2>
        <p className="text-sm text-slate-500">
          Transfer money instantaneously with ACID transactional safety and idempotency protection.
        </p>
      </div>

      {successResult && (
        <div className="bg-emerald-50 border border-emerald-200 rounded-2xl p-6 text-emerald-900 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-start space-x-3">
            <CheckCircle2 size={24} className="text-emerald-600 shrink-0 mt-0.5" />
            <div>
              <h4 className="font-extrabold text-base">Transfer Completed Successfully!</h4>
              <p className="text-xs text-emerald-700 mt-0.5">
                Amount of <span className="font-bold">${Number(successResult.amount).toFixed(2)}</span> has been transferred.
              </p>
              <p className="text-xs font-mono text-emerald-800 mt-1">
                Ref ID: <span className="font-bold">{successResult.transactionReference}</span>
              </p>
            </div>
          </div>
          <button
            onClick={() => setSuccessResult(null)}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold transition self-start md:self-auto"
          >
            New Transfer
          </button>
        </div>
      )}

      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-2xl text-rose-700 text-sm font-medium flex items-center space-x-3">
          <AlertCircle size={20} className="shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="bg-white rounded-2xl border border-slate-200 p-6 sm:p-8 shadow-sm">
        <form onSubmit={handleTransfer} className="space-y-6">
          {/* Source Account */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
              From Account (Debit Source)
            </label>
            <select
              value={fromAccountId}
              onChange={(e) => setFromAccountId(e.target.value)}
              className="w-full px-4 py-3 bg-slate-50 border border-slate-300 rounded-xl text-sm font-semibold text-slate-800 focus:ring-2 focus:ring-blue-500 focus:outline-none"
            >
              {accounts.map((acc) => (
                <option key={acc.id} value={acc.id}>
                  {acc.accountType} - {acc.accountNumber} (Available: ${Number(acc.balance).toFixed(2)})
                </option>
              ))}
            </select>
            {selectedFromAccount && (
              <p className="text-xs text-slate-500 mt-1.5 font-medium">
                Current Available Balance: <span className="font-bold text-slate-800">${Number(selectedFromAccount.balance).toFixed(2)}</span>
              </p>
            )}
          </div>

          {/* Quick Payee Pick from Beneficiaries */}
          {beneficiaries.length > 0 && (
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2 flex items-center gap-1.5">
                <Users size={14} /> Quick Select Beneficiary
              </label>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                {beneficiaries.map((b) => (
                  <button
                    key={b.id}
                    type="button"
                    onClick={() => {
                      // Attempt to find account ID matching beneficiary account number
                      const match = accounts.find(a => a.accountNumber === b.accountNumber);
                      if (match) {
                        setToAccountId(match.id);
                      } else {
                        // User can paste the account ID or number
                        setToAccountId(b.id);
                      }
                    }}
                    className="p-2.5 rounded-xl border border-slate-200 hover:border-blue-400 bg-slate-50/60 hover:bg-blue-50 text-left transition text-xs"
                  >
                    <p className="font-bold text-slate-800 truncate">{b.beneficiaryName}</p>
                    <p className="font-mono text-[11px] text-slate-500 truncate">{b.accountNumber}</p>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Recipient Account ID */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
              Recipient Account ID (toAccountId)
            </label>
            <input
              type="number"
              required
              value={toAccountId}
              onChange={(e) => setToAccountId(e.target.value)}
              placeholder="e.g. 2 or 1002"
              className="w-full px-4 py-3 bg-slate-50 border border-slate-300 rounded-xl text-sm font-mono focus:ring-2 focus:ring-blue-500 focus:outline-none"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              Enter target bank account internal ID (e.g. Sarah Smith's account ID from Demo seed data).
            </p>
          </div>

          {/* Amount */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
              Transfer Amount ($)
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-slate-400 font-bold">$</span>
              <input
                type="number"
                step="0.01"
                min="1"
                required
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="0.00"
                className="w-full pl-8 pr-4 py-3 bg-slate-50 border border-slate-300 rounded-xl text-lg font-bold text-slate-900 focus:ring-2 focus:ring-blue-500 focus:outline-none"
              />
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
              Payment Reference Note
            </label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Invoice payment or House rent"
              className="w-full px-4 py-3 bg-slate-50 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
            />
          </div>

          {/* Idempotency Protection Indicator */}
          <div className="p-3.5 bg-slate-50 border border-slate-200/80 rounded-xl flex items-center justify-between text-xs text-slate-600">
            <div className="flex items-center gap-2">
              <Shield size={16} className="text-blue-600 shrink-0" />
              <span className="font-mono text-[11px] truncate max-w-xs sm:max-w-md">
                Idempotency-Key: {idempotencyKey}
              </span>
            </div>
            <button
              type="button"
              onClick={generateNewIdempotencyKey}
              title="Generate new idempotency key"
              className="text-slate-400 hover:text-blue-600 transition"
            >
              <RefreshCw size={14} />
            </button>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 px-4 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-sm shadow-lg shadow-blue-500/25 transition flex items-center justify-center space-x-2 disabled:opacity-50"
          >
            <ArrowLeftRight size={18} />
            <span>{loading ? 'Executing Transfer...' : 'Initiate Secure Transfer'}</span>
          </button>
        </form>
      </div>
    </div>
  );
};
