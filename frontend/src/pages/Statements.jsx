import React, { useState, useEffect } from 'react';
import { accountApi, statementApi } from '../services/api';
import { FileText, Download, Calendar, ArrowDownLeft, ArrowUpRight, ArrowLeftRight, CheckCircle2 } from 'lucide-react';

export const Statements = () => {
  const [accounts, setAccounts] = useState([]);
  const [selectedAccountId, setSelectedAccountId] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [statement, setStatement] = useState(null);
  const [loading, setLoading] = useState(false);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const res = await accountApi.getAccounts();
        const accs = res.data.data || [];
        setAccounts(accs);
        if (accs.length > 0) {
          setSelectedAccountId(accs[0].id);
        }
      } catch (err) {
        console.error(err);
      }
    };
    fetchAccounts();
  }, []);

  const handleGenerateStatement = async () => {
    if (!selectedAccountId) return;
    try {
      setLoading(true);
      const params = {};
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;

      const res = await statementApi.getStatement(selectedAccountId, params);
      setStatement(res.data.data);
    } catch (err) {
      console.error(err);
      alert('Failed to generate statement');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedAccountId) {
      handleGenerateStatement();
    }
  }, [selectedAccountId]);

  const handleDownloadPdf = async () => {
    if (!selectedAccountId) return;
    try {
      setDownloading(true);
      const params = {};
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;

      const res = await statementApi.downloadPdf(selectedAccountId, params);
      const blob = new Blob([res.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `statement-${selectedAccountId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error(err);
      alert('Failed to download PDF statement');
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">Account Statements</h2>
          <p className="text-sm text-slate-500">
            Generate and export official PDF transaction statements for tax and audit compliance.
          </p>
        </div>
        <button
          onClick={handleDownloadPdf}
          disabled={downloading || !statement}
          className="flex items-center space-x-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 text-white rounded-xl text-sm font-bold shadow-md shadow-blue-500/20 transition disabled:opacity-50 self-start sm:self-auto"
        >
          <Download size={16} />
          <span>{downloading ? 'Exporting PDF...' : 'Download Official PDF'}</span>
        </button>
      </div>

      {/* Filter Options */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Select Account</label>
            <select
              value={selectedAccountId}
              onChange={(e) => setSelectedAccountId(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-xs font-semibold text-slate-800 focus:ring-2 focus:ring-blue-500 focus:outline-none"
            >
              {accounts.map((acc) => (
                <option key={acc.id} value={acc.id}>
                  {acc.accountType} - {acc.accountNumber}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Statement Start Date</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-xs font-semibold text-slate-800 focus:ring-2 focus:ring-blue-500 focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Statement End Date</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-xs font-semibold text-slate-800 focus:ring-2 focus:ring-blue-500 focus:outline-none"
            />
          </div>
        </div>

        <div className="flex justify-end pt-1">
          <button
            onClick={handleGenerateStatement}
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-semibold transition"
          >
            Apply Date Range
          </button>
        </div>
      </div>

      {/* Statement Preview Card */}
      {statement && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden p-6 sm:p-8 space-y-6">
          <div className="flex flex-col md:flex-row md:items-center justify-between pb-6 border-b border-slate-100 gap-4">
            <div>
              <span className="text-xs font-bold text-blue-600 uppercase tracking-widest">BankFlow Official</span>
              <h3 className="text-xl font-black text-slate-900 mt-0.5">Account Statement</h3>
              <p className="text-xs text-slate-400 mt-1">Generated electronically with cryptographic signature</p>
            </div>
            <div className="text-left md:text-right">
              <p className="text-xs text-slate-500 font-medium">Account Holder</p>
              <p className="text-base font-extrabold text-slate-900">{statement.customerName}</p>
              <p className="text-xs font-mono text-slate-600 mt-0.5">{statement.accountNumber}</p>
            </div>
          </div>

          {/* Key Summary Stats */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 bg-slate-50 rounded-xl border border-slate-100">
            <div>
              <p className="text-[11px] text-slate-500 font-medium">Account Category</p>
              <p className="text-sm font-bold text-slate-800">{statement.accountType}</p>
            </div>
            <div>
              <p className="text-[11px] text-slate-500 font-medium">Status</p>
              <span className="inline-block px-2 py-0.5 rounded text-[11px] font-bold bg-emerald-100 text-emerald-800">
                {statement.status}
              </span>
            </div>
            <div>
              <p className="text-[11px] text-slate-500 font-medium">Current Balance</p>
              <p className="text-sm font-black text-slate-900">${Number(statement.currentBalance).toFixed(2)}</p>
            </div>
            <div>
              <p className="text-[11px] text-slate-500 font-medium">Activity Items</p>
              <p className="text-sm font-bold text-slate-800">{statement.transactions?.length || 0} Transactions</p>
            </div>
          </div>

          {/* Activity Ledger */}
          <div>
            <h4 className="text-sm font-bold uppercase tracking-wider text-slate-700 mb-3">Transaction History</h4>
            {statement.transactions?.length === 0 ? (
              <p className="text-center py-8 text-sm text-slate-400">No transactions during this period.</p>
            ) : (
              <div className="overflow-x-auto border border-slate-100 rounded-xl">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-50 text-slate-500 font-bold uppercase tracking-wider">
                    <tr>
                      <th className="py-3 px-4">Date & Time</th>
                      <th className="py-3 px-4">Reference</th>
                      <th className="py-3 px-4">Type</th>
                      <th className="py-3 px-4">Description</th>
                      <th className="py-3 px-4 text-right">Amount</th>
                      <th className="py-3 px-4 text-right">Balance After</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {statement.transactions.map((tx) => {
                      const isDeposit = tx.type === 'DEPOSIT';
                      return (
                        <tr key={tx.id} className="hover:bg-slate-50/60">
                          <td className="py-3 px-4 text-slate-500">{new Date(tx.timestamp).toLocaleString()}</td>
                          <td className="py-3 px-4 font-mono text-slate-700">{tx.transactionReference}</td>
                          <td className="py-3 px-4 font-semibold">{tx.type}</td>
                          <td className="py-3 px-4 text-slate-600">{tx.description || '-'}</td>
                          <td className={`py-3 px-4 text-right font-bold ${isDeposit ? 'text-emerald-600' : 'text-slate-900'}`}>
                            {isDeposit ? '+' : '-'}${Number(tx.amount).toFixed(2)}
                          </td>
                          <td className="py-3 px-4 text-right font-mono font-semibold text-slate-800">
                            ${Number(tx.balanceAfterTransaction).toFixed(2)}
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
      )}
    </div>
  );
};
