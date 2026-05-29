// Local Storage Management
const StorageManager = {
  // Save token
  setToken(token) {
    if (token) {
      localStorage.setItem(API_CONFIG.STORAGE_KEYS.TOKEN, token);
    }
  },
  
  // Get token
  getToken() {
    return localStorage.getItem(API_CONFIG.STORAGE_KEYS.TOKEN);
  },
  
  // Save refresh token
  setRefreshToken(token) {
    if (token) {
      localStorage.setItem(API_CONFIG.STORAGE_KEYS.REFRESH_TOKEN, token);
    }
  },
  
  // Get refresh token
  getRefreshToken() {
    return localStorage.getItem(API_CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
  },
  
  // Save user info
  setUser(user) {
    if (user) {
      localStorage.setItem(API_CONFIG.STORAGE_KEYS.USER, JSON.stringify(user));
    }
  },
  
  // Get user info
  getUser() {
    const user = localStorage.getItem(API_CONFIG.STORAGE_KEYS.USER);
    return user ? JSON.parse(user) : null;
  },
  
  // Clear all auth data
  clearAuth() {
    localStorage.removeItem(API_CONFIG.STORAGE_KEYS.TOKEN);
    localStorage.removeItem(API_CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(API_CONFIG.STORAGE_KEYS.USER);
  },
  
  // Check if user is authenticated
  isAuthenticated() {
    const token = this.getToken();
    if (!token) return false;
    
    // Check if token is expired
    return !JwtUtils.isTokenExpired(token);
  },
  
  // Get current user role
  getUserRole() {
    const token = this.getToken();
    return token ? JwtUtils.getUserRole(token) : null;
  },
  
  // Check if current user is admin
  isAdmin() {
    const token = this.getToken();
    return token ? JwtUtils.isAdmin(token) : false;
  }
};
