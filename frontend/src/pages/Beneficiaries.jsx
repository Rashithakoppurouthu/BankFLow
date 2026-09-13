import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { beneficiaryApi } from '../services/api';
import { Users, PlusCircle, Trash2, ArrowLeftRight, Building2, AlertCircle, CheckCircle2, X } from 'lucide-react';

export const Beneficiaries = () => {
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [loading, setLoading] = useState(true);

  // Add Beneficiary Modal State
  const [isOpen, setIsOpen] = useState(false);
  const [formData, setFormData] = useState({
    beneficiaryName: '',
    accountNumber: '',
    bankName: '',
    ifscCode: '',
  });
  const [modalLoading, setModalLoading] = useState(false);
  const [modalError, setModalError] = useState('');
  const [modalSuccess, setModalSuccess] = useState('');

  const fetchBeneficiaries = async () => {
    try {
      setLoading(true);
      const res = await beneficiaryApi.getBeneficiaries();
      setBeneficiaries(res.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBeneficiaries();
  }, []);

  const handleAddSubmit = async (e) => {
    e.preventDefault();
    setModalError('');
    setModalSuccess('');
    setModalLoading(true);

    try {
      await beneficiaryApi.addBeneficiary(formData);
      setModalSuccess('Beneficiary added successfully!');
      setFormData({ beneficiaryName: '', accountNumber: '', bankName: '', ifscCode: '' });
      await fetchBeneficiaries();
      setTimeout(() => {
        setIsOpen(false);
        setModalSuccess('');
      }, 1000);
    } catch (err) {
      setModalError(err.response?.data?.message || 'Failed to add beneficiary');
    } finally {
      setModalLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to remove this payee?')) return;
    try {
      await beneficiaryApi.deleteBeneficiary(id);
      setBeneficiaries(beneficiaries.filter((b) => b.id !== id));
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete beneficiary');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">Saved Beneficiaries</h2>
          <p className="text-sm text-slate-500">Manage verified payees for instant and recurring transfers.</p>
        </div>
        <button
          onClick={() => {
            setIsOpen(true);
            setModalError('');
            setModalSuccess('');
          }}
          className="flex items-center space-x-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 rounded-xl text-white font-semibold text-sm shadow-md shadow-blue-500/20 transition self-start sm:self-auto"
        >
          <PlusCircle size={16} />
          <span>Add New Payee</span>
        </button>
      </div>

      {loading ? (
        <div className="p-12 text-center text-slate-400">Loading beneficiaries...</div>
      ) : beneficiaries.length === 0 ? (
        <div className="bg-white rounded-2xl p-12 text-center border border-slate-200">
          <Users size={48} className="mx-auto text-slate-300 mb-3" />
          <h3 className="text-lg font-bold text-slate-700">No saved beneficiaries yet</h3>
          <p className="text-sm text-slate-500 mt-1">Add trusted bank payees to transfer money quickly.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {beneficiaries.map((b) => (
            <div
              key={b.id}
              className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between">
                  <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
                    <Building2 size={20} />
                  </div>
                  <button
                    onClick={() => handleDelete(b.id)}
                    className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg hover:bg-rose-50 transition"
                    title="Remove payee"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>

                <h4 className="text-lg font-extrabold text-slate-900 mt-4">{b.beneficiaryName}</h4>
                <p className="text-xs text-slate-500 font-medium">{b.bankName}</p>

                <div className="mt-4 pt-3 border-t border-slate-100 space-y-1.5">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-400 font-medium">A/C Number:</span>
                    <span className="font-mono font-bold text-slate-800">{b.accountNumber}</span>
                  </div>
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-400 font-medium">IFSC Code:</span>
                    <span className="font-mono font-bold text-slate-800">{b.ifscCode}</span>
                  </div>
                </div>
              </div>

              <div className="mt-5 pt-3 border-t border-slate-100">
                <Link
                  to="/transfer"
                  className="w-full flex items-center justify-center space-x-2 py-2 px-3 bg-blue-50 hover:bg-blue-100 text-blue-700 rounded-xl text-xs font-bold transition"
                >
                  <ArrowLeftRight size={14} />
                  <span>Send Money</span>
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Beneficiary Modal */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <h3 className="text-lg font-bold text-slate-900">Add Trusted Payee</h3>
              <button onClick={() => setIsOpen(false)} className="text-slate-400 hover:text-slate-600">
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

            <form onSubmit={handleAddSubmit} className="mt-4 space-y-3.5">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Payee Full Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Alice Walker"
                  value={formData.beneficiaryName}
                  onChange={(e) => setFormData({ ...formData, beneficiaryName: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Account Number</label>
                <input
                  type="text"
                  required
                  placeholder="10 to 16 digits"
                  value={formData.accountNumber}
                  onChange={(e) => setFormData({ ...formData, accountNumber: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm font-mono focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Bank Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. BankFlow National or Chase"
                  value={formData.bankName}
                  onChange={(e) => setFormData({ ...formData, bankName: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">IFSC Code</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. SBIN0001234"
                  value={formData.ifscCode}
                  onChange={(e) => setFormData({ ...formData, ifscCode: e.target.value.toUpperCase() })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm font-mono uppercase focus:ring-2 focus:ring-blue-500 focus:outline-none"
                />
              </div>

              <div className="pt-2 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsOpen(false)}
                  className="w-1/2 py-2.5 rounded-xl border border-slate-300 text-slate-700 font-semibold text-sm hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={modalLoading}
                  className="w-1/2 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm shadow-md transition disabled:opacity-50"
                >
                  {modalLoading ? 'Saving...' : 'Save Payee'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
