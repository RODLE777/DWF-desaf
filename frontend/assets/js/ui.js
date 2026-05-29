/**
 * UI Service - Gestión de la interfaz de usuario
 * Modales, alertas, loaders, etc.
 */

class UIService {
  /**
   * Muestra un mensaje de error
   */
  static showError(message) {
    this.showAlert('error', message);
  }

  /**
   * Muestra un mensaje de éxito
   */
  static showSuccess(message) {
    this.showAlert('success', message);
  }

  /**
   * Muestra un mensaje de información
   */
  static showInfo(message) {
    this.showAlert('info', message);
  }

  /**
   * Muestra un mensaje de advertencia
   */
  static showWarning(message) {
    this.showAlert('warning', message);
  }

  /**
   * Muestra una alerta genérica
   */
  static showAlert(type, message) {
    // Busca o crea el contenedor de alertas
    let container = document.getElementById('alerts-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'alerts-container';
      container.className = 'alerts-container';
      document.body.appendChild(container);
    }

    // Crea el elemento de alerta
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = `
      <div class="alert-content">
        <p>${message}</p>
        <button class="alert-close" onclick="this.parentElement.parentElement.remove()">×</button>
      </div>
    `;

    container.appendChild(alert);

    // Auto-remove después de 5 segundos
    setTimeout(() => {
      if (alert.parentElement) {
        alert.remove();
      }
    }, 5000);
  }

  /**
   * Muestra un modal de confirmación
   */
  static showConfirm(title, message, onConfirm, onCancel) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay active';
    modal.innerHTML = `
      <div class="modal-content">
        <div class="modal-header">
          <h3>${title}</h3>
          <button class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>${message}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary cancel-btn">Cancelar</button>
          <button class="btn btn-primary confirm-btn">Confirmar</button>
        </div>
      </div>
    `;

    document.body.appendChild(modal);

    const confirmBtn = modal.querySelector('.confirm-btn');
    const cancelBtn = modal.querySelector('.cancel-btn');
    const closeBtn = modal.querySelector('.modal-close');

    confirmBtn.addEventListener('click', () => {
      modal.remove();
      if (onConfirm) onConfirm();
    });

    cancelBtn.addEventListener('click', () => {
      modal.remove();
      if (onCancel) onCancel();
    });

    closeBtn.addEventListener('click', () => {
      modal.remove();
      if (onCancel) onCancel();
    });

    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        modal.remove();
        if (onCancel) onCancel();
      }
    });
  }

  /**
   * Muestra un loader/spinner
   */
  static showLoader(message = 'Cargando...') {
    let loader = document.getElementById('loader');
    if (!loader) {
      loader = document.createElement('div');
      loader.id = 'loader';
      loader.className = 'loader';
      document.body.appendChild(loader);
    }

    loader.innerHTML = `
      <div class="loader-content">
        <div class="spinner"></div>
        <p>${message}</p>
      </div>
    `;
    loader.classList.add('active');
  }

  /**
   * Oculta el loader
   */
  static hideLoader() {
    const loader = document.getElementById('loader');
    if (loader) {
      loader.classList.remove('active');
    }
  }

  /**
   * Actualiza la navegación según el rol del usuario
   */
  static updateNavigation() {
    const user = AuthService.getCurrentUser();
    const isAdmin = AuthService.isAdmin();

    // Oculta/muestra elementos según el rol
    const adminElements = document.querySelectorAll('[data-role="admin"]');
    const userElements = document.querySelectorAll('[data-role="user"]');

    adminElements.forEach(el => {
      el.style.display = isAdmin ? 'block' : 'none';
    });

    userElements.forEach(el => {
      el.style.display = !isAdmin ? 'block' : 'none';
    });

    // Actualiza el nombre de usuario en la navbar
    const userNameEl = document.getElementById('user-name');
    if (userNameEl && user) {
      userNameEl.textContent = user.username;
    }
  }

  /**
   * Muestra un skeleton loader
   */
  static showSkeletonLoader(containerId, count = 3) {
    const container = document.getElementById(containerId);
    if (!container) return;

    let skeleton = '';
    for (let i = 0; i < count; i++) {
      skeleton += `
        <div class="skeleton-card">
          <div class="skeleton-header"></div>
          <div class="skeleton-line"></div>
          <div class="skeleton-line"></div>
          <div class="skeleton-line short"></div>
        </div>
      `;
    }

    container.innerHTML = skeleton;
  }

  /**
   * Desactiva un botón y muestra loader
   */
  static disableButton(button, loading = true) {
    if (loading) {
      button.disabled = true;
      button.classList.add('loading');
      button.innerHTML = `<span class="spinner-sm"></span> ${button.textContent}`;
    } else {
      button.disabled = false;
      button.classList.remove('loading');
    }
  }

  /**
   * Habilita un botón
   */
  static enableButton(button, text = null) {
    button.disabled = false;
    button.classList.remove('loading');
    if (text) button.textContent = text;
  }

  /**
   * Formatea y muestra mensajes de error del backend
   */
  static showErrorResponse(error) {
    if (error.message) {
      this.showError(error.message);
    } else if (error.error && error.error.message) {
      this.showError(error.error.message);
    } else if (error.status === 401) {
      this.showError('No autenticado. Por favor inicia sesión.');
    } else if (error.status === 403) {
      this.showError('No tienes permisos para realizar esta acción.');
    } else if (error.status === 404) {
      this.showError('Recurso no encontrado.');
    } else if (error.status === 400) {
      this.showError('Datos inválidos. Por favor verifica los campos.');
    } else {
      this.showError('Ocurrió un error. Intenta nuevamente.');
    }
  }
}
