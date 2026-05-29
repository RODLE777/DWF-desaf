/**
 * Configuración centralizada de la aplicación
 * - URL base de la API
 * - Endpoints disponibles
 * - Constantes de la aplicación
 */

const CONFIG = {
  // URL Base - Cambiar según tu entorno
  API_BASE_URL: 'http://localhost:8080/api',
  
  // Endpoints de Autenticación
  AUTH: {
    REGISTER: '/auth/register',
    LOGIN: '/auth/login',
    REFRESH_TOKEN: '/auth/refresh-token'
  },
  
  // Endpoints de Eventos
  EVENTS: {
    LIST: '/events',
    GET_BY_ID: (id) => `/events/${id}`
  },
  
  // Endpoints de Reservas
  BOOKINGS: {
    CREATE: '/bookings',
    MY_BOOKINGS: '/bookings/my',
    CANCEL: (id) => `/bookings/${id}`
  },
  
  // Tiempos (en ms)
  TOKEN_EXPIRATION: 24 * 60 * 60 * 1000, // 24 horas
  REFRESH_TOKEN_EXPIRATION: 7 * 24 * 60 * 60 * 1000, // 7 días
  
  // Claves de localStorage
  STORAGE_KEYS: {
    ACCESS_TOKEN: 'access_token',
    REFRESH_TOKEN: 'refresh_token',
    USER: 'user',
    ROLE: 'user_role'
  },
  
  // Roles disponibles
  ROLES: {
    ADMIN: 'ROLE_ADMIN',
    USER: 'ROLE_USER'
  },
  
  // Estados de reserva
  BOOKING_STATUS: {
    CONFIRMED: 'CONFIRMED',
    CANCELLED: 'CANCELLED'
  },
  
  // Rutas de la aplicación
  ROUTES: {
    LOGIN: '/login.html',
    REGISTER: '/register.html',
    DASHBOARD: '/dashboard.html',
    EVENTS: '/events.html',
    EVENT_DETAIL: '/event-detail.html',
    BOOKINGS: '/bookings.html',
    ADMIN: '/admin.html',
    INDEX: '/index.html'
  }
};

// Validación de configuración
if (!CONFIG.API_BASE_URL) {
  console.warn('[CONFIG] API_BASE_URL no está configurada');
}
