// UI Manager - Handles all UI interactions, notifications, and loaders
const UIManager = {
  // Show loading spinner
  showLoader(message = 'Cargando...') {
    const loader = document.getElementById('loader');
    if (loader) {
      loader.style.display = 'flex';
      const text = loader.querySelector('.loader-text');
      if (text) text.textContent = message;
    }
  },
  
  // Hide loading spinner
  hideLoader() {
    const loader = document.getElementById('loader');
    if (loader) {
      loader.style.display = 'none';
    }
  },
  
  // Show toast notification
  showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toast-container');
    if (!toastContainer) return;
    
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <div class="toast-content">
        <span class="toast-message">${message}</span>
        <button class="toast-close" onclick="this.parentElement.parentElement.remove()">✕</button>
      </div>
    `;
    
    toastContainer.appendChild(toast);
    
    // Auto remove after 4 seconds
    setTimeout(() => {
      if (toast.parentElement) toast.remove();
    }, 4000);
  },
  
  // Show confirmation modal
  showConfirm(title, message, onConfirm, onCancel) {
    const modal = document.getElementById('confirm-modal');
    if (!modal) return false;
    
    const titleEl = modal.querySelector('.modal-title');
    const messageEl = modal.querySelector('.modal-body p');
    const confirmBtn = modal.querySelector('.modal-confirm');
    const cancelBtn = modal.querySelector('.modal-cancel');
    
    if (titleEl) titleEl.textContent = title;
    if (messageEl) messageEl.textContent = message;
    
    // Remove old listeners
    const newConfirmBtn = confirmBtn.cloneNode(true);
    const newCancelBtn = cancelBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);
    cancelBtn.parentNode.replaceChild(newCancelBtn, cancelBtn);
    
    newConfirmBtn.addEventListener('click', () => {
      this.closeModal('confirm-modal');
      if (onConfirm) onConfirm();
    });
    
    newCancelBtn.addEventListener('click', () => {
      this.closeModal('confirm-modal');
      if (onCancel) onCancel();
    });
    
    this.openModal('confirm-modal');
    return true;
  },
  
  // Open modal
  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.style.display = 'flex';
      document.body.style.overflow = 'hidden';
    }
  },
  
  // Close modal
  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.style.display = 'none';
      document.body.style.overflow = 'auto';
    }
  },
  
  // Format date for display
  formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  },
  
  // Format date for datetime-local input
  formatDateForInput(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  },
  
  // Format time
  formatTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    });
  },
  
  // Format currency
  formatCurrency(amount) {
    return new Intl.NumberFormat('es-SV', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  },
  
  // Get remaining capacity
  getRemainingCapacity(totalCapacity, bookedCount) {
    return Math.max(0, totalCapacity - bookedCount);
  }
};
