# 💵 Deuda Cero  

> **API REST** desarrollada con **Spring Boot** para la gestión de gastos entre usuarios de manera sencilla y sin complicaciones.  

Incluye autenticación mediante **JWT**, creación de grupos, invitaciones, movimientos y un **dashboard interactivo** donde podrás visualizar:  
* Lo que **te deben**  
* Lo que **debes**  
* Tu **balance general**  

✨ Nuevas funcionalidades se seguirán incorporando en las próximas semanas y meses.  

---

## 🚀 Funcionalidades principales  

- 📝 [Registro de usuarios](ca://s?q=Registro_de_usuarios)  
- 🔑 [Inicio de sesión con JWT](ca://s?q=Inicio_de_sesion_con_JWT)  
- 🎟️ [Access Tokens](ca://s?q=Access_Tokens) y [Refresh Tokens](ca://s?q=Refresh_Tokens)  
- 👥 [Roles de aplicación](ca://s?q=Roles_USER_y_ADMIN): **USER** y **ADMIN**  
- 👤 [Roles en grupos](ca://s?q=Roles_en_grupos): **MIEMBRO** y **ADMIN**  
- 🏘️ [Creación de grupos](ca://s?q=Creacion_de_grupos)  
- 💸 [Gestión de movimientos](ca://s?q=Creacion_de_movimientos)  
- 📊 [Cálculo automático de balance](ca://s?q=Calculo_de_balance) por miembro  
- 📈 [Dashboard personalizado](ca://s?q=Dashboard_de_miembro) por grupo  
- ✉️ [Invitaciones a grupos](ca://s?q=Creacion_de_invitaciones)  
- ✅ [Validaciones de negocio](ca://s?q=Validaciones_de_negocio)  
- ⚠️ [Manejo global de excepciones](ca://s?q=Manejo_global_de_excepciones)  
- 🧪 [Testing unitario](ca://s?q=Testing_unitario) (en progreso)  
- 📖 [Documentación con Swagger/OpenAPI](ca://s?q=Documentacion_con_Swagger_OpenAPI) (próximamente)  

---

## 🛠️ Tecnologías utilizadas  

| Tecnología | Uso principal |
|------------|---------------|
| ☕ **Java 21** | Lenguaje base |
| 🚀 **Spring Boot 4.0.6** | Framework principal |
| 🔐 **Spring Security** | Autenticación y autorización |
| 🗄️ **Spring Data JPA** | Persistencia de datos |
| 🛢️ **MySQL** | Base de datos |
| 🔑 **JWT (JJWT)** | Tokens de sesión |
| 📦 **Hibernate** | ORM |
| ⚙️ **Maven** | Gestión de dependencias |
| 🧩 **Lombok** | Simplificación de código |
| 📖 **Swagger/OpenAPI** | Documentación (pendiente) |
| 🧪 **JUnit 5** | Testing |
| 🧪 **Mockito** | Mocking en pruebas |

---

## 🏗️ Arquitectura  

El proyecto sigue una **arquitectura en capas**:  

```plaintext
Controller
   ↓
Service
   ↓
Repository
   ↓
Base de Datos
```

Además utiliza:

- 📦 **DTOs** para entrada y salida de datos
- 🔄 **Mappers** para conversiones
- 🔐 **Spring Security** para autenticación y autorización
- 🔑 **JWT** para sesiones
- ⚠️ **Exception Handlers** para errores centralizados

## 🔒 Seguridad

La autenticación se realiza mediante JWT.

Características implementadas:

- 🔑 **Access Token**
- 🔄 **Refresh Token**
- 👥 Roles: **USER** y **ADMIN**
- 🔐 Endpoints protegidos con **Spring Security**
- 🛡️ Autorización basada en roles
- 🚪 Logout mediante invalidación de Refresh Token
- 👤 Endpoint que devuelve información del usuario autenticado
- 🛡️ Seguridad ante **BOLA** (*Broken Object Level Authentication*)