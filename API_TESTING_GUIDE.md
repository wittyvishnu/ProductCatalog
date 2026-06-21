# ProductCatalog API Testing Guide

## Quick Start

### 1. Register a User (Signup)
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

Expected Response (201):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john@example.com",
  "username": "john_doe",
  "role": "USER"
}
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

Expected Response (200):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john@example.com",
  "username": "john_doe",
  "role": "USER"
}
```

### 3. Get Current User
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <your_jwt_token>"
```

Expected Response (200):
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "password": "$2a$10$...",
  "role": "USER"
}
```

---

## Product Management (Requires Authentication)

### 4. Create Product (ADMIN ONLY)

First, register an admin user or update a user's role in the database to ADMIN.

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <admin_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "price": 999.99,
    "description": "High-performance laptop with SSD"
  }'
```

Expected Response (201):
```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "description": "High-performance laptop with SSD",
  "createdBy": 2,
  "createdAt": "2024-06-21T10:30:00",
  "updatedAt": "2024-06-21T10:30:00"
}
```

### 5. View All Products (Authenticated Users)
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer <your_jwt_token>"
```

Expected Response (200):
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "price": 999.99,
    "description": "High-performance laptop with SSD",
    "createdBy": 2,
    "createdAt": "2024-06-21T10:30:00",
    "updatedAt": "2024-06-21T10:30:00"
  },
  {
    "id": 2,
    "name": "Mouse",
    "price": 29.99,
    "description": "Wireless mouse",
    "createdBy": 2,
    "createdAt": "2024-06-21T11:00:00",
    "updatedAt": "2024-06-21T11:00:00"
  }
]
```

### 6. Get Single Product (Authenticated Users)
```bash
curl -X GET http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer <your_jwt_token>"
```

Expected Response (200):
```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "description": "High-performance laptop with SSD",
  "createdBy": 2,
  "createdAt": "2024-06-21T10:30:00",
  "updatedAt": "2024-06-21T10:30:00"
}
```

### 7. Update Product (ADMIN ONLY)
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer <admin_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "price": 1299.99,
    "description": "High-performance gaming laptop with RTX 4090"
  }'
```

Expected Response (200):
```json
{
  "id": 1,
  "name": "Gaming Laptop",
  "price": 1299.99,
  "description": "High-performance gaming laptop with RTX 4090",
  "createdBy": 2,
  "createdAt": "2024-06-21T10:30:00",
  "updatedAt": "2024-06-21T11:15:00"
}
```

### 8. Delete Product (ADMIN ONLY)
```bash
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer <admin_jwt_token>"
```

Expected Response (204): No Content

---

## Error Scenarios

### 1. Duplicate Email on Signup
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "another_user",
    "email": "john@example.com",
    "password": "password123"
  }'
```

Response (409 Conflict):
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered: john@example.com",
  "path": "/api/auth/signup",
  "timestamp": "2024-06-21T10:35:00"
}
```

### 2. Invalid Login Credentials
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "wrongpassword"
  }'
```

Response (401 Unauthorized):
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/auth/login",
  "timestamp": "2024-06-21T10:35:00"
}
```

### 3. Missing JWT Token
```bash
curl -X GET http://localhost:8080/api/products
```

Response (401 Unauthorized):
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/products",
  "timestamp": "2024-06-21T10:35:00"
}
```

### 4. Non-ADMIN User Trying to Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <user_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product",
    "price": 99.99,
    "description": "Test"
  }'
```

Response (403 Forbidden):
```json
{
  "status": 403,
  "error": "Access Denied",
  "message": "Access is denied",
  "path": "/api/products",
  "timestamp": "2024-06-21T10:35:00"
}
```

### 5. Product Not Found
```bash
curl -X GET http://localhost:8080/api/products/999 \
  -H "Authorization: Bearer <your_jwt_token>"
```

Response (404 Not Found):
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 999",
  "path": "/api/products/999",
  "timestamp": "2024-06-21T10:35:00"
}
```

### 6. Validation Error
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ab",
    "email": "invalid-email",
    "password": "short"
  }'
```

Response (400 Bad Request):
```json
{
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "username": "Username must be between 3 and 20 characters",
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters"
  },
  "path": "/api/auth/signup",
  "timestamp": "2024-06-21T10:35:00"
}
```

---

## Setup for ADMIN User

To create an ADMIN user, you need to either:

### Option 1: Create via Database
```sql
INSERT INTO users (username, email, password, role) 
VALUES ('admin', 'admin@example.com', 'bcrypt_hashed_password', 'ADMIN');
```

### Option 2: Manual SQL Update After Signup
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

### Option 3: Create Admin Setup Endpoint
Implement a protected endpoint that only existing ADMINs can use to promote users to ADMIN role.

---

## Testing with Postman

1. **Signup Request**
   - Method: POST
   - URL: `http://localhost:8080/api/auth/signup`
   - Body (JSON):
     ```json
     {
       "username": "testuser",
       "email": "test@example.com",
       "password": "test123456"
     }
     ```
   - Save the `token` from response

2. **Set Authorization**
   - Go to "Authorization" tab
   - Type: Bearer Token
   - Token: Paste the JWT token from signup response

3. **Test Protected Endpoints**
   - All subsequent requests will use the saved token
   - Try GET /api/products to view all products
   - Try POST /api/products (will fail if not ADMIN)

---

## Important Notes

- **JWT Tokens expire after 24 hours** - Users must login again after expiration
- **Passwords are hashed** with BCrypt - Never store plain text passwords
- **ADMIN role is required** to create, update, or delete products
- **Regular USER role** can only view products
- **Email must be unique** - Each user needs a unique email address
- **Bearer token format** - Always use "Bearer <token>" in Authorization header
