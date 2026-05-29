/**
 * Utilities - Funciones auxiliares
 * Formateo de fechas, validaciones, etc.
 */

class Utils {
  /**
   * Formatea una fecha ISO a formato legible
   */
  static formatDate(isoString) {
    const date = new Date(isoString);
    const options = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    };
    return date.toLocaleDateString('es-SV', options);
  }

  /**
   * Formatea un número como moneda
   */
  static formatCurrency(amount) {
    return new Intl.NumberFormat('es-SV', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  /**
   * Valida un email
   */
  static isValidEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  }

  /**
   * Valida un username
   */
  static isValidUsername(username) {
    return username.length >= 3 && username.length <= 50;
  }

  /**
   * Valida una contraseña
   */
  static isValidPassword(password) {
    return password.length >= 6;
  }

  /**
   * Valida una edad
   */
  static isValidAge(age) {
    const ageNum = parseInt(age);
    return ageNum >= 1 && ageNum <= 150;
  }

  /**
   * Valida un número entero
   */
  static isValidInteger(value) {
    const num = parseInt(value);
    return !isNaN(num) && num > 0;
  }

  /**
   * Elimina espacios en blanco de inicio y fin
   */
  static trim(text) {
    return text ? text.trim() : '';
  }

  /**
   * Calcula días restantes hasta un evento
   */
  static daysUntilEvent(eventDate) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const event = new Date(eventDate);
    event.setHours(0, 0, 0, 0);

    const diffTime = event - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    return diffDays;
  }

  /**
   * Formatea estado de reserva
   */
  static formatBookingStatus(status) {
    const statusMap = {
      'CONFIRMED': 'Confirmada',
      'CANCELLED': 'Cancelada'
    };
    return statusMap[status] || status;
  }

  /**
   * Formatea rol de usuario
   */
  static formatRole(role) {
    const roleMap = {
      'ROLE_ADMIN': 'Administrador',
      'ROLE_USER': 'Usuario'
    };
    return roleMap[role] || role;
  }

  /**
   * Copia texto al portapapeles
   */
  static copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
      UIService.showSuccess('Copiado al portapapeles');
    }).catch(() => {
      UIService.showError('No se pudo copiar al portapapeles');
    });
  }

  /**
   * Genera un ID único
   */
  static generateId() {
    return '_' + Math.random().toString(36).substr(2, 9);
  }

  /**
   * Debounce para funciones
   */
  static debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }

  /**
   * Throttle para funciones
   */
  static throttle(func, limit) {
    let inThrottle;
    return function(...args) {
      if (!inThrottle) {
        func.apply(this, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    };
  }
}
