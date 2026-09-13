import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { accountApi, transactionApi } from '../services/api';
import {
  Wallet,
  ArrowUpRight,
  ArrowDownLeft,
  ArrowLeftRight,
  CreditCard,
  PlusCircle,
  Clock,
  CheckCircle2,
  AlertCircle,
  X,
} from 'lucide-react';

export const Dashboard = () => {
  const { user } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  // Quick Deposit Modal State
  const [isDepositModalOpen, setIsDepositModalOpen] = useState(false);
  const [selectedAccountId, setSelectedAccountId] = useState('');
  const [depositAmount, setDepositAmount] = useState('');
  const [depositDesc, setDepositDesc] = useState('');
  const [depositSubmitting, setDepositSubmitting] = useState(false);
  const [modalError, setModalError] = useState('');
  const [modalSuccess, setModalSuccess] = useState('');

  const fetchData = async () => {
    try {
      setLoading(true);
      const [accRes, txRes] = await Promise.all([
        accountApi.getAccounts(),
        transactionApi.getRecentTransactions(6),
      ]);
      setAccounts(accRes.data.data || []);
      setRecentTransactions(txRes.data.data || []);
      if (accRes.data.data?.length > 0 && !selectedAccountId) {
        setSelectedAccountId(accRes.data.data[0].id);
      }
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const totalBalance = accounts.reduce((acc, curr) => acc + Number(curr.balance || 0), 0);

  const handleDepositSubmit = async (e) => {
    e.preventDefault();
    setModalError('');
    setModalSuccess('');
    setDepositSubmitting(true);

    try {
      await accountApi.deposit(selectedAccountId, {
        amount: Number(depositAmount),
        description: depositDesc || 'Quick Deposit',
      });
      setModalSuccess('Deposit processed successfully!');
      setDepositAmount('');
      setDepositDesc('');
      await fetchData();
      setTimeout(() => {
        setIsDepositModalOpen(false);
        setModalSuccess('');
      }, 1200);
    } catch (err) {
      setModalError(err.response?.data?.message || 'Deposit failed');
    } finally {
      setDepositSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-slate-900 via-blue-950 to-slate-900 p-6 md:p-8 rounded-2xl text-white shadow-xl shadow-slate-900/10 border border-slate-800">
        <div>
          <span className="text-blue-400 text-xs font-bold uppercase tracking-widest">Customer Portal</span>
          <h2 className="text-2xl md:text-3xl font-extrabold mt-1">Welcome back, {user?.name || 'Customer'}!</h2>
          <p className="text-slate-400 text-sm mt-1">Manage accounts, transfer funds instantly, and monitor real-time activity.</p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => {
              setIsDepositModalOpen(true);
              setModalError('');
              setModalSuccess('');
            }}
            className="flex items-center space-x-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 rounded-xl text-sm font-semibold transition shadow-md shadow-blue-500/20"
          >
            <PlusCircle size={16} />
            <span>Quick Deposit</span>
          </button>
          <Link
            to="/transfer"
            className="flex items-center space-x-2 px-4 py-2.5 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl text-sm font-semibold transition"
          >
            <ArrowLeftRight size={16} />
            <span>Transfer Funds</span>
          </Link>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Total Depository Balance</span>
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
              <Wallet size={20} />
            </div>
          </div>
          <p className="text-3xl font-extrabold text-slate-900 mt-3">
            ${totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
          <span className="text-xs text-emerald-600 font-semibold flex items-center gap-1 mt-2">
            <CheckCircle2 size={13} /> Across {accounts.length} linked account{accounts.length === 1 ? '' : 's'}
          </span>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Active Bank Accounts</span>
            <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold">
              <CreditCard size={20} />
            </div>
          </div>
          <p className="text-3xl font-extrabold text-slate-900 mt-3">{accounts.length}</p>
          <Link to="/accounts" className="text-xs text-blue-600 hover:underline font-semibold flex items-center gap-1 mt-2">
            View account details &rarr;
          </Link>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Security & Status</span>
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
              <CheckCircle2 size={20} />
            </div>
          </div>
          <p className="text-xl font-bold text-emerald-700 mt-3">Verified Active</p>
          <span className="text-xs text-slate-500 font-medium flex items-center gap-1 mt-2">
            JWT 256-bit Encrypted Session
          </span>
        </div>
      </div>

      {/* Linked Accounts Carousel / Grid */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-bold text-slate-900">Your Bank Accounts</h3>
          <Link to="/accounts" className="text-sm font-semibold text-blue-600 hover:text-blue-700">
            Manage All
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {accounts.map((acc) => (
            <div
              key={acc.id}
              className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:border-blue-300 transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold px-2.5 py-1 rounded-full bg-slate-100 text-slate-700 uppercase">
                    {acc.accountType}
                  </span>
                  <span
                    className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                      acc.status === 'ACTIVE'
                        ? 'bg-emerald-50 text-emerald-700'
                        : 'bg-rose-50 text-rose-700'
                    }`}
                  >
                    {acc.status}
                  </span>
                </div>
                <p className="text-xs font-mono text-slate-400 mt-4 tracking-wider">ACCOUNT NUMBER</p>
                <p className="text-lg font-mono font-bold text-slate-800 tracking-wider">
                  {acc.accountNumber.replace(/(\d{4})/g, '$1 ').trim()}
                </p>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 flex items-end justify-between">
                <div>
                  <p className="text-xs text-slate-500 font-medium">Available Balance</p>
                  <p className="text-2xl font-extrabold text-slate-900">
                    ${Number(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </p>
                </div>
                <Link
                  to={`/accounts/${acc.id}`}
                  className="px-3 py-1.5 bg-slate-50 hover:bg-blue-50 text-blue-600 rounded-lg text-xs font-bold transition"
                >
                  Details
                </Link>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Activity Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold text-slate-900">Recent Transactions</h3>
            <p className="text-xs text-slate-500">Live record of deposits, withdrawals, and inter-account transfers</p>
          </div>
          <Link to="/transactions" className="text-sm font-semibold text-blue-600 hover:text-blue-700">
            View All Ledger
          </Link>
        </div>

        {recentTransactions.length === 0 ? (
          <div className="p-8 text-center text-slate-400 text-sm">
            No transactions found yet. Initiate a deposit or transfer to see your activity!
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-[11px] font-bold uppercase tracking-wider">
                <tr>
                  <th className="py-3.5 px-6">Type</th>
                  <th className="py-3.5 px-6">Reference</th>
                  <th className="py-3.5 px-6">Account</th>
                  <th className="py-3.5 px-6">Description</th>
                  <th className="py-3.5 px-6">Amount</th>
                  <th className="py-3.5 px-6">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {recentTransactions.map((tx) => {
                  const isDeposit = tx.type === 'DEPOSIT';
                  const isWithdrawal = tx.type === 'WITHDRAWAL';
                  return (
                    <tr key={tx.id} className="hover:bg-slate-50/80 transition">
                      <td className="py-3.5 px-6">
                        <span
                          className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold ${
                            isDeposit
                              ? 'bg-emerald-50 text-emerald-700'
                              : isWithdrawal
                              ? 'bg-amber-50 text-amber-700'
                              : 'bg-blue-50 text-blue-700'
                          }`}
                        >
                          {isDeposit && <ArrowDownLeft size={13} />}
                          {isWithdrawal && <ArrowUpRight size={13} />}
                          {!isDeposit && !isWithdrawal && <ArrowLeftRight size={13} />}
                          {tx.type}
                        </span>
                      </td>
                      <td className="py-3.5 px-6 font-mono text-xs text-slate-600">{tx.transactionReference}</td>
                      <td className="py-3.5 px-6 font-mono text-xs text-slate-800">{tx.accountNumber}</td>
                      <td className="py-3.5 px-6 text-slate-700 font-medium">{tx.description || '-'}</td>
                      <td className="py-3.5 px-6 font-bold">
                        <span className={isDeposit ? 'text-emerald-600' : 'text-slate-900'}>
                          {isDeposit ? '+' : '-'}${Number(tx.amount).toFixed(2)}
                        </span>
                      </td>
                      <td className="py-3.5 px-6 text-xs text-slate-400">
                        {new Date(tx.timestamp).toLocaleString()}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Quick Deposit Modal */}
      {isDepositModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <h3 className="text-lg font-bold text-slate-900">Deposit Funds</h3>
              <button
                onClick={() => setIsDepositModalOpen(false)}
                className="text-slate-400 hover:text-slate-600"
              >
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

            <form onSubmit={handleDepositSubmit} className="mt-4 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Select Account</label>
                <select
                  value={selectedAccountId}
                  onChange={(e) => setSelectedAccountId(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                >
                  {accounts.map((acc) => (
                    <option key={acc.id} value={acc.id}>
                      {acc.accountType} - {acc.accountNumber} (${Number(acc.balance).toFixed(2)})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Deposit Amount ($)</label>
                <input
                  type="number"
                  step="0.01"
                  min="1"
                  required
                  placeholder="500.00"
                  value={depositAmount}
                  onChange={(e) => setDepositAmount(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Description (Optional)</label>
                <input
                  type="text"
                  placeholder="e.g. Salary Deposit"
                  value={depositDesc}
                  onChange={(e) => setDepositDesc(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div className="pt-2 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsDepositModalOpen(false)}
                  className="w-1/2 py-2.5 rounded-xl border border-slate-300 text-slate-700 font-semibold text-sm hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={depositSubmitting}
                  className="w-1/2 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm shadow-md transition disabled:opacity-50"
                >
                  {depositSubmitting ? 'Processing...' : 'Confirm Deposit'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
