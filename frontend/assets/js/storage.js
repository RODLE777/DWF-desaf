/**
 * Storage Service - Gestión centralizada de localStorage
 * Maneja tokens, usuario, y datos de sesión
 */

class StorageService {
  /**
   * Guarda el token de acceso
   */
  static setAccessToken(token) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN, token);
  }

  /**
   * Obtiene el token de acceso
   */
  static getAccessToken() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
  }

  /**
   * Guarda el refresh token
   */
  static setRefreshToken(token) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN, token);
  }

  /**
   * Obtiene el refresh token
   */
  static getRefreshToken() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
  }

  /**
   * Guarda los datos del usuario
   */
  static setUser(user) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.USER, JSON.stringify(user));
  }

  /**
   * Obtiene los datos del usuario
   */
  static getUser() {
    const user = localStorage.getItem(CONFIG.STORAGE_KEYS.USER);
    return user ? JSON.parse(user) : null;
  }

  /**
   * Guarda el rol del usuario
   */
  static setRole(role) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.ROLE, role);
  }

  /**
   * Obtiene el rol del usuario
   */
  static getRole() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.ROLE);
  }

  /**
   * Verifica si existe un usuario autenticado
   */
  static isAuthenticated() {
    return !!this.getAccessToken();
  }

  /**
   * Limpia toda la sesión
   */
  static clearSession() {
    localStorage.removeItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.USER);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.ROLE);
  }

  /**
   * Guarda datos temporales
   */
  static setTemp(key, value) {
    const temp = JSON.parse(localStorage.getItem('_temp') || '{}');
    temp[key] = value;
    localStorage.setItem('_temp', JSON.stringify(temp));
  }

  /**
   * Obtiene datos temporales
   */
  static getTemp(key) {
    const temp = JSON.parse(localStorage.getItem('_temp') || '{}');
    return temp[key];
  }

  /**
   * Limpia datos temporales
   */
  static clearTemp(key) {
    const temp = JSON.parse(localStorage.getItem('_temp') || '{}');
    delete temp[key];
    localStorage.setItem('_temp', JSON.stringify(temp));
  }
}
