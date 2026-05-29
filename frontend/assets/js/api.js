/**
 * API Service - Capa centralizada para consumir la API REST
 * Maneja:
 * - Requests HTTP (GET, POST, PUT, DELETE)
 * - JWT automático en headers
 * - Refresh token
 * - Manejo de errores global
 * - Expiración de token
 */

class APIService {
  constructor() {
    this.baseURL = CONFIG.API_BASE_URL;
    this.isRefreshing = false;
    this.failedQueue = [];
  }

  /**
   * Obtiene el token de acceso desde localStorage
   */
  getAccessToken() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
  }

  /**
   * Obtiene el refresh token desde localStorage
   */
  getRefreshToken() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
  }

  /**
   * Construye los headers necesarios para la petición
   * Incluye JWT automáticamente si existe token
   */
  getHeaders(includeAuth = true) {
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    if (includeAuth) {
      const token = this.getAccessToken();
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }

    return headers;
  }

  /**
   * Procesa una petición HTTP
   * Maneja errores 401, refresh token, y errores globales
   */
  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      method: options.method || 'GET',
      headers: this.getHeaders(options.includeAuth !== false),
      ...options
    };

    if (config.body && typeof config.body === 'object') {
      config.body = JSON.stringify(config.body);
    }

    try {
      const response = await fetch(url, config);

      // Token expirado - Intenta refresh
      if (response.status === 401 && options.includeAuth !== false) {
        return await this.handleTokenExpiration(endpoint, options);
      }

      // Otros errores
      if (!response.ok) {
        await this.handleError(response);
      }

      // Success (204 No Content)
      if (response.status === 204) {
        return { success: true };
      }

      return await response.json();
    } catch (error) {
      console.error('[API] Error en petición:', error);
      throw {
        status: 0,
        message: 'Error de conexión con el servidor',
        error: error.message
      };
    }
  }

  /**
   * Intenta renovar el token cuando expira
   */
  async handleTokenExpiration(endpoint, options) {
    if (this.isRefreshing) {
      return new Promise((resolve) => {
        this.failedQueue.push(() => {
          resolve(this.request(endpoint, options));
        });
      });
    }

    this.isRefreshing = true;

    try {
      const refreshToken = this.getRefreshToken();
      if (!refreshToken) {
        throw new Error('No hay refresh token disponible');
      }

      const response = await fetch(`${this.baseURL}${CONFIG.AUTH.REFRESH_TOKEN}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${refreshToken}`
        }
      });

      if (!response.ok) {
        throw new Error('Refresh token inválido o expirado');
      }

      const data = await response.json();
      
      // Guarda los nuevos tokens
      localStorage.setItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN, data.access_token);
      localStorage.setItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN, data.refresh_token);

      this.isRefreshing = false;

      // Procesa la cola de peticiones fallidas
      this.failedQueue.forEach(callback => callback());
      this.failedQueue = [];

      // Reintenta la petición original
      return await this.request(endpoint, { ...options, retry: true });
    } catch (error) {
      console.error('[API] Error al refrescar token:', error);
      this.isRefreshing = false;
      this.failedQueue = [];
      
      // Token inválido - logout automático
      AuthService.logout();
      throw {
        status: 401,
        message: 'Sesión expirada. Por favor, inicia sesión nuevamente.',
        error: error.message
      };
    }
  }

  /**
   * Maneja errores de respuesta
   */
  async handleError(response) {
    const contentType = response.headers.get('content-type');
    let errorData = {
      status: response.status,
      message: `Error ${response.status}`,
      error: null
    };

    if (contentType && contentType.includes('application/json')) {
      try {
        const json = await response.json();
        errorData.message = json.message || json.error || errorData.message;
        errorData.error = json;
      } catch (e) {
        // No se puede parsear como JSON
      }
    }

    throw errorData;
  }

  /**
   * GET - Obtener datos
   */
  async get(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'GET' });
  }

  /**
   * POST - Crear datos
   */
  async post(endpoint, body, options = {}) {
    return this.request(endpoint, { ...options, method: 'POST', body });
  }

  /**
   * PUT - Actualizar datos
   */
  async put(endpoint, body, options = {}) {
    return this.request(endpoint, { ...options, method: 'PUT', body });
  }

  /**
   * DELETE - Eliminar datos
   */
  async delete(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'DELETE' });
  }
}

// Instancia global del servicio de API
const api = new APIService();
