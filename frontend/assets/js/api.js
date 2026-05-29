// API Service - Handles all HTTP requests with JWT authentication
const APIService = {
  // Fetch with timeout
  async fetchWithTimeout(url, options = {}) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT);
    
    try {
      const response = await fetch(url, {
        ...options,
        signal: controller.signal
      });
      clearTimeout(timeoutId);
      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      throw error;
    }
  },
  
  // Get Authorization header
  getAuthHeader() {
    const token = StorageManager.getToken();
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  },
  
  // Make request with error handling
  async request(method, endpoint, body = null) {
    const url = API_CONFIG.BASE_URL + endpoint;
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeader()
      }
    };
    
    if (body) {
      options.body = JSON.stringify(body);
    }
    
    try {
      const response = await this.fetchWithTimeout(url, options);
      
      // Handle 401 - token expired or invalid
      if (response.status === 401) {
        StorageManager.clearAuth();
        UIManager.showToast('Sesión expirada. Por favor, inicia sesión nuevamente.', 'error');
        window.location.href = API_CONFIG.ROUTES.LOGIN;
        return null;
      }
      
      // Handle 403 - access denied
      if (response.status === 403) {
        UIManager.showToast('Acceso denegado. No tienes permisos para esta acción.', 'error');
        throw new Error('Access Denied');
      }
      
      // Parse response
      const data = await response.json().catch(() => null);
      
      // Handle 400 - validation errors
      if (response.status === 400) {
        const errorMsg = data?.message || 'Error de validación';
        throw new Error(errorMsg);
      }
      
      // Handle other errors
      if (!response.ok) {
        const errorMsg = data?.message || `Error: ${response.statusText}`;
        throw new Error(errorMsg);
      }
      
      return data;
    } catch (error) {
      console.error(`[API] Request failed: ${method} ${endpoint}`, error);
      if (error.name !== 'AbortError') {
        UIManager.showToast(error.message || 'Error en la solicitud', 'error');
      }
      throw error;
    }
  },
  
  // AUTH ENDPOINTS
  async register(username, firstname, lastname, age, password) {
    return this.request('POST', API_CONFIG.ENDPOINTS.REGISTER, {
      username,
      firstname,
      lastname,
      age: parseInt(age),
      password
    });
  },
  
  async login(username, password) {
    const response = await this.request('POST', API_CONFIG.ENDPOINTS.LOGIN, {
      username,
      password
    });
    
    if (response && response.token) {
      StorageManager.setToken(response.token);
      if (response.refreshToken) {
        StorageManager.setRefreshToken(response.refreshToken);
      }
    }
    
    return response;
  },
  
  // EVENT ENDPOINTS
  async getEvents() {
    return this.request('GET', API_CONFIG.ENDPOINTS.EVENTS);
  },
  
  async getEventDetail(id) {
    return this.request('GET', API_CONFIG.ENDPOINTS.EVENT_DETAIL(id));
  },
  
  async createEvent(eventData) {
    return this.request('POST', API_CONFIG.ENDPOINTS.EVENTS, eventData);
  },
  
  async updateEvent(id, eventData) {
    return this.request('PUT', API_CONFIG.ENDPOINTS.EVENT_DETAIL(id), eventData);
  },
  
  async deleteEvent(id) {
    return this.request('DELETE', API_CONFIG.ENDPOINTS.EVENT_DETAIL(id));
  },
  
  // BOOKING ENDPOINTS
  async createBooking(eventId, quantity) {
    return this.request('POST', API_CONFIG.ENDPOINTS.BOOKINGS, {
      eventId: parseInt(eventId),
      quantity: parseInt(quantity)
    });
  },
  
  async getBookings() {
    return this.request('GET', API_CONFIG.ENDPOINTS.MY_BOOKINGS);
  },
  
  async getMyBookings() {
    return this.request('GET', API_CONFIG.ENDPOINTS.MY_BOOKINGS);
  },
  
  async cancelBooking(id) {
    return this.request('DELETE', API_CONFIG.ENDPOINTS.CANCEL_BOOKING(id));
  }
};
