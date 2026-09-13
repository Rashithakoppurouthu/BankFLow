import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import {
  History,
  ArrowDownLeft,
  ArrowUpRight,
  ArrowLeftRight,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
} from 'lucide-react';

export const AdminTransactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(15);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getTransactions(page, size);
      const pageData = res.data.data;
      setTransactions(pageData.content || []);
      setTotalPages(pageData.totalPages || 0);
      setTotalElements(pageData.totalElements || 0);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, [page]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">System Transaction Audit</h2>
          <p className="text-sm text-slate-500">Immutable ledger of all financial activities across the institution.</p>
        </div>
        <button
          onClick={fetchTransactions}
          className="flex items-center space-x-2 px-3 py-2 bg-white border border-slate-300 rounded-xl text-xs font-semibold text-slate-700 hover:bg-slate-50 transition self-start sm:self-auto"
        >
          <RefreshCw size={14} />
          <span>Refresh</span>
        </button>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-400">Loading full transaction ledger...</div>
        ) : transactions.length === 0 ? (
          <div className="p-12 text-center text-slate-400">No transactions recorded yet.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-[11px] font-bold uppercase tracking-wider">
                <tr>
                  <th className="py-3.5 px-6">Type</th>
                  <th className="py-3.5 px-6">Reference</th>
                  <th className="py-3.5 px-6">A/C Number</th>
                  <th className="py-3.5 px-6">Counterparty</th>
                  <th className="py-3.5 px-6">Amount</th>
                  <th className="py-3.5 px-6">Description</th>
                  <th className="py-3.5 px-6">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {transactions.map((tx) => {
                  const isDeposit = tx.type === 'DEPOSIT';
                  const isWithdrawal = tx.type === 'WITHDRAWAL';
                  return (
                    <tr key={tx.id} className="hover:bg-slate-50/80 transition">
                      <td className="py-3.5 px-6">
                        <span
                          className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold ${
                            isDeposit
                              ? 'bg-emerald-50 text-emerald-700'
                              : isWithdrawal
                              ? 'bg-amber-50 text-amber-700'
                              : 'bg-blue-50 text-blue-700'
                          }`}
                        >
                          {isDeposit && <ArrowDownLeft size={12} />}
                          {isWithdrawal && <ArrowUpRight size={12} />}
                          {!isDeposit && !isWithdrawal && <ArrowLeftRight size={12} />}
                          {tx.type}
                        </span>
                      </td>
                      <td className="py-3.5 px-6 font-mono text-xs text-slate-700">{tx.transactionReference}</td>
                      <td className="py-3.5 px-6 font-mono text-xs font-bold text-slate-800">{tx.accountNumber}</td>
                      <td className="py-3.5 px-6 font-mono text-xs text-slate-500">
                        {tx.targetAccountNumber || '-'}
                      </td>
                      <td className="py-3.5 px-6 font-bold text-slate-900">${Number(tx.amount).toFixed(2)}</td>
                      <td className="py-3.5 px-6 text-slate-600 text-xs">{tx.description || '-'}</td>
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

        {totalPages > 1 && (
          <div className="p-4 border-t border-slate-100 flex items-center justify-between">
            <span className="text-xs text-slate-500 font-medium">
              Page <span className="font-bold text-slate-800">{page + 1}</span> of{' '}
              <span className="font-bold text-slate-800">{totalPages}</span> ({totalElements} entries)
            </span>
            <div className="flex items-center gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                className="p-2 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40 transition"
              >
                <ChevronLeft size={16} />
              </button>
              <button
                disabled={page + 1 >= totalPages}
                onClick={() => setPage(page + 1)}
                className="p-2 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40 transition"
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
