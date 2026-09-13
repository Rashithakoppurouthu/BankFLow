import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Bearer Token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Catch 401 Unauthorized and redirect to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const currentPath = window.location.pathname;
      if (currentPath !== '/login' && currentPath !== '/register') {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login?expired=true';
      }
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  getProfile: () => api.get('/auth/profile'),
  updateProfile: (profileData) => api.put('/auth/profile', profileData),
};

// Bank Account APIs
export const accountApi = {
  getAccounts: () => api.get('/accounts'),
  getAccountById: (id) => api.get(`/accounts/${id}`),
  createAccount: (data) => api.post('/accounts', data),
  deposit: (accountId, data) => api.post(`/accounts/${accountId}/deposit`, data),
  withdraw: (accountId, data) => api.post(`/accounts/${accountId}/withdraw`, data),
};

// Money Transfer APIs
export const transferApi = {
  transfer: (transferData, idempotencyKey = null) => {
    const headers = {};
    if (idempotencyKey) {
      headers['Idempotency-Key'] = idempotencyKey;
    }
    return api.post('/transfers', transferData, { headers });
  },
};

// Beneficiary APIs
export const beneficiaryApi = {
  getBeneficiaries: () => api.get('/beneficiaries'),
  addBeneficiary: (data) => api.post('/beneficiaries', data),
  deleteBeneficiary: (id) => api.delete(`/beneficiaries/${id}`),
};

// Transaction History APIs
export const transactionApi = {
  getTransactions: (params) => api.get('/transactions', { params }),
  getRecentTransactions: (limit = 5) => api.get('/transactions/recent', { params: { limit } }),
};

// Account Statement APIs
export const statementApi = {
  getStatement: (accountId, params) => api.get(`/statements/${accountId}`, { params }),
  downloadPdf: (accountId, params) =>
    api.get(`/statements/${accountId}/pdf`, {
      params,
      responseType: 'blob',
    }),
};

// Admin APIs
export const adminApi = {
  getDashboardStats: () => api.get('/admin/dashboard'),
  getUsers: () => api.get('/admin/users'),
  getAccounts: () => api.get('/admin/accounts'),
  getTransactions: (page = 0, size = 15) =>
    api.get('/admin/transactions', { params: { page, size } }),
  freezeAccount: (id) => api.put(`/admin/accounts/${id}/freeze`),
  unfreezeAccount: (id) => api.put(`/admin/accounts/${id}/unfreeze`),
};

export default api;
