# Gestor de Comunidad

![License](https://img.shields.io/badge/Licencia-CC%20BY%204.0-2E7D32?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-Kotlin-3DDC84?style=for-the-badge&logo=android)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-FFCA28?style=for-the-badge&logo=firebase)

<p align="center">
  <b>Aplicación Android para gestionar eventos comunitarios</b><br>
  Autenticación, perfil de usuario, eventos, asistencia y comentarios con Firebase.
</p>

## ✨ Lo que hace

- 🔐 Inicio de sesión con correo y contraseña
- 👤 Registro de usuarios
- 🔎 Inicio de sesión con Google
- 🧾 Perfil de usuario con datos en Firestore
- 📅 Creación y listado de eventos
- 🧭 Navegación lateral entre perfil, eventos y cierre de sesión
- ✅ Registro de asistencia a eventos
- 💬 Comentarios y valoraciones en eventos

## 🧰 Tecnologías

- Kotlin
- Android SDK
- Firebase Authentication
- Firebase Firestore
- Credential Manager
- Material 3

## 🚀 Instalación

1. Clonar el repositorio.
2. Ábrirlo en Android Studio.
3. Sincroniza Gradle.
4. Configura Firebase con tu archivo `google-services.json`.
5. Ejecutar la app en un emulador o dispositivo físico.

## 📱 Flujo principal

```text
Login -> Registro / Google -> Perfil -> Eventos -> Detalle / Crear / Asistir
```

## 🧩 Estructura general

- `LoginActivity` para autenticación
- `RegisterActivity` para registro
- `ProfileActivity` para perfil y eventos del usuario
- `EventActivity` para el flujo de eventos
- `AuthRepository` para Firebase Auth
- `FirestoreRepository` para datos de usuario
- `EventRepository` para operaciones de eventos

## 👥 Equipo de Desarrollo

| Nombre | Carnet |
| --- | --- |
| Ronald Alexander Martínez Gutiérrez | MG223061 |
| Katherine Paola Pineda Rodríguez | PR232427 |
| René Francisco Guevara Alfaro | GA202826 |
| Karina Lisbeth Angel Quezada | AQ161844 |

## 🎨 Estilo visual

- Paleta principal en verde
- Componentes Material
- Interfaz limpia y orientada a comunidad

## 📄 Licencia

Este proyecto está bajo la licencia [Creative Commons Attribution 4.0 International (CC BY 4.0)](LICENSE).
