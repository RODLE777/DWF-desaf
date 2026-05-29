# EventBooking Frontend

Frontend profesional vanilla JavaScript para el Sistema de Reservas de Eventos (Event Booking System) basado en Spring Boot.

## 🎯 Características

- ✅ **Arquitectura Limpia**: Separación de responsabilidades con módulos especializados
- ✅ **Autenticación JWT**: Login, registro, refresh token automático
- ✅ **Control de Acceso**: Guards de autenticación y roles (ADMIN, USER)
- ✅ **Gestión de Eventos**: Listado, búsqueda, detalles, filtrado
- ✅ **Sistema de Reservas**: Crear, ver, cancelar reservas con validaciones
- ✅ **Panel Administrativo**: Gestión de eventos y estadísticas
- ✅ **Interfaz Responsiva**: Diseño mobile-first con Flexbox y CSS Grid
- ✅ **UX Moderna**: Loaders, modales, alertas, feedback visual
- ✅ **Validaciones**: Frontend + manejo de errores del backend

## 📁 Estructura del Proyecto

```
frontend/
├── index.html                  # Página inicial
├── login.html                  # Login
├── register.html               # Registro
├── dashboard.html              # Dashboard principal
├── events.html                 # Listado de eventos
├── event-detail.html           # Detalle y reserva de evento
├── bookings.html               # Mis reservas
├── admin.html                  # Panel administrativo
│
└── assets/
    ├── css/
    │   ├── variables.css       # Tokens de diseño (colores, tipografía)
    │   ├── global.css          # Estilos globales
    │   ├── navbar.css          # Estilos navbar
    │   ├── auth.css            # Estilos autenticación
    │   ├── dashboard.css       # Estilos dashboard
    │   ├── events.css          # Estilos eventos
    │   ├── bookings.css        # Estilos reservas
    │   └── admin.css           # Estilos admin
    │
    ├── js/
    │   ├── config.js           # Configuración central
    │   ├── api.js              # Servicio de API REST
    │   ├── storage.js          # Gestión de localStorage
    │   ├── auth.js             # Servicio de autenticación
    │   ├── guards.js           # Guards de protección de rutas
    │   ├── utils.js            # Utilidades
    │   └── ui.js               # Gestión de UI
    │
    ├── img/                    # Imágenes
    └── icons/                  # Iconos SVG
```

## 🚀 Guía de Inicio

### Prerequisitos

- Node.js (para servir archivos estáticos, opcional)
- Backend Spring Boot ejecutándose en `http://localhost:8080`
- Navegador moderno (Chrome, Firefox, Safari, Edge)

### Instalación

1. **Clonar o descargar el proyecto**

```bash
git clone <repository-url>
cd frontend
```

2. **Configurar la URL de la API**

Editar `assets/js/config.js` y actualizar `API_BASE_URL`:

```javascript
const CONFIG = {
  API_BASE_URL: 'http://localhost:8080/api', // Cambiar según tu entorno
  // ...
};
```

3. **Servir la aplicación**

Opción A - Usar Live Server (recomendado):
```bash
# Instalar extensión Live Server en VS Code o usar:
npx http-server
```

Opción B - Usar Python:
```bash
python -m http.server 8000
# Acceder a http://localhost:8000
```

Opción C - Vercel:
```bash
vercel deploy
```

## 🔐 Autenticación

### Flujo de Login

1. Usuario ingresa credenciales en `/login.html`
2. Se envía POST a `/api/auth/login`
3. Backend retorna:
   - `access_token` (JWT válido por 24 horas)
   - `refresh_token` (válido por 7 días)
   - `username` y `role`
4. Tokens se guardan en `localStorage`
5. Se redirige a `/dashboard.html`

### Flujo de Refresh Token

Si el `access_token` expira (401):
1. Se intercepta la respuesta 401
2. Se envía refresh token al endpoint `/api/auth/refresh-token`
3. Se obtienen nuevos tokens
4. Se reintenta la petición original
5. Si el refresh token también expira → logout automático

### Protección de Rutas

```javascript
// Guards automáticos ejecutados al cargar
Guards.requireAuth()      // Redirige a login si no está autenticado
Guards.requireAdmin()     // Solo admins
Guards.requireUser()      // Solo usuarios normales
Guards.requireGuest()     // Solo no autenticados (login, register)
```

## 📡 API REST

### Endpoints Implementados

```javascript
// Autenticación (sin JWT requerido)
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh-token

// Eventos (requiere JWT)
GET    /api/events                    // Listar con paginación
GET    /api/events/{id}               // Detalle
POST   /api/events                    // Crear (ROLE_ADMIN)
PUT    /api/events/{id}               // Actualizar (ROLE_ADMIN)
DELETE /api/events/{id}               // Eliminar (ROLE_ADMIN)

// Reservas (requiere JWT)
POST   /api/bookings                  // Crear reserva
GET    /api/bookings/my               // Mis reservas
DELETE /api/bookings/{id}             // Cancelar reserva
```

### Headers Automáticos

Todas las peticiones incluyen automáticamente:

```javascript
Authorization: Bearer <access_token>
Content-Type: application/json
```

## 🎨 Diseño

### Paleta de Colores

- **Primario**: Azul (#3B82F6) - Acciones principales
- **Éxito**: Verde (#10B981) - Confirmaciones
- **Error**: Rojo (#EF4444) - Errores
- **Neutrales**: Grises para backgrounds y bordes
- **Fondo**: Blanco (#FFFFFF)

### Tipografía

- **Font Family**: System fonts (Segoe UI, Roboto, etc.)
- **Body**: 1rem, weight 400
- **Headings**: Weights 600-700

### Componentes

- **Botones**: Primary, Secondary, Danger, Success
- **Tarjetas**: Event cards con hover effect
- **Modales**: Confirmaciones elegantes
- **Alertas**: Toast notifications (5s auto-dismiss)
- **Loaders**: Spinners y skeleton loaders
- **Tablas**: Responsivas con acciones

## 🔄 Flujos Principales

### 1. Registro

```
register.html → AuthService.register() → POST /api/auth/register
→ Guardar tokens → localStorage → Redirigir dashboard
```

### 2. Ver Eventos

```
events.html → api.get(/api/events) → Renderizar grid de tarjetas
→ Click en evento → event-detail.html?id=X
```

### 3. Crear Reserva

```
event-detail.html → Seleccionar cantidad → POST /api/bookings
→ Validar cupos → Confirmar → bookings.html
```

### 4. Cancelar Reserva

```
bookings.html → ShowConfirm modal → DELETE /api/bookings/{id}
→ Actualizar lista de reservas
```

### 5. Panel Admin

```
admin.html (Guard: ROLE_ADMIN) → Ver estadísticas
→ Gestionar eventos → DELETE /api/events/{id}
```

## 🛠 Servicios y Módulos

### api.js

Maneja todas las peticiones HTTP:

```javascript
api.get(endpoint)                    // GET
api.post(endpoint, body)             // POST
api.put(endpoint, body)              // PUT
api.delete(endpoint)                 // DELETE
```

Características:
- JWT automático en headers
- Manejo de errores global
- Refresh token automático
- Retry de peticiones fallidas

### auth.js

Gestión de autenticación:

```javascript
AuthService.register(userData)       // Registro
AuthService.login(credentials)       // Login
AuthService.logout()                 // Logout
AuthService.isAuthenticated()        // ¿Autenticado?
AuthService.hasRole(role)            // ¿Tiene rol?
AuthService.isAdmin()                // ¿Es admin?
```

### storage.js

Gestión de localStorage:

```javascript
StorageService.setAccessToken(token)
StorageService.getAccessToken()
StorageService.setRefreshToken(token)
StorageService.getRefreshToken()
StorageService.setUser(user)
StorageService.getUser()
StorageService.setRole(role)
StorageService.clearSession()        // Logout limpio
```

### ui.js

Gestión de la interfaz:

```javascript
UIService.showSuccess(message)       // Alerta verde
UIService.showError(message)         // Alerta roja
UIService.showConfirm(title, msg, onConfirm)  // Modal confirmación
UIService.showLoader(message)        // Spinner
UIService.hideLoader()
UIService.updateNavigation()         // Actualizar según rol
UIService.disableButton(button)      // Deshabilitar botón
UIService.enableButton(button)
```

### utils.js

Utilidades:

```javascript
Utils.formatDate(isoString)          // Formatea fechas
Utils.formatCurrency(amount)         // Formatea dinero
Utils.isValidEmail(email)
Utils.isValidUsername(username)
Utils.isValidPassword(password)
Utils.formatBookingStatus(status)
Utils.formatRole(role)
Utils.debounce(func, wait)
Utils.throttle(func, limit)
```

## 🔒 Seguridad

### CORS

Frontend y backend deben estar en:
- **Desarrollo**: `http://localhost:3000` y `http://localhost:8080`
- **Producción**: Dominios configurados en backend

Backend ya tiene `@CrossOrigin(origins = "*")` configurado.

### Token Storage

Los tokens se guardan en `localStorage` (vulnerable a XSS, pero necesario para Single Page App sin backend session):

Para mayor seguridad en producción:
- Usar `httpOnly` cookies en el servidor
- Implementar CSRF tokens
- Usar HTTPS sempre

### Validaciones

- **Frontend**: Validar formato, campos vacíos, rangos
- **Backend**: Validar datos, permisos, JWT, roles
- **Headers**: JWT automático en Authorization

## 📱 Responsividad

Breakpoints CSS:

```css
Mobile:  < 480px
Tablet:  480px - 768px
Desktop: > 768px
```

Todas las páginas adaptan:
- Grid a una columna en mobile
- Navbar a vertical en mobile
- Tablas scrollables en mobile
- Modales full-width en mobile

## 🐛 Manejo de Errores

### Errores Comunes

```javascript
// 401 - No autenticado
→ UIService.showError('No autenticado')
→ Logout automático

// 403 - Sin permisos
→ UIService.showError('No tienes permisos')
→ Redirigir dashboard

// 404 - No encontrado
→ UIService.showError('Recurso no encontrado')

// 400 - Datos inválidos
→ UIService.showError('Datos inválidos')

// 0 - Error de conexión
→ UIService.showError('Error de conexión')
```

## 📝 Convenciones de Código

### Nomenclatura

- **Clases**: PascalCase (`AuthService`, `UIService`)
- **Métodos**: camelCase (`getAccessToken()`)
- **Constantes**: UPPER_SNAKE_CASE (`CONFIG`, `STORAGE_KEYS`)
- **Variables**: camelCase (`userName`, `eventId`)
- **IDs HTML**: kebab-case (`user-avatar`, `events-grid`)

### Comentarios

```javascript
/**
 * Descripción de la función
 * @param {type} name - Descripción del parámetro
 * @returns {type} Descripción del retorno
 */
function myFunction(name) {
  // Comentario de lógica importante
}
```

## 🚀 Deploy

### Vercel

```bash
vercel deploy --prod
```

### GitHub Pages

```bash
# Push a rama gh-pages
git subtree push --prefix frontend origin gh-pages
```

### Otros (Netlify, Firebase, etc.)

Solo archivos HTML, CSS, JS estáticos - compatible con cualquier hosting.

## 🔧 Troubleshooting

### "API_BASE_URL no está configurada"

Revisar `assets/js/config.js` → API_BASE_URL debe ser válida

### CORS Error

Asegurar que backend tiene:
```java
@CrossOrigin(origins = "*")  // O especificar dominio
```

### Token expirado

El frontend maneja automáticamente refresh token. Si sigue fallando:
1. Limpiar localStorage
2. Hacer logout manual
3. Intentar login nuevamente

### Rutas no funcionan

Las rutas son archivos HTML. Asegurar que:
1. El servidor sirve archivos estáticos
2. Las URLs son correctas (`/login.html`, not `/login`)

## 📚 Documentación Adicional

- [Spring Boot Backend](../README.md)
- [API Swagger](http://localhost:8080/swagger-ui)
- [OWASP - Web Security](https://owasp.org/)
- [MDN - Web APIs](https://developer.mozilla.org/)

## 👤 Autor

Desarrollado como frontend profesional para Event Booking System.

## 📄 Licencia

MIT License

---

**Última actualización**: 29/05/2024
**Versión**: 1.0.0
