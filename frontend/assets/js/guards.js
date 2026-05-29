// Route Guards - Protect routes based on authentication and roles
const RouteGuard = {
  // Check if authenticated
  isAuthenticated() {
    return StorageManager.isAuthenticated();
  },
  
  // Check if admin
  isAdmin() {
    return StorageManager.isAdmin();
  },
  
  // Get current user role
  getUserRole() {
    return StorageManager.getUserRole();
  },
  
  // Require authentication - redirect to login if not authenticated
  requireAuth() {
    if (!this.isAuthenticated()) {
      window.location.href = '/login.html';
      return false;
    }
    return true;
  },
  
  // Require admin role - redirect to events if not admin
  requireAdmin() {
    if (!this.isAuthenticated()) {
      window.location.href = '/login.html';
      return false;
    }
    
    if (!this.isAdmin()) {
      UIManager.showToast('Acceso denegado: requiere permisos de administrador', 'error');
      window.location.href = '/events.html';
      return false;
    }
    
    return true;
  },
  
  // Redirect to events if already authenticated
  requireGuest() {
    if (this.isAuthenticated()) {
      window.location.href = '/events.html';
      return false;
    }
    return true;
  }
};

// Check page permissions on load
document.addEventListener('DOMContentLoaded', () => {
  const currentPage = window.location.pathname;
  
  // Protected pages
  const protectedPages = ['/events.html', '/event-detail.html', '/bookings.html', '/admin.html'];
  const adminPages = ['/admin.html'];
  const guestPages = ['/login.html', '/register.html', '/index.html'];
  
  if (protectedPages.some(page => currentPage.includes(page))) {
    if (!RouteGuard.requireAuth()) return;
  }
  
  if (adminPages.some(page => currentPage.includes(page))) {
    if (!RouteGuard.requireAdmin()) return;
  }
  
  if (guestPages.some(page => currentPage.includes(page))) {
    if (!RouteGuard.requireGuest()) return;
  }
  
  // Update UI based on role
  const token = StorageManager.getToken();
  if (token) {
    const isAdmin = StorageManager.isAdmin();
    const adminButtons = document.querySelectorAll('[data-admin-only]');
    adminButtons.forEach(btn => {
      btn.style.display = isAdmin ? 'inline-block' : 'none';
    });
  }
});
