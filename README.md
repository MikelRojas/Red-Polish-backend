# Red Polish – Backend API

Backend desarrollado con **Spring Boot** que soporta una plataforma híbrida de **E-Commerce + Sistema de Reservas**, integrando autenticación segura, control de roles y procesamiento de pagos.

---

## Descripción

Red Polish Backend expone una API RESTful diseñada bajo arquitectura en capas y principios SOLID. Centraliza la lógica de negocio para:

- Gestión de usuarios y autenticación JWT  
- Administración de productos y categorías  
- Carrito de compras y promociones  
- Procesamiento de pagos  
- Gestión de servicios y citas  

El sistema está estructurado para ser mantenible, escalable y desacoplado del frontend.

---

## Arquitectura

Organización por capas:

controller
service
service.impl
repository
entity
dto
mapper
config
exception
utils


### Principios aplicados

- Separación de responsabilidades  
- DTOs para desacoplar entidades  
- Mappers dedicados  
- Manejo global de excepciones  
- Seguridad centralizada  

---

## Tecnologías

- Java 17+
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven

---

## Módulos Funcionales

### Autenticación y Seguridad
- Login y registro
- Generación y validación de JWT
- Control de acceso por roles

### E-Commerce
- CRUD de productos
- Categorías
- Carrito persistente
- Sistema de promociones
- Registro de compras

### Sistema de Reservas
- Gestión de servicios
- Creación y validación de citas
- Control de disponibilidad

### Pagos
- Integración con múltiples pasarelas
- Abstracción por servicios

---

## Ejecución

```bash
mvn clean install
mvn spring-boot:run
La API se ejecuta por defecto en:

http://localhost:8080
Diseño Orientado a Escalabilidad
El proyecto está preparado para:

Implementar contenedorización (Docker)

Integrar documentación Swagger

Añadir pruebas automatizadas

Escalar hacia arquitectura distribuida

Rol dentro del Ecosistema
Este backend funciona como:

Núcleo de negocio

Servidor de autenticación

Gestor de pagos

Motor de reservas

API central para frontend

<div align="center" style="margin-top:20px; font-size:14px; color:gray;"> Arquitectura limpia · Seguridad robusta · Diseño escalable </div> ```