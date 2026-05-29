# 🚀 Guía Rápida de Inicio - Frontend EventBooking

## ⚡ 5 Minutos para Empezar

### 1. Verificar Backend Ejecutándose

```bash
# El backend debe estar en http://localhost:8080
# Verificar: http://localhost:8080/swagger-ui
```

### 2. Configurar API Base URL (Importante)

Editar: `frontend/assets/js/config.js`

```javascript
const CONFIG = {
  // CAMBIAR ESTO según tu entorno
  API_BASE_URL: 'http://localhost:8080/api',  // ← Aquí
  // ...
};
```

**Ejemplos según entorno:**
- Desarrollo local: `http://localhost:8080/api`
- Producción (servidor): `https://api.example.com/api`
- Vercel: `https://tu-backend.vercel.app/api`

### 3. Servir el Frontend

**Opción A: Live Server (Recomendado - VS Code)**

```bash
# Instalar extensión "Live Server"
# Click derecho en index.html → "Open with Live Server"
# Automáticamente abre en http://127.0.0.1:5500
```

**Opción B: HTTP Server**

```bash
# Desde la carpeta frontend/
cd frontend
npx http-server
# Acceder a http://localhost:8000
```

**Opción C: Python**

```bash
cd frontend
python -m http.server 8000
# Acceder a http://localhost:8000
```

### 4. Probar la Aplicación

1. **Abrir en navegador**: http://localhost:8000 (o el puerto que uses)
2. **Crear cuenta**: 
   - Click "Crear Cuenta"
   - Completa el formulario
   - Se guarda automáticamente
3. **Ver eventos**: 
   - Automáticamente redirige al dashboard
   - Click en "Eventos"
4. **Reservar**:
   - Click en evento
   - Selecciona cantidad
   - Click "Confirmar Reserva"
5. **Ver reservas**:
   - Click en "Mis Reservas"

## 🔑 Credenciales de Prueba

Si tu backend ya tiene datos, usa:

```
Usuario: admin
Contraseña: admin123
```

O crea una cuenta nueva durante el registro.

## 📱 Estructura de Carpetas

```
frontend/
├── index.html                 # Página de inicio
├── login.html                 # Login
├── register.html              # Registro
├── dashboard.html             # Dashboard
├── events.html                # Lista de eventos
├── event-detail.html          # Detalle y reserva
├── bookings.html              # Mis reservas
├── admin.html                 # Panel admin
│
└── assets/
    ├── css/                   # Estilos
    │   ├── variables.css      # Colores, tipografía
    │   ├── global.css         # Estilos generales
    │   ├── navbar.css         # Navegación
    │   ├── dashboard.css      # Dashboard
    │   ├── events.css         # Eventos
    │   ├── bookings.css       # Reservas
    │   ├── admin.css          # Admin
    │   └── responsive.css     # Mobile
    │
    └── js/                    # JavaScript
        ├── config.js          # Configuración (⚠️ Modificar URL aquí)
        ├── api.js             # Peticiones HTTP
        ├── auth.js            # Autenticación
        ├── storage.js         # localStorage
        ├── guards.js          # Protección de rutas
        ├── ui.js              # UI (alertas, modales, etc)
        └── utils.js           # Utilidades
```

## 🎯 Flujo de Prueba Recomendado

### Caso 1: Usuario Normal

1. **Registro**
   - Ir a `/register.html`
   - Crear cuenta con datos válidos
   - Se redirige a dashboard

2. **Explorar Eventos**
   - Click en "Eventos"
   - Ver listado de eventos
   - Click en evento para ver detalles

3. **Crear Reserva**
   - En detalle del evento, selecciona cantidad
   - Click "Confirmar Reserva"
   - Ver confirmación

4. **Ver Reservas**
   - Click en "Mis Reservas"
   - Ver reservas creadas
   - Opción de cancelar

5. **Logout**
   - Click en "Salir"
   - Se redirige a login

### Caso 2: Administrador

1. **Login como Admin**
   - Usuario: `admin` (u otro admin del sistema)
   - Contraseña: su contraseña

2. **Acceder Panel Admin**
   - Aparece opción "Administración" en navbar
   - Click para ver panel

3. **Funciones Admin**
   - Ver estadísticas
   - Gestionar eventos (ver, editar, eliminar)
   - Ver todas las reservas del sistema

## 🔧 Troubleshooting Rápido

### Error: "API_BASE_URL is not configured"

```
✓ Solución: Edita config.js y actualiza API_BASE_URL
```

### Error: "CORS error"

```
✓ Solución: Asegura que el backend tiene @CrossOrigin(origins = "*")
```

### No puedo loguearme

```
✓ Solución 1: Crea una cuenta nueva (register)
✓ Solución 2: Revisa que el backend está ejecutándose
✓ Solución 3: Limpia localStorage (F12 → Application → Storage)
```

### Página en blanco

```
✓ Solución 1: Abre DevTools (F12) y revisa errors
✓ Solución 2: Asegura que estás accediendo a /login.html, no /login
✓ Solución 3: Recarga la página (Ctrl+Shift+R para limpiar caché)
```

### Botones no funcionan

```
✓ Solución: Revisa en DevTools → Console si hay errores JavaScript
✓ Es probable que la API_BASE_URL esté mal configurada
```

## 📦 Desplegando a Producción

### Vercel (Recomendado)

```bash
# Desde la raíz del proyecto
vercel deploy --prod

# Responder preguntas:
# - Which scope? Tu scope
# - Linked to project? No (o Yes si ya existe)
# - Project name? eventbooking-frontend
# - Directory? ./frontend
```

### GitHub Pages

```bash
git subtree push --prefix frontend origin gh-pages
```

### Netlify Drag & Drop

1. Ir a netlify.com
2. Drag & drop la carpeta `frontend/`
3. Listo!

### Configuración de Producción

Antes de desplegar, actualizar `config.js`:

```javascript
const CONFIG = {
  API_BASE_URL: 'https://tu-api-backend.com/api',  // ← URL de producción
  // ...
};
```

## 🎨 Personalización

### Cambiar Colores

Editar `assets/css/variables.css`:

```css
:root {
  --color-primary: #3B82F6;      /* Azul → Cambia a tu color */
  --color-success: #10B981;      /* Verde */
  --color-error: #EF4444;        /* Rojo */
  /* ... más colores ... */
}
```

### Cambiar Logo

En todas las páginas HTML, en el navbar:

```html
<div class="navbar-brand">🎫 EventBooking</div>
<!-- Cambiar emoji o agregar logo -->
<div class="navbar-brand"><img src="logo.png" alt="Logo"></div>
```

### Cambiar Textos

Simplemente buscar y reemplazar el texto en los HTML.

## 📞 Soporte

Si tienes problemas:

1. **Revisa el README.md** - Documentación completa
2. **Abre DevTools** (F12) - Mira la consola para errores
3. **Revisa Network** - Verifica que las peticiones se envían
4. **Limpia caché** - Ctrl+Shift+Del (Chrome)

## 🚀 Siguientes Pasos

Una vez funcionando:

- [ ] Personalizar colores y logo
- [ ] Desplegar a producción
- [ ] Configurar dominio personalizado
- [ ] Agregar más validaciones
- [ ] Mejorar UX/UI según feedback
- [ ] Agregar más funcionalidades

---

**¡Listo! Ya deberías tener el frontend funcionando. ¡Que disfrutes!** 🎉

Última actualización: 29/05/2024
