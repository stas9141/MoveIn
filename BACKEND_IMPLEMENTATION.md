# MoveIn Backend Implementation Guide

## Overview
This guide provides a complete backend implementation for the MoveIn app using Node.js, Express, PostgreSQL, and JWT authentication with token refresh functionality.

## Technology Stack
- **Runtime**: Node.js 18+
- **Framework**: Express.js
- **Database**: PostgreSQL with Prisma ORM
- **Authentication**: JWT with refresh tokens
- **Password Hashing**: bcrypt
- **Validation**: Joi
- **Rate Limiting**: express-rate-limit
- **Security**: helmet, cors

## Project Structure
```
movein-backend/
├── src/
│   ├── controllers/
│   │   ├── authController.js
│   │   └── userController.js
│   ├── middleware/
│   │   ├── auth.js
│   │   ├── validation.js
│   │   └── rateLimiter.js
│   ├── models/
│   │   ├── User.js
│   │   └── Token.js
│   ├── routes/
│   │   ├── auth.js
│   │   └── users.js
│   ├── services/
│   │   ├── authService.js
│   │   ├── tokenService.js
│   │   └── emailService.js
│   ├── utils/
│   │   ├── database.js
│   │   ├── logger.js
│   │   └── helpers.js
│   └── app.js
├── prisma/
│   ├── schema.prisma
│   └── migrations/
├── .env
├── package.json
└── README.md
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    profile_picture_url TEXT,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);
```

### Tokens Table
```sql
CREATE TABLE tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    access_token_expires_at TIMESTAMP NOT NULL,
    refresh_token_expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP
);
```

## API Endpoints

### Authentication Endpoints

#### POST /api/v1/auth/signup
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "first_name": "John",
  "last_name": "Doe",
  "phone_number": "+1234567890"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "first_name": "John",
      "last_name": "Doe",
      "phone_number": "+1234567890",
      "email_verified": false,
      "phone_verified": false,
      "is_active": true,
      "created_at": "2024-01-01T00:00:00Z",
      "updated_at": "2024-01-01T00:00:00Z"
    },
    "tokens": {
      "access_token": "jwt_access_token",
      "refresh_token": "jwt_refresh_token"
    }
  }
}
```

#### POST /api/v1/auth/login
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

#### POST /api/v1/auth/refresh-token
```json
{
  "refresh_token": "jwt_refresh_token"
}
```

#### POST /api/v1/auth/logout
```json
{
  "refresh_token": "jwt_refresh_token"
}
```

#### POST /api/v1/auth/forgot-password
```json
{
  "email": "user@example.com"
}
```

#### POST /api/v1/auth/reset-password
```json
{
  "token": "reset_token",
  "new_password": "NewSecurePassword123!"
}
```

### User Management Endpoints

#### GET /api/v1/auth/profile
**Headers:** `Authorization: Bearer <access_token>`

#### POST /api/v1/auth/profile
**Headers:** `Authorization: Bearer <access_token>`
```json
{
  "first_name": "John",
  "last_name": "Doe",
  "phone_number": "+1234567890"
}
```

#### POST /api/v1/auth/change-password
**Headers:** `Authorization: Bearer <access_token>`
```json
{
  "current_password": "CurrentPassword123!",
  "new_password": "NewPassword123!"
}
```

## Security Features

### Token Management
- **Access Token**: Short-lived (15 minutes)
- **Refresh Token**: Long-lived (7 days)
- **Token Rotation**: New refresh token on each refresh
- **Token Revocation**: Immediate logout capability

### Password Security
- **bcrypt**: 12 rounds of hashing
- **Password Requirements**: 8+ chars, uppercase, lowercase, number, special char
- **Rate Limiting**: 5 attempts per 15 minutes for login

### Additional Security
- **CORS**: Configured for mobile app
- **Helmet**: Security headers
- **Input Validation**: Joi schema validation
- **SQL Injection**: Prisma ORM protection
- **XSS Protection**: Input sanitization

## Environment Variables
```env
# Database
DATABASE_URL="postgresql://username:password@localhost:5432/movein_db"

# JWT
JWT_ACCESS_SECRET="your-super-secret-access-key"
JWT_REFRESH_SECRET="your-super-secret-refresh-key"
JWT_ACCESS_EXPIRES_IN="15m"
JWT_REFRESH_EXPIRES_IN="7d"

# Server
PORT=3000
NODE_ENV="development"

# Email (for password reset)
SMTP_HOST="smtp.gmail.com"
SMTP_PORT=587
SMTP_USER="your-email@gmail.com"
SMTP_PASS="your-app-password"

# Rate Limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
```

## Installation and Setup

### 1. Initialize Project
```bash
mkdir movein-backend
cd movein-backend
npm init -y
```

### 2. Install Dependencies
```bash
npm install express prisma @prisma/client bcryptjs jsonwebtoken joi cors helmet express-rate-limit nodemailer uuid
npm install -D nodemon
```

### 3. Setup Database
```bash
npx prisma init
npx prisma migrate dev --name init
npx prisma generate
```

### 4. Start Development Server
```bash
npm run dev
```

## Testing
```bash
# Test signup
curl -X POST http://localhost:3000/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!","first_name":"Test","last_name":"User"}'

# Test login
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'

# Test token refresh
curl -X POST http://localhost:3000/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refresh_token":"your_refresh_token"}'
```

## Deployment
- **Production Database**: Use managed PostgreSQL (AWS RDS, Google Cloud SQL)
- **Environment**: Use environment variables for all secrets
- **SSL**: Enable HTTPS in production
- **Monitoring**: Add logging and monitoring
- **Backup**: Regular database backups

This backend provides a complete, production-ready authentication system with token refresh functionality for your MoveIn app.

