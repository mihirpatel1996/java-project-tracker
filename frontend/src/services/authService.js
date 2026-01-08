import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle 401 responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Only redirect if not already on auth pages
      const authPages = ['/login', '/register', '/verify', '/forgot-password', '/reset-password'];
      if (!authPages.some(page => window.location.pathname.startsWith(page))) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const authService = {
  async register(firstName, lastName, email, companyName, password, confirmPassword) {
    const response = await api.post('/api/auth/register', {
      firstName,
      lastName,
      email,
      companyName,
      password,
      confirmPassword,
    });
    return response.data;
  },

  async verifyEmail(email, code) {
    const response = await api.post('/api/auth/verify', {
      email,
      code,
    });
    return response.data;
  },

  async resendCode(email) {
    const response = await api.post('/api/auth/resend-code', {
      email,
    });
    return response.data;
  },

  async login(email, password) {
    const response = await api.post('/api/auth/login', {
      email,
      password,
    });
    
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
    
    return response.data;
  },

  async forgotPassword(email) {
    const response = await api.post('/api/auth/forgot-password', {
      email,
    });
    return response.data;
  },

  async resetPassword(email, code, newPassword) {
    const response = await api.post('/api/auth/reset-password', {
      email,
      code,
      newPassword,
    });
    return response.data;
  },

  async getCurrentUser() {
    const response = await api.get('/api/auth/me');
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getToken() {
    return localStorage.getItem('token');
  },

  getStoredUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  isAuthenticated() {
    return !!localStorage.getItem('token');
  },
};

export default authService;