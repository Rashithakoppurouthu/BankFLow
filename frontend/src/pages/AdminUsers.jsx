import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { Users, Shield, Mail, Phone, Calendar, RefreshCw } from 'lucide-react';

export const AdminUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getUsers();
      setUsers(res.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">Customer Directory</h2>
          <p className="text-sm text-slate-500">Overview of all registered customers, credentials, and roles.</p>
        </div>
        <button
          onClick={fetchUsers}
          className="flex items-center space-x-2 px-3 py-2 bg-white border border-slate-300 rounded-xl text-xs font-semibold text-slate-700 hover:bg-slate-50 transition self-start sm:self-auto"
        >
          <RefreshCw size={14} />
          <span>Refresh List</span>
        </button>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-400">Loading user directory...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-[11px] font-bold uppercase tracking-wider">
                <tr>
                  <th className="py-3.5 px-6">User ID</th>
                  <th className="py-3.5 px-6">Full Name</th>
                  <th className="py-3.5 px-6">Email Address</th>
                  <th className="py-3.5 px-6">Phone</th>
                  <th className="py-3.5 px-6">Roles</th>
                  <th className="py-3.5 px-6">Registered On</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3.5 px-6 font-mono text-xs font-bold text-slate-500">#{u.id}</td>
                    <td className="py-3.5 px-6 font-bold text-slate-900">{u.fullName}</td>
                    <td className="py-3.5 px-6 text-slate-600 font-mono text-xs">{u.email}</td>
                    <td className="py-3.5 px-6 text-slate-600 text-xs">{u.phone}</td>
                    <td className="py-3.5 px-6">
                      <div className="flex gap-1.5 flex-wrap">
                        {u.roles?.map((r) => (
                          <span
                            key={r}
                            className={`px-2 py-0.5 rounded text-[10px] font-extrabold uppercase ${
                              r === 'ROLE_ADMIN'
                                ? 'bg-purple-100 text-purple-800'
                                : 'bg-blue-100 text-blue-800'
                            }`}
                          >
                            {r.replace('ROLE_', '')}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-3.5 px-6 text-xs text-slate-400">
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
