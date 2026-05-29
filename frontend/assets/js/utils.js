// Utility Functions
const Utils = {
  // Validate email format
  isValidEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  },
  
  // Validate username (3-50 chars)
  isValidUsername(username) {
    return username && username.length >= 3 && username.length <= 50;
  },
  
  // Validate password (minimum 6 chars)
  isValidPassword(password) {
    return password && password.length >= 6;
  },
  
  // Validate age (1-150)
  isValidAge(age) {
    const ageNum = parseInt(age);
    return !isNaN(ageNum) && ageNum >= 1 && ageNum <= 150;
  },
  
  // Validate positive integer
  isValidInteger(value) {
    const num = parseInt(value);
    return !isNaN(num) && num > 0;
  },
  
  // Trim whitespace
  trim(text) {
    return text ? text.trim() : '';
  },
  
  // Calculate days until event
  daysUntilEvent(eventDate) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const event = new Date(eventDate);
    event.setHours(0, 0, 0, 0);
    
    const diffTime = event - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    return diffDays;
  },
  
  // Format booking status
  formatBookingStatus(status) {
    const statusMap = {
      'CONFIRMED': 'Confirmada',
      'CANCELLED': 'Cancelada'
    };
    return statusMap[status] || status;
  },
  
  // Format role
  formatRole(role) {
    const roleMap = {
      'ROLE_ADMIN': 'Administrador',
      'ROLE_USER': 'Usuario'
    };
    return roleMap[role] || role;
  },
  
  // Generate unique ID
  generateId() {
    return '_' + Math.random().toString(36).substr(2, 9);
  },
  
  // Debounce function
  debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  },
  
  // Throttle function
  throttle(func, limit) {
    let inThrottle;
    return function(...args) {
      if (!inThrottle) {
        func.apply(this, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    };
  }
};
