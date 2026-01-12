# VeterinariaApp - Formativa 1 (Desarrollo App Móviles II)

## 📌 Objetivo de la Entrega (Semana 1)
El enfoque principal de esta semana ha sido diseñar una interfaz de usuario funcional utilizando **Jetpack Compose**, aplicando principios de **UX y accesibilidad digital**:
- **Jerarquía Visual**: Uso de tipografías y contrastes de Material Design 3.
- **Accesibilidad**: Navegación coherente y etiquetas descriptivas.
- **Feedback Dinámico**: Validaciones en tiempo real en formularios de registro.

---

## 🛠️ Características Técnicas del Proyecto

### 1. Interfaz de Usuario (UI) Moderna
- Implementación 100% en **Jetpack Compose**.
- Uso de componentes avanzados: `Scaffold`, `LazyColumn`, `StateFlow` para manejo de estados, y animaciones de transición.
- **Navegación**: Flujo estructurado entre datos del dueño y datos de la mascota.

### 2. Componentes Nativos de Android
- **Foreground Service**: `NotificacionService` para alertas y recordatorios de citas.
- **Broadcast Receiver**: `ConnectivityReceiver` que monitorea el estado de la red para asegurar la sincronización.
- **Content Provider**: `VeterinariaProvider` para permitir el acceso seguro a los datos de las mascotas.
- **Intents**: Uso de Intents explícitos para navegación e implícitos para compartir información.

### 3. Lógica de Negocio y Arquitectura
- **Patrón MVVM**: Separación clara entre la vista y la lógica de datos.
- **Principios SOLID**: Aplicación del principio *Open/Closed* en el sistema de medicamentos y descuentos.
- **KISS**: Código simple y mantenible en los modelos de datos (`Mascota`, `Dueno`, `Cliente`).
- **Validaciones**: Utilidades con Regex para asegurar la integridad de correos y teléfonos.

---

## 🧪 Calidad y Pruebas
- **Unit Testing**: Pruebas unitarias con JUnit y Mockito para los ViewModels (ej: `RegistroViewModelTest`), asegurando que la lógica de negocio responda correctamente ante datos válidos e inválidos.

---

## 🚀 Instrucciones de Ejecución
1.  **Requisitos**: Android Studio Koala o superior y API 34 (recomendado).
2.  **Sincronización**: Abrir el proyecto y esperar la sincronización de Gradle.
3.  **Tests**: Para ejecutar las pruebas, clic derecho en la carpeta `test` -> "Run 'Tests in cl.duoc...'".

---
**Autor:** Liliana Tapia  
**Institución:** DUOC UC
