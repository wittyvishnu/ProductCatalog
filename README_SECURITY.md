# ProductCatalog - Security Implementation Guide

## Project Overview

ProductCatalog is a Spring Boot application that provides a secure REST API for managing a product catalog with JWT-based authentication and role-based access control (RBAC).

## Key Features

✅ **JWT Authentication** - Stateless token-based authentication with 24-hour expiration
✅ **User Management** - Signup, login, and user profile retrieval
✅ **Role-Based Access Control** - USER and ADMIN roles with different permissions
✅ **Product CRUD** - Create, read, update, delete products with audit trails
✅ **Security Best Practices** - BCrypt password hashing, CSRF protection, input validation
✅ **Error Handling** - Centralized exception handling with meaningful error responses

## Architecture Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ 1. POST /api/auth/signup
       │    POST /api/auth/login
       │ 2. Add JWT in Authorization header
       ▼
┌─────────────────────────────────────┐
│    AuthTokenFilter                  │  JWT Validation Layer
│    - Extracts JWT token             │  - Validates signature
│    - Sets SecurityContext           │  - Checks expiration
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│    @PreAuthorize Annotations        │  Method-Level Security
│    - hasRole('ADMIN')               │  - Role-based filtering
│    - isAuthenticated()              │  - Endpoint protection
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│    Controller Layer                 │  REST Endpoints
│    - User Controller                │  - Auth endpoints
│    - Product Controller             │  - Product CRUD
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│    Service Layer                    │  Business Logic
│    - userService                    │  - Authentication
│    - productService                 │  - Product operations
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│    Repository Layer                 │  Data Access
│    - userRepo                       │  - CRUD operations
│    - productRepo                    │  - Database queries
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│    Database (Supabase PostgreSQL)   │
│    - users table                    │
│    - products table                 │
└─────────────────────────────────────┘
```

## Security Layers

### Layer 1: Authentication (Login/Signup)
- User provides email and password
- Password validated against BCrypt hash
- JWT token generated on successful authentication
- Token contains user email as subject
- Token expires after 24 hours

### Layer 2: Request Interception (AuthTokenFilter)
- Every authenticated request intercepted
- JWT token extracted from Authorization header
- Token signature validated using secret key
- Token expiration checked
- User context set for the request

### Layer 3: Authorization (@PreAuthorize)
- Method-level authorization annotations
- Role-based checks (ADMIN vs USER)
- Permission-based checks (isAuthenticated())
- Request denied if user lacks required role/permission

### Layer 4: Exception Handling (GlobalExceptionHandler)
- Centralized error handling
- Proper HTTP status codes returned
- Security-relevant errors don't expose sensitive info
- Meaningful error messages for debugging

## User Roles

### USER Role (Default)
- Can signup and login
- Can view products
- Can view their own profile
- **Cannot** create, edit, or delete products

### ADMIN Role
- All USER permissions
- **Can** create products
- **Can** edit products
- **Can** delete products
- Access to all product management endpoints

## Security Considerations

### ✅ Implemented
- JWT tokens for stateless authentication
- BCrypt password hashing (10 rounds)
- Email uniqueness validation
- Token expiration (24 hours)
- Role-based access control
- CSRF protection disabled for REST API (intentional)
- Method-level security annotations
- Centralized exception handling
- Input validation with @Valid annotations
- Secure password requirements

### ⚠️ Considerations for Production

1. **HTTPS/TLS**
   - Always use HTTPS in production
   - Set `secure=true` on cookies
   - Enable HSTS headers

2. **JWT Secret Management**
   - Store JWT secret in environment variables (not in code)
   - Use strong, randomly generated secret (32+ characters)
   - Rotate secret periodically

3. **Token Management**
   - Implement token blacklisting for logout
   - Consider refresh tokens for long sessions
   - Set appropriate expiration times

4. **Rate Limiting**
   - Implement rate limiting on auth endpoints
   - Prevent brute force attacks on login
   - Use libraries like Spring Cloud RateLimiter

5. **Logging and Monitoring**
   - Log all authentication attempts
   - Monitor failed login attempts
   - Alert on suspicious activity
   - Never log passwords or tokens

6. **CORS Configuration**
   - Configure CORS for frontend domain
   - Restrict allowed origins
   - Use environment-specific CORS rules

7. **Input Validation**
   - Validate all user inputs
   - Sanitize email addresses
   - Enforce password complexity
   - Limit request payload sizes

8. **Audit Trail**
   - Track product creation/modification
   - Record who performed which action
   - Maintain audit logs for compliance

## File Structure

```
src/main/java/com/example/product/
├── config/
│   └── SecurityConfig.java              # Spring Security configuration
├── controller/
│   ├── userController.java              # Auth endpoints
│   └── productController.java           # Product CRUD endpoints
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── SignupRequest.java
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   └── ErrorResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java      # Centralized error handling
│   ├── UserAlreadyExistsException.java
│   ├── InvalidCredentialsException.java
│   └── ProductNotFoundException.java
├── jwt/
│   ├── JwtUtils.java                    # JWT token operations
│   ├── AuthTokenFilter.java             # JWT validation filter
│   └── AuthEntryPointJwt.java           # 401 error handler
├── model/
│   ├── userModel.java                   # User entity
│   ├── productModel.java                # Product entity
│   └── Role.java                        # Role enum
├── repo/
│   ├── userRepo.java                    # User repository
│   └── productRepo.java                 # Product repository
└── service/
    ├── userService.java                 # User business logic
    └── productService.java              # Product business logic
```

## Running the Application

### Prerequisites
- Java 17+
- PostgreSQL (Supabase)
- Maven or Maven Wrapper

### Setup

1. **Configure Database**
   ```properties
   spring.datasource.url=jdbc:postgresql://your-db-host:5432/your-db
   spring.datasource.username=your-username
   spring.datasource.password=your-password
   ```

2. **Configure JWT**
   ```properties
   jwt.secret=your-secret-key-min-32-chars
   jwt.expiration=86400000
   ```

3. **Build Project**
   ```bash
   mvn clean build
   ```

4. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Documentation

### Authentication Endpoints

#### Signup
```
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Response: 201 Created
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john@example.com",
  "username": "john_doe",
  "role": "USER"
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Response: 200 OK
```

#### Get Current User
```
GET /api/auth/me
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER"
}
```

### Product Endpoints

#### Create Product (ADMIN)
```
POST /api/products
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Laptop",
  "price": 999.99,
  "description": "High-performance laptop"
}

Response: 201 Created
```

#### Get All Products
```
GET /api/products
Authorization: Bearer <token>

Response: 200 OK
[...]
```

#### Get Product
```
GET /api/products/{id}
Authorization: Bearer <token>

Response: 200 OK
```

#### Update Product (ADMIN)
```
PUT /api/products/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Updated Name",
  "price": 1099.99,
  "description": "Updated description"
}

Response: 200 OK
```

#### Delete Product (ADMIN)
```
DELETE /api/products/{id}
Authorization: Bearer <admin_token>

Response: 204 No Content
```

## Testing

See **API_TESTING_GUIDE.md** for detailed testing instructions with curl examples.

## Troubleshooting

### Issue: "Invalid JWT token"
- **Cause**: Token signature validation failed
- **Solution**: Ensure JWT secret matches between signup and subsequent requests

### Issue: "Email already registered"
- **Cause**: Attempting to signup with existing email
- **Solution**: Use a different email or login with existing account

### Issue: "Access is denied"
- **Cause**: User lacks required ADMIN role
- **Solution**: Contact administrator or use ADMIN account

### Issue: "Full authentication is required"
- **Cause**: Missing or invalid JWT token in Authorization header
- **Solution**: Add valid JWT token to request: `Authorization: Bearer <token>`

## Documentation Files

- **IMPLEMENTATION_SUMMARY.md** - Detailed implementation overview
- **API_TESTING_GUIDE.md** - API testing with curl examples
- **README_SECURITY.md** - This file

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review API_TESTING_GUIDE.md for endpoint examples
3. Check application logs for error details
4. Verify database connectivity and credentials

## License

This project is part of the ProductCatalog system.
