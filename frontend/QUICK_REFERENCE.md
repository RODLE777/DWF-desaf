# Frontend Quick Reference Guide

## Project Structure Overview

```
frontend/
├── 📄 HTML Pages (7 main pages + 1 legacy)
├── 📁 assets/
│   ├── css/
│   │   └── styles.css (← Single master stylesheet - 754 lines)
│   └── js/
│       ├── config.js (API configuration)
│       ├── api.js (HTTP requests)
│       ├── storage.js (localStorage management)
│       ├── guards.js (Route protection)
│       ├── utils.js (Validation functions)
│       ├── ui.js (UI interactions)
│       └── jwt-utils.js (JWT parsing)
```

## Key Utilities Quick Reference

### UIManager (ui.js)
```javascript
UIManager.showLoader('Loading...')      // Show spinner
UIManager.hideLoader()                  // Hide spinner
UIManager.showToast(msg, 'success')    // Show notification (success/error/info/warning)
UIManager.showConfirm(title, msg, onConfirm, onCancel)  // Confirmation modal
UIManager.formatDate(date)              // Format to Spanish date
UIManager.formatDateForInput(date)      // Format for datetime-local input
UIManager.formatTime(date)              // Format to HH:MM
UIManager.formatCurrency(amount)        // Format as USD ($)
UIManager.getRemainingCapacity(cap, booked)  // Get available spots
```

### APIService (api.js)
```javascript
// Auth
await APIService.register(user, first, last, age, pass)
await APIService.login(user, pass)

// Events
await APIService.getEvents()            // Get all events
await APIService.getEventDetail(id)     // Get single event
await APIService.createEvent(data)      // Create new event
await APIService.updateEvent(id, data)  // Update event
await APIService.deleteEvent(id)        // Delete event

// Bookings
await APIService.createBooking(eventId, qty)  // Create booking
await APIService.getBookings()          // Get user bookings
await APIService.cancelBooking(id)      // Cancel booking
```

### StorageManager (storage.js)
```javascript
StorageManager.getToken()               // Get JWT token
StorageManager.setToken(token)          // Store JWT token
StorageManager.clearAuth()              // Clear all auth data
StorageManager.isAuthenticated()        // Check if logged in
StorageManager.isAdmin()                // Check if admin
StorageManager.getUserData()            // Get stored user info
```

### RouteGuard (guards.js)
```javascript
if (!RouteGuard.requireAuth()) return;  // Redirect if not logged in
if (!RouteGuard.requireAdmin()) return; // Redirect if not admin
```

## Common Patterns

### Handle Loading & Toast
```javascript
UIManager.showLoader('Processing...');
try {
  const result = await APIService.getEvents();
  renderEvents(result);
} catch (error) {
  console.error(error);
} finally {
  UIManager.hideLoader();
}
```

### Show Confirmation Dialog
```javascript
const confirmed = await new Promise(resolve => {
  UIManager.showConfirm(
    'Delete Event',
    'Are you sure?',
    () => resolve(true),
    () => resolve(false)
  );
});

if (confirmed) {
  // Delete the item
}
```

### Validate & Show Errors
```javascript
let valid = true;
document.getElementById('error').textContent = '';

if (!Utils.isValidUsername(username)) {
  document.getElementById('error').textContent = 'Username: 3-50 chars';
  valid = false;
}

if (!valid) return;
// Submit form
```

### Format Values for Display
```javascript
const date = UIManager.formatDate('2025-05-29');        // "29 de mayo de 2025"
const time = UIManager.formatTime('2025-05-29T14:30');  // "14:30"
const price = UIManager.formatCurrency(99.99);          // "$99.99"
const spots = UIManager.getRemainingCapacity(100, 45);  // 55
```

## CSS Classes Reference

### Buttons
```html
<button class="btn btn-primary">Primary</button>
<button class="btn btn-secondary">Secondary</button>
<button class="btn btn-danger">Danger</button>
<button class="btn btn-success">Success</button>

<button class="btn btn-sm">Small</button>
<button class="btn btn-lg">Large</button>
<button class="btn btn-block">Full Width</button>
<button class="btn" disabled>Disabled</button>
```

### Forms
```html
<div class="form-group">
  <label for="input">Label</label>
  <input type="text" id="input" required>
  <div class="form-error" id="error"></div>
</div>

<textarea placeholder="Message" required></textarea>
<select required><option>Choose...</option></select>
```

### Cards
```html
<div class="card">
  <div class="card-header"><h3>Title</h3></div>
  <div class="card-body">Content here</div>
  <div class="card-footer">Actions</div>
</div>
```

### Layout
```html
<div class="flex">Item 1</div>               <!-- Flex layout with gap -->
<div class="flex-between">Left</div>        <!-- Space between -->
<div class="flex-center">Centered</div>     <!-- Center all -->
<div class="grid">Item 1</div>              <!-- Responsive grid -->
<div class="container">Max width 1200px</div>
```

### Status Badges
```html
<span class="badge badge-success">Success</span>
<span class="badge badge-danger">Error</span>
<span class="badge badge-warning">Warning</span>
<span class="badge badge-info">Info</span>
```

### Utilities
```html
<p class="text-center">Centered text</p>
<p class="text-muted">Secondary text color</p>
<p class="text-right">Right aligned</p>

<div class="mt-3">Margin top (24px)</div>
<div class="mb-2">Margin bottom (16px)</div>
<div class="hidden">Hidden</div>
```

## API Response Format

### Login/Register Success
```javascript
{
  token: "eyJ0eXAi...",
  refreshToken: "optional",
  username: "user",
  role: "admin|user"
}
```

### Get Events
```javascript
[
  {
    id: 1,
    name: "Event Name",
    description: "Description",
    date: "2025-05-29T14:00",
    capacity: 100,
    bookedCount: 45,
    price: 29.99
  }
]
```

### Get Bookings
```javascript
[
  {
    id: 1,
    eventName: "Event",
    eventDate: "2025-05-29T14:00",
    quantity: 2,
    totalPrice: 59.98,
    status: "active|completed|cancelled"
  }
]
```

## Color Variables

```css
--primary-color: #4361ee    /* Blue */
--secondary-color: #6c757d  /* Gray */
--success-color: #198754    /* Green */
--danger-color: #dc3545     /* Red */
--warning-color: #ffc107    /* Yellow */
--info-color: #0dcaf0       /* Cyan */

--bg-primary: #f8f9fa       /* Light background */
--bg-secondary: #ffffff     /* White */
--bg-tertiary: #e9ecef      /* Light gray */

--text-primary: #212529     /* Dark text */
--text-secondary: #6c757d   /* Gray text */
--text-light: #adb5bd       /* Light gray text */
```

## Spacing Scale

```
xs: 4px
sm: 8px
md: 16px
lg: 24px
xl: 32px
2xl: 48px
```

Use in CSS as `var(--spacing-md)` or in Tailwind: `p-4`, `m-2`, `gap-6`, etc.

## Common Tasks

### Add New Page
1. Create `newpage.html`
2. Include script imports:
   ```html
   <script src="assets/js/config.js"></script>
   <script src="assets/js/jwt-utils.js"></script>
   <script src="assets/js/storage.js"></script>
   <script src="assets/js/guards.js"></script>
   <script src="assets/js/utils.js"></script>
   <script src="assets/js/ui.js"></script>
   <script src="assets/js/api.js"></script>
   ```
3. Add auth check: `if (!RouteGuard.requireAuth()) return;`
4. Link from navbar in index/other pages

### Protect Page (Auth Required)
```javascript
// At top of page script
if (!RouteGuard.requireAuth()) return;
```

### Protect Page (Admin Only)
```javascript
// At top of page script
if (!RouteGuard.requireAdmin()) return;
```

### Call API & Handle Errors
```javascript
try {
  UIManager.showLoader('Loading...');
  const data = await APIService.getEvents();
  // Use data
} catch (error) {
  console.error(error);
  // Error toast is shown automatically
} finally {
  UIManager.hideLoader();
}
```

### Show Admin Element Only
```html
<button data-admin-only style="display: none;">Admin Action</button>

<script>
if (StorageManager.isAdmin()) {
  document.querySelector('[data-admin-only]').style.display = 'inline-block';
}
</script>
```

## Debugging Tips

### Check Authentication Status
```javascript
console.log('Token:', StorageManager.getToken());
console.log('Is Auth:', StorageManager.isAuthenticated());
console.log('Is Admin:', StorageManager.isAdmin());
console.log('User:', StorageManager.getUserData());
```

### Check API Response
```javascript
const response = await APIService.getEvents();
console.log('Response:', response);
```

### Enable Debug Mode
Add to any script:
```javascript
const DEBUG = true;
if (DEBUG) console.log('[v0] Starting app...');
```

## Browser DevTools Console

```javascript
// Check stored token
JSON.parse(localStorage.getItem('authToken'))

// Check auth data
JSON.parse(localStorage.getItem('authData'))

// Clear all storage
localStorage.clear()

// Make test API call
fetch('http://localhost:3000/api/events', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
  }
}).then(r => r.json()).then(console.log)
```

## File Size Summary

- `styles.css`: 754 lines (~25KB)
- All HTML files: ~45KB combined
- All JS files: ~30KB combined
- **Total Frontend**: ~100KB

Lightweight and fast-loading! ⚡
