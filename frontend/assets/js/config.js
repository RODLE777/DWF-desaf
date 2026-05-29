// Configuration for Event Booking Frontend
const API_CONFIG = {
  // Change this to your backend URL
  BASE_URL: 'http://localhost:8080',
  
  // API endpoints
  ENDPOINTS: {
    // Auth
    REGISTER: '/api/auth/register',
    LOGIN: '/api/auth/login',
    REFRESH_TOKEN: '/api/auth/refresh-token',
    
    // Events (protected by JWT)
    EVENTS: '/api/events',
    EVENT_DETAIL: (id) => `/api/events/${id}`,
    
    // Bookings (protected by JWT)
    BOOKINGS: '/api/bookings',
    MY_BOOKINGS: '/api/bookings/my',
    CANCEL_BOOKING: (id) => `/api/bookings/${id}`,
    
    // Users
    USERS: '/api/users'
  },
  
  // Local storage keys
  STORAGE_KEYS: {
    TOKEN: 'event_booking_token',
    REFRESH_TOKEN: 'event_booking_refresh_token',
    USER: 'event_booking_user'
  },
  
  // API timeout in ms
  TIMEOUT: 10000,
  
  // Booking statuses
  BOOKING_STATUS: {
    CONFIRMED: 'CONFIRMED',
    CANCELLED: 'CANCELLED'
  },
  
  // Routes
  ROUTES: {
    LOGIN: '/login.html',
    REGISTER: '/register.html',
    EVENTS: '/events.html',
    EVENT_DETAIL: '/event-detail.html',
    BOOKINGS: '/bookings.html',
    ADMIN: '/admin.html',
    INDEX: '/'
  }
};
