import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Wallet,
  ArrowLeftRight,
  History,
  Users,
  User,
  Shield,
  LogOut,
  Menu,
  X,
  FileText,
  CreditCard,
  Building2,
} from 'lucide-react';

export const Layout = () => {
  const { user, isAdmin, logout } = useAuth();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const customerNavItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Accounts', path: '/accounts', icon: Wallet },
    { name: 'Transfer Money', path: '/transfer', icon: ArrowLeftRight },
    { name: 'Transactions', path: '/transactions', icon: History },
    { name: 'Beneficiaries', path: '/beneficiaries', icon: Users },
    { name: 'Statements', path: '/statements', icon: FileText },
    { name: 'Profile', path: '/profile', icon: User },
  ];

  const adminNavItems = [
    { name: 'Admin Dashboard', path: '/admin', icon: Shield },
    { name: 'Manage Users', path: '/admin/users', icon: Users },
    { name: 'Manage Accounts', path: '/admin/accounts', icon: CreditCard },
    { name: 'All Transactions', path: '/admin/transactions', icon: History },
  ];

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col md:flex-row">
      {/* Mobile Topbar */}
      <div className="md:hidden bg-white border-b border-slate-200 px-4 py-3 flex items-center justify-between sticky top-0 z-30">
        <div className="flex items-center space-x-2">
          <div className="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center text-white font-bold text-xl shadow-sm shadow-blue-200">
            B
          </div>
          <span className="text-xl font-bold tracking-tight text-slate-900">BankFlow</span>
        </div>
        <button
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          className="p-2 rounded-lg text-slate-600 hover:bg-slate-100"
        >
          {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Sidebar for Desktop */}
      <aside
        className={`fixed md:sticky top-0 left-0 z-40 h-screen w-64 bg-slate-900 text-white flex flex-col transition-transform duration-300 ease-in-out ${
          isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        }`}
      >
        {/* Brand Header */}
        <div className="p-6 border-b border-slate-800 flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center text-white font-bold text-2xl shadow-lg shadow-blue-900/50">
            BF
          </div>
          <div>
            <h1 className="text-xl font-extrabold tracking-tight text-white leading-tight">BankFlow</h1>
            <p className="text-xs text-slate-400 font-medium tracking-wide uppercase">Core Banking</p>
          </div>
        </div>

        {/* User Mini Profile */}
        <div className="p-4 mx-3 my-3 rounded-xl bg-slate-800/80 border border-slate-700/60 flex items-center space-x-3">
          <div className="w-10 h-10 rounded-full bg-blue-600/30 text-blue-400 font-bold flex items-center justify-center border border-blue-500/30">
            {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-white truncate">{user?.name || 'Customer'}</p>
            <span
              className={`inline-block px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider rounded-full ${
                isAdmin ? 'bg-purple-900/60 text-purple-300 border border-purple-700/50' : 'bg-blue-900/60 text-blue-300 border border-blue-700/50'
              }`}
            >
              {isAdmin ? 'ADMIN' : 'CUSTOMER'}
            </span>
          </div>
        </div>

        {/* Nav Links */}
        <nav className="flex-1 px-3 py-2 space-y-1 overflow-y-auto">
          <p className="px-3 pt-2 pb-1 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
            Banking Services
          </p>
          {customerNavItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => setIsMobileMenuOpen(false)}
                className={({ isActive }) =>
                  `flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-blue-600 text-white shadow-sm shadow-blue-500/30'
                      : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                  }`
                }
              >
                <Icon size={18} />
                <span>{item.name}</span>
              </NavLink>
            );
          })}

          {isAdmin && (
            <>
              <p className="px-3 pt-4 pb-1 text-[11px] font-semibold uppercase tracking-wider text-amber-400">
                Administration
              </p>
              {adminNavItems.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.path}
                    to={item.path}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className={({ isActive }) =>
                      `flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                        isActive
                          ? 'bg-amber-600 text-white shadow-sm shadow-amber-500/30'
                          : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                      }`
                    }
                  >
                    <Icon size={18} />
                    <span>{item.name}</span>
                  </NavLink>
                );
              })}
            </>
          )}
        </nav>

        {/* Logout Footer */}
        <div className="p-4 border-t border-slate-800">
          <button
            onClick={logout}
            className="w-full flex items-center justify-center space-x-2 px-4 py-2.5 rounded-lg text-sm font-semibold text-rose-300 bg-rose-950/40 hover:bg-rose-900/60 border border-rose-800/40 transition-colors"
          >
            <LogOut size={16} />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* Main Content Viewport */}
      <main className="flex-1 flex flex-col min-w-0 overflow-x-hidden">
        <div className="p-4 md:p-8 max-w-7xl w-full mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
};
