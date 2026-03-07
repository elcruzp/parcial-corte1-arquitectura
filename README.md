# ERP Iglesias — Diagnóstico Arquitectónico y Aplicación de Patrones de Diseño

## Parcial Primer Corte — Patrones de Diseño (Creacionales y Estructurales)

**Asignatura:** Arquitectura de Software
**Profesor:** Luis Angel Vargas Narvaez

**Integrantes**

* Juan Camilo Cruz Pardo
* Daniel Steven Fontalvo Matiz

**Fecha:** 09 Marzo 2026

---

# 1. Contexto del Proyecto

El proyecto **ERP Iglesias** es un sistema ERP (Enterprise Resource Planning) diseñado para la gestión administrativa de iglesias.  
El sistema permite administrar información relacionada con:

* iglesias
* usuarios
* roles
* permisos
* datos administrativos

La aplicación está compuesta por una arquitectura cliente-servidor con frontend, backend y base de datos.  
Como arquitectos junior, el objetivo de este trabajo fue:

* Diagnosticar la arquitectura actual
* Identificar problemas de diseño
* Proponer mejoras usando patrones de diseño
* Aplicar principios SOLID
* Implementar cambios sin afectar la funcionalidad del sistema  


### Repositorio base analizado:  
- https://github.com/lanvargas94/erp_iglesias  
---

# 2. Stack Tecnológico del Proyecto

Durante el diagnóstico se identificó el siguiente stack tecnológico.

| Componente    | Tecnología        |
| ------------- | ----------------- |
| Frontend      | Angular           |
| Backend       | Java Spring Boot  |
| Base de datos | PostgreSQL        |
| Contenedores  | Docker            |
| Orquestación  | Docker Compose    |
| Lenguajes     | Java / TypeScript |
  


### Puertos principales del sistema:

- Frontend

    [http://localhost:4200](http://localhost:4200)

- Backend

    [http://localhost:8080](http://localhost:8080)

- Base de datos

    localhost:5432 (configurado manualmente en pg Admin 4)  

![pg Admin 4](<evidencias/04-database accesed.png>)  

---

# 3. Estructura del Proyecto

La estructura general del repositorio es la siguiente:

```
erp_iglesias
│
├── backend
│
├── frontend
│
├── ADR
│
├── evidencias
│
├── docker-compose.yml
│
└──README.md
```

### Componentes:

**Backend**

Contiene la API REST desarrollada en Spring Boot.

**Frontend**

Interfaz web desarrollada en Angular.

**ADR**

Carpeta que contiene el documento de decisiones arquitectónicas.

**Evidencias**

Carpeta que contiene:

* Capturas de pruebas
* Diagrama MER
* Evidencia funcional

---

# 4. Diagnóstico Arquitectónico

---

# 5. Diagrama MER (Modelo Entidad-Relación)

---

# 6. Architecture Decision Record (ADR)

---

# 7. Decisiones Arquitectónicas Propuestas (10)
# 8. Cambios Implementados (5)
# 9. Commits Implementados
# 10. Pruebas Funcionales
# 11. Ejecución del Proyecto

Para ejecutar el sistema localmente se utiliza Docker.

1. Clonar repositorio

```
git clone <repo>
cd erp_iglesias
```

2. Levantar contenedores

```
docker compose up -d
```

3. Acceder a los servicios

- Frontend

```
http://localhost:4200
```

- Backend

```
http://localhost:8080
```

- Base de datos

```
localhost:5432 (configurada manualmente en pg Admin 4)
```

---

# 12. Conclusiones

# Autores

* Juan Camilo Cruz Pardo  
Estudiante de Ingeniería de Sistemas

* Daniel Steven Fontalvo Matiz  
Estudiante de Ingeniería de Sistemas

---
