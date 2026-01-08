import { createContext, useContext, useState, useEffect } from 'react';
import authService from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const token = authService.getToken();
      if (token) {
        // Try to get user from localStorage first for faster load
        const storedUser = authService.getStoredUser();
        if (storedUser) {
          setUser(storedUser);
        }
        
        // Then verify with backend
        try {
          const userData = await authService.getCurrentUser();
          setUser(userData);
          localStorage.setItem('user', JSON.stringify(userData));
        } catch (error) {
          // Token might be expired
          console.error('Token validation failed:', error);
          authService.logout();
          setUser(null);
        }
      } else {
        setUser(null);
      }
    } catch (error) {
      console.error('Auth check failed:', error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    const response = await authService.login(email, password);
    setUser(response.user);
    return response;
  };

  const register = async (firstName, lastName, email, companyName, password, confirmPassword) => {
    const response = await authService.register(firstName, lastName, email, companyName, password, confirmPassword);
    return response;
  };

  const verifyEmail = async (email, code) => {
    const response = await authService.verifyEmail(email, code);
    return response;
  };

  const resendCode = async (email) => {
    const response = await authService.resendCode(email);
    return response;
  };

  const forgotPassword = async (email) => {
    const response = await authService.forgotPassword(email);
    return response;
  };

  const resetPassword = async (email, code, newPassword) => {
    const response = await authService.resetPassword(email, code, newPassword);
    return response;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const value = {
    user,
    loading,
    login,
    register,
    verifyEmail,
    resendCode,
    forgotPassword,
    resetPassword,
    logout,
    checkAuthStatus,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}