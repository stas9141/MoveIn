# MoveIn Backend API

A secure, production-ready backend API for the MoveIn app with JWT authentication, token refresh, and comprehensive user management.

## Features

- 🔐 **JWT Authentication** with access and refresh tokens
- 🔄 **Token Refresh** with automatic rotation
- 🛡️ **Security** with bcrypt password hashing, rate limiting, and input validation
- 📧 **Email Services** for password reset and verification
- 🗄️ **Database** with PostgreSQL and Prisma ORM
- 📝 **Logging** with Winston
- 🚀 **Production Ready** with error handling and monitoring

## Quick Start

### Prerequisites

- Node.js 18+
- PostgreSQL 12+
- npm or yarn

### Installation

1. **Clone and setup**
   ```bash
   cd backend
   npm install
   ```

2. **Environment Configuration**
   ```bash
   cp env.example .env
   # Edit .env with your configuration
   ```

3. **Database Setup**
   ```bash
   # Create PostgreSQL database
   createdb movein_db
   
   # Run migrations
   npm run db:migrate
   
   # Generate Prisma client
   npm run db:generate
   ```

4. **Start Development Server**
   ```bash
   npm run dev
   ```

The API will be available at `http://localhost:3000`

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/signup` | Register new user | No |
| POST | `/api/v1/auth/login` | Login user | No |
| POST | `/api/v1/auth/refresh-token` | Refresh access token | No |
| POST | `/api/v1/auth/logout` | Logout user | Yes |
| POST | `/api/v1/auth/forgot-password` | Request password reset | No |
| POST | `/api/v1/auth/reset-password` | Reset password | No |
| GET | `/api/v1/auth/profile` | Get user profile | Yes |
| POST | `/api/v1/auth/profile` | Update user profile | Yes |
| POST | `/api/v1/auth/change-password` | Change password | Yes |

### Users

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/users/profile` | Get current user profile | Yes |
| GET | `/api/v1/users/:id` | Get user by ID | Yes |
| PUT | `/api/v1/users/:id` | Update user | Yes |
| DELETE | `/api/v1/users/:id` | Deactivate user | Yes |

## Authentication Flow

### 1. User Registration
```bash
curl -X POST http://localhost:3000/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!",
    "first_name": "John",
    "last_name": "Doe",
    "phone_number": "+1234567890"
  }'
```

### 2. User Login
```bash
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }'
```

### 3. Token Refresh
```bash
curl -X POST http://localhost:3000/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "your_refresh_token_here"
  }'
```

### 4. Authenticated Request
```bash
curl -X GET http://localhost:3000/api/v1/auth/profile \
  -H "Authorization: Bearer your_access_token_here"
```

## Token Management

### Access Token
- **Lifetime**: 15 minutes
- **Purpose**: Authenticate API requests
- **Storage**: Client-side (secure storage recommended)

### Refresh Token
- **Lifetime**: 7 days
- **Purpose**: Generate new access tokens
- **Storage**: Client-side (secure storage required)
- **Rotation**: New refresh token issued on each refresh

### Token Security
- Tokens are hashed and stored in database
- Automatic cleanup of expired tokens
- Immediate revocation on logout
- All user tokens revoked on password change

## Security Features

### Password Security
- bcrypt hashing with 12 rounds
- Strong password requirements
- Password history tracking (future feature)

### Rate Limiting
- General API: 100 requests per 15 minutes
- Authentication: 5 attempts per 15 minutes
- Configurable per endpoint

### Input Validation
- Joi schema validation
- XSS protection
- SQL injection prevention (Prisma ORM)

### CORS & Headers
- Configurable CORS origins
- Security headers with Helmet
- Content Security Policy

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

# Email
SMTP_HOST="smtp.gmail.com"
SMTP_PORT=587
SMTP_USER="your-email@gmail.com"
SMTP_PASS="your-app-password"

# Security
BCRYPT_ROUNDS=12
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
```

## Development

### Scripts
```bash
npm run dev          # Start development server
npm start            # Start production server
npm run db:migrate   # Run database migrations
npm run db:generate  # Generate Prisma client
npm run db:studio    # Open Prisma Studio
npm test             # Run tests
```

### Database Management
```bash
# Create migration
npx prisma migrate dev --name migration_name

# Reset database
npx prisma migrate reset

# View database
npx prisma studio
```

## Production Deployment

### 1. Environment Setup
- Use managed PostgreSQL (AWS RDS, Google Cloud SQL)
- Set strong JWT secrets
- Configure production email service
- Enable HTTPS

### 2. Security Checklist
- [ ] Strong JWT secrets (32+ characters)
- [ ] HTTPS enabled
- [ ] Rate limiting configured
- [ ] CORS origins restricted
- [ ] Database credentials secured
- [ ] Email credentials secured
- [ ] Logging configured
- [ ] Monitoring enabled

### 3. Monitoring
- Database connection health
- Token cleanup jobs
- Error rates and response times
- Failed authentication attempts

## Testing

### Manual Testing
```bash
# Health check
curl http://localhost:3000/health

# Test signup
curl -X POST http://localhost:3000/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!","first_name":"Test","last_name":"User"}'

# Test login
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'
```

### Automated Testing
```bash
npm test
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check DATABASE_URL format
   - Ensure PostgreSQL is running
   - Verify database exists

2. **JWT Token Invalid**
   - Check JWT secrets match
   - Verify token hasn't expired
   - Ensure token is in database

3. **Email Not Sending**
   - Check SMTP credentials
   - Verify email service configuration
   - Check firewall settings

### Logs
- Application logs: `logs/combined.log`
- Error logs: `logs/error.log`
- Database queries: Console (development only)

## Contributing

1. Fork the repository
2. Create feature branch
3. Make changes
4. Add tests
5. Submit pull request

## License

MIT License - see LICENSE file for details

