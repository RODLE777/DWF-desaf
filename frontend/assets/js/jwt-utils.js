// JWT Utility Functions
const JwtUtils = {
  // Decode JWT without verification (client-side only)
  parseJwt(token) {
    if (!token) return null;
    
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('[v0] Error decoding JWT:', error);
      return null;
    }
  },
  
  // Check if token is expired
  isTokenExpired(token) {
    const decoded = this.parseJwt(token);
    if (!decoded) return true;
    
    const expirationTime = decoded.exp * 1000; // exp is in seconds, convert to ms
    return Date.now() >= expirationTime;
  },
  
  // Extract user role from token
  getUserRole(token) {
    const decoded = this.parseJwt(token);
    if (!decoded) return null;
    
    // Roles can be in 'roles' array or 'authorities' array
    const roles = decoded.roles || decoded.authorities || [];
    return roles.length > 0 ? roles[0] : null;
  },
  
  // Extract username from token
  getUsername(token) {
    const decoded = this.parseJwt(token);
    return decoded ? decoded.sub || decoded.username : null;
  },
  
  // Check if user is admin
  isAdmin(token) {
    const role = this.getUserRole(token);
    return role && role.includes('ADMIN');
  },
  
  // Get all roles from token
  getAllRoles(token) {
    const decoded = this.parseJwt(token);
    return decoded ? (decoded.roles || decoded.authorities || []) : [];
  }
};

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = JwtUtils;
}
