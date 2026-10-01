# Bookworm

Bookworm is a full-stack digital library platform built with **Spring Boot and React**. It provides users with a platform to browse, purchase, rent, and manage digital books, while administrators can manage books, categories, users, and library content.

## Features

* User registration and login
* JWT-based authentication and authorization
* Google OAuth2 authentication
* Role-based access control
* Book management
* Category and subcategory management
* Book search and browsing
* Book purchase and rental
* User library management
* Library packages and user shelves
* Book/file uploads
* Automatic handling of expired rentals
* Admin management

## Technology Stack

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* REST APIs
* MySQL
* Maven

### Frontend

* React
* TypeScript
* Vite

### DevOps

* Docker
* Docker Compose

## Architecture

The application follows a layered architecture:

```text
React Frontend
      │
      ▼
REST API
      │
      ▼
Spring Boot
      │
      ├── Controller
      │
      ├── Service
      │
      ├── Repository
      │
      ▼
   MySQL
```

## Main Modules

### Authentication

* JWT authentication
* Google OAuth2 login
* Role-based authorization

### Books

* Book management
* Categories and subcategories
* Book metadata and uploads

### Purchase & Rental

Users can purchase or rent books and access them through their personal library.

```text
User
 ↓
Purchase / Rental
 ↓
Transaction
 ↓
User Library
```

### User Library

The library system manages books available to users through:

* Purchased books
* Rented books
* Library packages
* User shelves

## Project Structure

```text
Bookworm/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── docker-compose.yml
│
└── frontend/
    ├── src/
    ├── package.json
    └── vite.config.ts
```

## Running the Project

### Backend with Docker

```bash
cd backend
docker compose up --build
```

Stop containers:

```bash
docker compose down
```

Remove containers and database volumes:

```bash
docker compose down -v
```

> `docker compose down -v` deletes the database data stored in Docker volumes.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

## Backend API Example

Get a user's purchases:

```http
GET /api/v1/users/{userId}/purchases
```

Request flow:

```text
Controller → Service → Repository → MySQL
```

## Development

* Spring Boot DevTools for backend development
* Vite HMR for frontend development
* Docker Compose for local infrastructure
* MySQL for persistent data

## Future Improvements

* Redis caching
* Kafka event streaming
* Payment service
* Notification service
* API Gateway
* Prometheus & Grafana monitoring
* CI/CD pipeline
* Cloud deployment  

