# ProductCatalog Security & Product Management Implementation

## Overview
Complete implementation of JWT-based authentication with role-based access control (RBAC) for a Spring Boot product management system.

## Implemented Components

### 1. JWT Security Layer
- **JwtUtils.java** - JWT token generation and validation
  - `generateJwtTokenFromEmail()` - Creates JWT tokens from user email
  - `getEmailFromJwtToken()` - Extracts email from JWT
  - `validateJwtToken()` - Validates JWT signature and expiration
  
- **AuthTokenFilter.java** - Request interceptor for JWT validation
  - Intercepts each request to validate JWT token
  - Sets authentication context with user details
  - Parses Bearer token from Authorization header
  
- **AuthEntryPointJwt.java** - Custom 401 error handler
  - Returns JSON error response for unauthorized requests
  - Provides clear error messages and request path info

### 2. Data Transfer Objects (DTOs)
- **LoginRequest** - Email and password for login
- **LoginResponse** - JWT token and user information
- **SignupRequest** - Username, email, password for account creation
- **ProductRequest** - Product CRUD operations
- **ProductResponse** - Product data with timestamps
- **ErrorResponse** - Standardized error messages

### 3. Exception Handling
- **UserAlreadyExistsException** - Thrown on duplicate email signup (409 Conflict)
- **InvalidCredentialsException** - Thrown on failed login (401 Unauthorized)
- **ProductNotFoundException** - Thrown when product not found (404 Not Found)
- **GlobalExceptionHandler** - Centralized exception handling with proper HTTP status codes

### 4. Authentication Service (userService)
- **signup()** - Create new user with USER role
  - Validates email uniqueness
  - Hashes password with BCrypt
  - Returns JWT token
  
- **login()** - Authenticate and generate JWT
  - Validates email/password combination
  - Generates JWT token for authenticated user
  - Returns user details with token
  
- **getUserByEmail()** - Retrieve user by email
- **getUserById()** - Retrieve user by ID

### 5. Product Management Service (productService)
- **addProduct()** - Create product (ADMIN only)
- **getProduct()** - Retrieve single product (Authenticated)
- **getAllProducts()** - List all products (Authenticated)
- **editProduct()** - Update product (ADMIN only)
- **deleteProduct()** - Delete product (ADMIN only)

### 6. REST Controllers

#### User Controller (`/api/auth`)
- `POST /api/auth/signup` - Create account (Public)
- `POST /api/auth/login` - Login user (Public)
- `GET /api/auth/me` - Get current user info (Authenticated)

#### Product Controller (`/api/products`)
- `POST /api/products` - Create product (ADMIN only)
- `GET /api/products` - List all products (Authenticated)
- `GET /api/products/{id}` - Get product details (Authenticated)
- `PUT /api/products/{id}` - Update product (ADMIN only)
- `DELETE /api/products/{id}` - Delete product (ADMIN only)

### 7. Security Configuration (SecurityConfig)
- JWT filter chain integration
- Stateless session policy
- Role-based authorization with @PreAuthorize annotations
- CSRF disabled for REST API
- Password encoding with BCryptPasswordEncoder
- Custom authentication entry point for error handling

### 8. Database Models

#### User Model
- id (PK)
- username
- email (Unique)
- password (BCrypt hashed)
- role (USER or ADMIN)

#### Product Model
- id (PK)
- name
- price
- description
- created_by (User ID)
- created_at (Timestamp)
- updated_at (Timestamp)

## Authentication Flow

1. **Signup**
   - POST /api/auth/signup with SignupRequest
   - System checks email uniqueness
   - Password hashed with BCrypt
   - User created with USER role
   - JWT token returned

2. **Login**
   - POST /api/auth/login with LoginRequest
   - Email and password validated
   - JWT token generated
   - User details returned with token

3. **Authenticated Request**
   - Client sends JWT in Authorization header: `Bearer <token>`
   - AuthTokenFilter intercepts and validates token
   - User email extracted and set as authentication principal
   - Request proceeds with authentication context

## Authorization Rules

### Public Endpoints
- `/api/auth/signup` - Anyone can register
- `/api/auth/login` - Anyone can login
- `/` - Health check

### Authenticated (USER and ADMIN)
- `GET /api/products` - View all products
- `GET /api/products/{id}` - View product details
- `GET /api/auth/me` - Get current user info

### Admin Only
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

## Security Features

- JWT-based stateless authentication
- BCrypt password hashing
- Email uniqueness validation
- Role-based access control (RBAC)
- Token expiration (24 hours by default)
- Centralized exception handling
- CORS-ready configuration
- Method-level security with @PreAuthorize

## Environment Configuration

```properties
# JWT Configuration
jwt.secret=MyVerySecretKeyForJwtAuthenticationProject123456789
jwt.expiration=86400000 (24 hours in milliseconds)

# Database
spring.datasource.url=jdbc:postgresql://...
spring.datasource.username=...
spring.datasource.password=...
spring.jpa.hibernate.ddl-auto=update
```

## API Usage Examples

### Signup
```bash
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Response (201 Created):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john@example.com",
  "username": "john_doe",
  "role": "USER"
}
```

### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john@example.com",
  "username": "john_doe",
  "role": "USER"
}
```

### Add Product (ADMIN)
```bash
POST /api/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Laptop",
  "price": 999.99,
  "description": "High-performance laptop"
}

Response (201 Created):
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "description": "High-performance laptop",
  "createdBy": 2,
  "createdAt": "2024-06-21T10:30:00",
  "updatedAt": "2024-06-21T10:30:00"
}
```

### View Products
```bash
GET /api/products
Authorization: Bearer <token>

Response (200 OK):
[
  {
    "id": 1,
    "name": "Laptop",
    "price": 999.99,
    "description": "High-performance laptop",
    "createdBy": 2,
    "createdAt": "2024-06-21T10:30:00",
    "updatedAt": "2024-06-21T10:30:00"
  }
]
```

## Database Schema

The system automatically creates tables using Hibernate's DDL auto-update feature:

### users table
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255),
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255),
  role VARCHAR(50) DEFAULT 'USER'
);
```

### products table
```sql
CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255),
  price DOUBLE PRECISION,
  description TEXT,
  created_by BIGINT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

## Notes
- All user passwords are hashed using BCryptPasswordEncoder
- Default role for new users is USER
- ADMIN role must be assigned manually via database or admin endpoint (if implemented)
- JWT tokens expire after 24 hours (configurable in application.properties)
- All timestamps are automatically managed by Hibernate
