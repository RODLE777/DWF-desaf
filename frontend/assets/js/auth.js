/**
 * Auth Service - Lógica de autenticación
 * Registro, login, logout y gestión de sesión
 */

class AuthService {
  /**
   * Registra un nuevo usuario
   * Consumo del endpoint: POST /api/auth/register
   */
  static async register(userData) {
    try {
      const payload = {
        username: userData.username,
        firstname: userData.firstname,
        lastname: userData.lastname,
        age: parseInt(userData.age),
        password: userData.password
      };

      const response = await api.post(CONFIG.AUTH.REGISTER, payload, {
        includeAuth: false
      });

      // Guarda los tokens y datos del usuario
      this.saveAuthData(response);
      
      return { success: true, data: response };
    } catch (error) {
      console.error('[AuthService] Error en registro:', error);
      return { success: false, error };
    }
  }

  /**
   * Inicia sesión
   * Consumo del endpoint: POST /api/auth/login
   */
  static async login(credentials) {
    try {
      const payload = {
        username: credentials.username,
        password: credentials.password
      };

      const response = await api.post(CONFIG.AUTH.LOGIN, payload, {
        includeAuth: false
      });

      // Guarda los tokens y datos del usuario
      this.saveAuthData(response);
      
      return { success: true, data: response };
    } catch (error) {
      console.error('[AuthService] Error en login:', error);
      return { success: false, error };
    }
  }

  /**
   * Guarda los datos de autenticación en localStorage
   */
  static saveAuthData(response) {
    StorageService.setAccessToken(response.access_token);
    StorageService.setRefreshToken(response.refresh_token);
    StorageService.setRole(response.role);

    const user = {
      username: response.username,
      role: response.role
    };
    StorageService.setUser(user);
  }

  /**
   * Cierra sesión del usuario
   * Limpia tokens y datos de sesión
   */
  static logout() {
    StorageService.clearSession();
    window.location.href = CONFIG.ROUTES.LOGIN;
  }

  /**
   * Obtiene el usuario autenticado actual
   */
  static getCurrentUser() {
    return StorageService.getUser();
  }

  /**
   * Verifica si el usuario tiene un rol específico
   */
  static hasRole(role) {
    const userRole = StorageService.getRole();
    return userRole === role;
  }

  /**
   * Verifica si el usuario es administrador
   */
  static isAdmin() {
    return this.hasRole(CONFIG.ROLES.ADMIN);
  }

  /**
   * Verifica si el usuario está autenticado
   */
  static isAuthenticated() {
    return StorageService.isAuthenticated();
  }
}
