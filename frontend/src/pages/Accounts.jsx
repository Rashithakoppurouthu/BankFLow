import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { accountApi } from '../services/api';
import {
  Wallet,
  PlusCircle,
  ArrowDownLeft,
  ArrowUpRight,
  CheckCircle2,
  AlertCircle,
  X,
  CreditCard,
} from 'lucide-react';

export const Accounts = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);

  // New Account Modal
  const [isNewAccountOpen, setIsNewAccountOpen] = useState(false);
  const [accountType, setAccountType] = useState('SAVINGS');
  const [initialDeposit, setInitialDeposit] = useState('100.00');

  // Action Modal (Deposit or Withdraw)
  const [activeModal, setActiveModal] = useState(null); // 'DEPOSIT' | 'WITHDRAW'
  const [targetAccount, setTargetAccount] = useState(null);
  const [actionAmount, setActionAmount] = useState('');
  const [actionDesc, setActionDesc] = useState('');
  const [modalLoading, setModalLoading] = useState(false);
  const [modalError, setModalError] = useState('');
  const [modalSuccess, setModalSuccess] = useState('');

  const fetchAccounts = async () => {
    try {
      setLoading(true);
      const res = await accountApi.getAccounts();
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

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    setModalError('');
    setModalSuccess('');
    setModalLoading(true);

    try {
      await accountApi.createAccount({
        accountType,
        initialDeposit: Number(initialDeposit) || 0,
      });
      setModalSuccess('New bank account provisioned successfully!');
      await fetchAccounts();
      setTimeout(() => {
        setIsNewAccountOpen(false);
        setModalSuccess('');
      }, 1000);
    } catch (err) {
      setModalError(err.response?.data?.message || 'Failed to create account');
    } finally {
      setModalLoading(false);
    }
  };

  const handleActionSubmit = async (e) => {
    e.preventDefault();
    setModalError('');
    setModalSuccess('');
    setModalLoading(true);

    try {
      if (activeModal === 'DEPOSIT') {
        await accountApi.deposit(targetAccount.id, {
          amount: Number(actionAmount),
          description: actionDesc || 'Cash Deposit',
        });
        setModalSuccess('Deposit completed successfully!');
      } else {
        await accountApi.withdraw(targetAccount.id, {
          amount: Number(actionAmount),
          description: actionDesc || 'Cash Withdrawal',
        });
        setModalSuccess('Withdrawal completed successfully!');
      }

      await fetchAccounts();
      setTimeout(() => {
        setActiveModal(null);
        setTargetAccount(null);
        setActionAmount('');
        setActionDesc('');
        setModalSuccess('');
      }, 1000);
    } catch (err) {
      setModalError(err.response?.data?.message || 'Transaction failed');
    } finally {
      setModalLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">Bank Accounts</h2>
          <p className="text-sm text-slate-500">Manage your depository accounts, view real-time balances, and transact.</p>
        </div>
        <button
          onClick={() => {
            setIsNewAccountOpen(true);
            setModalError('');
            setModalSuccess('');
          }}
          className="flex items-center space-x-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 rounded-xl text-white font-semibold text-sm shadow-md shadow-blue-500/20 transition self-start sm:self-auto"
        >
          <PlusCircle size={16} />
          <span>Open New Account</span>
        </button>
      </div>

      {loading ? (
        <div className="p-12 text-center text-slate-400">Loading accounts...</div>
      ) : accounts.length === 0 ? (
        <div className="bg-white rounded-2xl p-12 text-center border border-slate-200">
          <Wallet size={48} className="mx-auto text-slate-300 mb-3" />
          <h3 className="text-lg font-bold text-slate-700">No bank accounts found</h3>
          <p className="text-sm text-slate-500 mt-1">Open a Savings or Current account to get started.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {accounts.map((account) => (
            <div
              key={account.id}
              className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold px-3 py-1 bg-slate-100 text-slate-800 rounded-full tracking-wide uppercase">
                    {account.accountType}
                  </span>
                  <span
                    className={`text-xs font-bold px-2.5 py-0.5 rounded-full ${
                      account.status === 'ACTIVE'
                        ? 'bg-emerald-50 text-emerald-700'
                        : 'bg-rose-50 text-rose-700'
                    }`}
                  >
                    {account.status}
                  </span>
                </div>

                <p className="text-[11px] font-mono font-semibold text-slate-400 mt-5 uppercase tracking-wider">
                  Account Number
                </p>
                <p className="text-xl font-mono font-extrabold text-slate-800 tracking-wider">
                  {account.accountNumber.replace(/(\d{4})/g, '$1 ').trim()}
                </p>

                <p className="text-[11px] font-mono font-semibold text-slate-400 mt-4 uppercase tracking-wider">
                  Available Balance
                </p>
                <p className="text-3xl font-extrabold text-slate-900 tracking-tight">
                  ${Number(account.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </p>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between gap-2">
                <button
                  onClick={() => {
                    setTargetAccount(account);
                    setActiveModal('DEPOSIT');
                    setActionAmount('');
                    setActionDesc('');
                    setModalError('');
                    setModalSuccess('');
                  }}
                  className="flex-1 flex items-center justify-center space-x-1 py-2 px-3 rounded-lg bg-emerald-50 hover:bg-emerald-100 text-emerald-700 text-xs font-bold transition"
                >
                  <ArrowDownLeft size={14} />
                  <span>Deposit</span>
                </button>
                <button
                  onClick={() => {
                    setTargetAccount(account);
                    setActiveModal('WITHDRAW');
                    setActionAmount('');
                    setActionDesc('');
                    setModalError('');
                    setModalSuccess('');
                  }}
                  className="flex-1 flex items-center justify-center space-x-1 py-2 px-3 rounded-lg bg-amber-50 hover:bg-amber-100 text-amber-700 text-xs font-bold transition"
                >
                  <ArrowUpRight size={14} />
                  <span>Withdraw</span>
                </button>
                <Link
                  to={`/accounts/${account.id}`}
                  className="py-2 px-3 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-700 text-xs font-bold transition"
                >
                  History
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Open New Account Modal */}
      {isNewAccountOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <h3 className="text-lg font-bold text-slate-900">Open a Bank Account</h3>
              <button onClick={() => setIsNewAccountOpen(false)} className="text-slate-400 hover:text-slate-600">
                <X size={20} />
              </button>
            </div>

            {modalError && (
              <div className="mt-4 p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs font-medium flex items-center gap-2">
                <AlertCircle size={16} />
                <span>{modalError}</span>
              </div>
            )}
            {modalSuccess && (
              <div className="mt-4 p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-700 text-xs font-medium flex items-center gap-2">
                <CheckCircle2 size={16} />
                <span>{modalSuccess}</span>
              </div>
            )}

            <form onSubmit={handleCreateAccount} className="mt-4 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Account Category</label>
                <select
                  value={accountType}
                  onChange={(e) => setAccountType(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                >
                  <option value="SAVINGS">SAVINGS (Interest bearing personal account)</option>
                  <option value="CURRENT">CURRENT (Business & high-volume depository)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Initial Opening Deposit ($)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  required
                  value={initialDeposit}
                  onChange={(e) => setInitialDeposit(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div className="pt-2 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsNewAccountOpen(false)}
                  className="w-1/2 py-2.5 rounded-xl border border-slate-300 text-slate-700 font-semibold text-sm hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={modalLoading}
                  className="w-1/2 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm shadow-md transition disabled:opacity-50"
                >
                  {modalLoading ? 'Provisioning...' : 'Confirm Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Deposit / Withdraw Action Modal */}
      {activeModal && targetAccount && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <h3 className="text-lg font-bold text-slate-900">
                {activeModal === 'DEPOSIT' ? 'Deposit Cash' : 'Withdraw Cash'}
              </h3>
              <button onClick={() => setActiveModal(null)} className="text-slate-400 hover:text-slate-600">
                <X size={20} />
              </button>
            </div>

            <div className="my-3 p-3 bg-slate-50 rounded-xl border border-slate-100">
              <p className="text-xs text-slate-500">Selected Account</p>
              <p className="text-sm font-bold text-slate-800">
                {targetAccount.accountType} - {targetAccount.accountNumber}
              </p>
              <p className="text-xs text-slate-500 mt-1">
                Available: ${Number(targetAccount.balance).toFixed(2)}
              </p>
            </div>

            {modalError && (
              <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs font-medium flex items-center gap-2 mb-3">
                <AlertCircle size={16} />
                <span>{modalError}</span>
              </div>
            )}
            {modalSuccess && (
              <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-700 text-xs font-medium flex items-center gap-2 mb-3">
                <CheckCircle2 size={16} />
                <span>{modalSuccess}</span>
              </div>
            )}

            <form onSubmit={handleActionSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Amount ($)</label>
                <input
                  type="number"
                  step="0.01"
                  min="1"
                  required
                  placeholder="100.00"
                  value={actionAmount}
                  onChange={(e) => setActionAmount(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Description (Optional)</label>
                <input
                  type="text"
                  placeholder={activeModal === 'DEPOSIT' ? 'Cash deposit' : 'ATM Withdrawal'}
                  value={actionDesc}
                  onChange={(e) => setActionDesc(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div className="pt-2 flex gap-3">
                <button
                  type="button"
                  onClick={() => setActiveModal(null)}
                  className="w-1/2 py-2.5 rounded-xl border border-slate-300 text-slate-700 font-semibold text-sm hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={modalLoading}
                  className={`w-1/2 py-2.5 rounded-xl text-white font-semibold text-sm shadow-md transition disabled:opacity-50 ${
                    activeModal === 'DEPOSIT' ? 'bg-emerald-600 hover:bg-emerald-500' : 'bg-amber-600 hover:bg-amber-500'
                  }`}
                >
                  {modalLoading ? 'Processing...' : activeModal === 'DEPOSIT' ? 'Deposit' : 'Withdraw'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
