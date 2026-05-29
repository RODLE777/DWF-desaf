/**
 * Guards - Protección de rutas basada en autenticación y roles
 */

class Guards {
  /**
   * Guard de autenticación
   * Redirige a login si no está autenticado
   */
  static requireAuth() {
    if (!AuthService.isAuthenticated()) {
      window.location.href = CONFIG.ROUTES.LOGIN;
      return false;
    }
    return true;
  }

  /**
   * Guard de rol de administrador
   * Redirige al dashboard si no es admin
   */
  static requireAdmin() {
    if (!AuthService.isAuthenticated()) {
      window.location.href = CONFIG.ROUTES.LOGIN;
      return false;
    }

    if (!AuthService.isAdmin()) {
      window.location.href = CONFIG.ROUTES.DASHBOARD;
      return false;
    }

    return true;
  }

  /**
   * Guard de rol de usuario
   * Redirige si no es usuario
   */
  static requireUser() {
    if (!AuthService.isAuthenticated()) {
      window.location.href = CONFIG.ROUTES.LOGIN;
      return false;
    }

    if (!AuthService.hasRole(CONFIG.ROLES.USER)) {
      window.location.href = CONFIG.ROUTES.DASHBOARD;
      return false;
    }

    return true;
  }

  /**
   * Guard para rutas públicas
   * Redirige al dashboard si ya está autenticado
   */
  static requireGuest() {
    if (AuthService.isAuthenticated()) {
      window.location.href = CONFIG.ROUTES.DASHBOARD;
      return false;
    }
    return true;
  }
}

// Ejecutar guard de autenticación según la página
document.addEventListener('DOMContentLoaded', () => {
  const currentPage = window.location.pathname;

  // Rutas que requieren autenticación
  const protectedRoutes = [
    CONFIG.ROUTES.DASHBOARD,
    CONFIG.ROUTES.EVENTS,
    CONFIG.ROUTES.EVENT_DETAIL,
    CONFIG.ROUTES.BOOKINGS,
    CONFIG.ROUTES.ADMIN
  ];

  // Rutas que requieren ser guest (no autenticado)
  const guestRoutes = [
    CONFIG.ROUTES.LOGIN,
    CONFIG.ROUTES.REGISTER
  ];

  if (protectedRoutes.some(route => currentPage.includes(route))) {
    Guards.requireAuth();
  }

  if (guestRoutes.some(route => currentPage.includes(route))) {
    Guards.requireGuest();
  }

  // Validar admin para rutas administrativas
  if (currentPage.includes(CONFIG.ROUTES.ADMIN)) {
    if (!AuthService.isAdmin()) {
      window.location.href = CONFIG.ROUTES.DASHBOARD;
    }
  }
});
