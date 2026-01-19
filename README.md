# 🐾 VeterinariaApp - Sumativa 1 (Desarrollo App Móviles II)

## 📖 Descripción del Proyecto
**VeterinariaApp** es una solución móvil integral diseñada para la gestión de atenciones veterinarias y ventas de farmacia. El proyecto aplica principios avanzados de desarrollo en Android, enfocándose en la modularidad, la persistencia de datos y una experiencia de usuario (UX) fluida y accesible.

---

## 🛠️ Pilares Tecnológicos y Arquitectura

### 1. Arquitectura y Patrones
- **MVVM (Model-View-ViewModel):** Implementación de una arquitectura limpia que separa la lógica de negocio de la interfaz de usuario.
- **StateFlow y Coroutines:** Manejo reactivo de estados para actualizaciones en tiempo real sin bloqueos del hilo principal.
- **Repository Pattern:** Abstracción de la fuente de datos (Room y SharedPreferences) a través de una interfaz, facilitando el mantenimiento y las pruebas unitarias.

### 2. Componentes Nativos de Android
- **Services (Foreground):** Implementación de `NotificacionService` para proporcionar feedback inmediato al usuario tras acciones críticas (agendamiento, compras, compartir).
- **Broadcast Receivers:** Uso de `ConnectivityReceiver` para monitorear el estado de red de forma global.
- **Intents:** 
    - **Explícitos:** Comunicación directa con servicios internos (Notificaciones).
    - **Implícitos:** Integración con el sistema operativo para la funcionalidad de "Compartir Resumen" (`ACTION_SEND`).
- **Room Persistence:** Base de datos local para la persistencia de Mascotas, Consultas y Pedidos.
- **Content Provider:** `VeterinariaProvider` diseñado para permitir el acceso seguro a los datos de las mascotas por aplicaciones autorizadas.

### 3. Lógica de Negocio Avanzada (UX)
- **Agenda Inteligente:** Sistema de asignación automática de veterinarios que verifica disponibilidad en tiempo real consultando la base de datos, evitando colisiones de horarios.
- **Flujo Híbrido:** Capacidad de procesar ventas directas de farmacia (Venta Mostrador) de forma independiente a las consultas clínicas, permitiendo un uso flexible de la app.
- **Tematización Dinámica:** Soporte completo para **Modo Oscuro** (Dark Mode) adaptativo mediante Material Design 3, garantizando legibilidad y confort visual.

---

## 🎨 Principios de Diseño y UX
- **Jerarquía Visual:** Uso riguroso de **Material Design 3**, con una paleta de colores profesional basada en tonos verdes que transmiten salud y confianza.
- **Accesibilidad:** Etiquetas claras, contrastes validados para modo luz/noche y navegación coherente a través de un flujo de registro por etapas.
- **Feedback Continuo:** Uso de animaciones, indicadores de progreso (`CircularProgressIndicator`) y notificaciones de sistema para mantener al usuario informado en cada paso.

---

## 🚀 Funcionalidades Clave
- ✅ **Registro Multietapa:** Formulario validado para Dueño, Mascota y Selección de Servicio.
- ✅ **Carrito de Compras:** Sistema de farmacia con catálogo de medicamentos, cálculo automático de totales y aplicación de descuentos.
- ✅ **Resumen Transaccional:** Pantalla final con detalle de cita, desglose de farmacia y opción de compartir el resumen en redes sociales.
- ✅ **Listado de Agenda:** Visualización organizada de las próximas citas agendadas, permitiendo llevar un control histórico.

---

## 🧪 Pruebas y Calidad
- **Unit Testing:** Implementación de pruebas unitarias para validar la lógica de agendamiento y cálculos de costos de servicios.
- **Validaciones Robustas:** Uso de expresiones regulares (Regex) para garantizar la integridad de correos electrónicos, números telefónicos y datos numéricos en los formularios.

---

## 📂 Estructura del Proyecto
```text
cl.duoc.veterinaria
├── data             # Repositorio y persistencia (Room / Entities)
├── model            # Entidades de dominio y modelos de datos
├── service          # Lógica de agenda, costos y NotificacionService
├── ui               # Componentes de interfaz (Compose)
│   ├── registro     # Flujo de agendamiento (ResumenScreen, etc.)
│   ├── viewmodel    # Lógica de estado y ViewModels (MVVM)
│   └── theme        # Definición de estilos, tipografía y colores
└── util             # Funciones de ayuda y validaciones (ValidationUtils)
```

---
**Desarrollado por:** Liliana Tapia  
**Carrera:** Desarrollo de aplicaciones
**Institución:** DUOC UC

---

### Instrucciones de Instalación
1. Clonar el repositorio.
2. Abrir en **Android Studio Koala** (o superior).
3. Sincronizar Gradle.
4. Ejecutar en un emulador o dispositivo físico con **API 33+**.
