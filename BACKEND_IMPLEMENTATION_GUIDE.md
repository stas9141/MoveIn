# 🚀 Backend Implementation Guide

## 🎯 **Overview**
This guide provides a complete backend implementation for the MoveIn app's authentication system, including database setup, API development, and security best practices.

---

## **Phase 1: Backend Infrastructure Setup 🏗️**

### **Step 1: Technology Stack Selection**

#### **Recommended Stack:**
- **Backend Framework**: Node.js with Express.js (or Spring Boot for Java)
- **Database**: PostgreSQL with Prisma ORM (or MongoDB with Mongoose)
- **Authentication**: JWT with refresh tokens
- **Password Hashing**: bcrypt (or Argon2 for enhanced security)
- **API Documentation**: Swagger/OpenAPI
- **Deployment**: Docker with AWS/GCP/Azure

#### **Alternative Stack:**
- **Backend**: Spring Boot (Java/Kotlin)
- **Database**: PostgreSQL with JPA/Hibernate
- **Authentication**: Spring Security with JWT
- **Password Hashing**: BCryptPasswordEncoder

---

## **Phase 2: Database Design and Setup 🗄️**

### **Step 2: User Database Schema**

#### **2.1 PostgreSQL Database Schema**
```sql
-- Users table
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
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP
);

-- User sessions table
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL,
    device_info JSONB,
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Password reset tokens table
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Email verification tokens table
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Login attempts table (for security monitoring)
CREATE TABLE login_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    ip_address INET NOT NULL,
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(100),
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_email_verified ON users(email_verified);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_refresh_token ON user_sessions(refresh_token_hash);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_login_attempts_email ON login_attempts(email);
CREATE INDEX idx_login_attempts_ip ON login_attempts(ip_address);
```

#### **2.2 Prisma Schema (Alternative ORM)**
```prisma
// schema.prisma
generator client {
  provider = "prisma-client-js"
}

datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

model User {
  id                    String    @id @default(uuid())
  email                 String    @unique
  passwordHash          String    @map("password_hash")
  firstName             String    @map("first_name")
  lastName              String    @map("last_name")
  phoneNumber           String?   @map("phone_number")
  profilePictureUrl     String?   @map("profile_picture_url")
  emailVerified         Boolean   @default(false) @map("email_verified")
  phoneVerified         Boolean   @default(false) @map("phone_verified")
  isActive              Boolean   @default(true) @map("is_active")
  createdAt             DateTime  @default(now()) @map("created_at")
  updatedAt             DateTime  @updatedAt @map("updated_at")
  lastLoginAt           DateTime? @map("last_login_at")
  failedLoginAttempts   Int       @default(0) @map("failed_login_attempts")
  lockedUntil           DateTime? @map("locked_until")

  // Relations
  sessions              UserSession[]
  passwordResetTokens   PasswordResetToken[]
  emailVerificationTokens EmailVerificationToken[]
  loginAttempts         LoginAttempt[]

  @@map("users")
}

model UserSession {
  id              String   @id @default(uuid())
  userId          String   @map("user_id")
  refreshTokenHash String  @map("refresh_token_hash")
  deviceInfo      Json?
  ipAddress       String?  @map("ip_address")
  userAgent       String?  @map("user_agent")
  expiresAt       DateTime @map("expires_at")
  createdAt       DateTime @default(now()) @map("created_at")
  isActive        Boolean  @default(true) @map("is_active")

  user            User     @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@map("user_sessions")
}

model PasswordResetToken {
  id        String    @id @default(uuid())
  userId    String    @map("user_id")
  tokenHash String    @map("token_hash")
  expiresAt DateTime  @map("expires_at")
  usedAt    DateTime? @map("used_at")
  createdAt DateTime  @default(now()) @map("created_at")

  user      User      @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@map("password_reset_tokens")
}

model EmailVerificationToken {
  id        String    @id @default(uuid())
  userId    String    @map("user_id")
  tokenHash String    @map("token_hash")
  expiresAt DateTime  @map("expires_at")
  usedAt    DateTime? @map("used_at")
  createdAt DateTime  @default(now()) @map("created_at")

  user      User      @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@map("email_verification_tokens")
}

model LoginAttempt {
  id            String    @id @default(uuid())
  email         String
  ipAddress     String    @map("ip_address")
  userAgent     String?   @map("user_agent")
  success       Boolean
  failureReason String?   @map("failure_reason")
  attemptedAt   DateTime  @default(now()) @map("attempted_at")

  user          User      @relation(fields: [email], references: [email])

  @@map("login_attempts")
}
```

---

## **Phase 3: API Development 🔌**

### **Step 3: Authentication API Implementation**

#### **3.1 Node.js/Express Implementation**

```javascript
// server.js
const express = require('express');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { PrismaClient } = require('@prisma/client');
const rateLimit = require('express-rate-limit');
const helmet = require('helmet');
const cors = require('cors');

const app = express();
const prisma = new PrismaClient();

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Rate limiting
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // limit each IP to 5 requests per windowMs
  message: 'Too many authentication attempts, please try again later.',
  standardHeaders: true,
  legacyHeaders: false,
});

// Environment variables
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'your-refresh-secret';
const ACCESS_TOKEN_EXPIRY = '15m';
const REFRESH_TOKEN_EXPIRY = '7d';

// Utility functions
const generateTokens = (userId) => {
  const accessToken = jwt.sign(
    { userId, type: 'access' },
    JWT_SECRET,
    { expiresIn: ACCESS_TOKEN_EXPIRY }
  );
  
  const refreshToken = jwt.sign(
    { userId, type: 'refresh' },
    JWT_REFRESH_SECRET,
    { expiresIn: REFRESH_TOKEN_EXPIRY }
  );
  
  return { accessToken, refreshToken };
};

const hashPassword = async (password) => {
  const saltRounds = 12;
  return await bcrypt.hash(password, saltRounds);
};

const verifyPassword = async (password, hash) => {
  return await bcrypt.compare(password, hash);
};

// Input validation
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

const validatePassword = (password) => {
  // At least 8 characters, 1 uppercase, 1 lowercase, 1 number, 1 special character
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
  return passwordRegex.test(password);
};

// Sign-up API
app.post('/api/signup', authLimiter, async (req, res) => {
  try {
    const { email, password, firstName, lastName, phoneNumber } = req.body;
    
    // Input validation
    if (!email || !password || !firstName || !lastName) {
      return res.status(400).json({
        success: false,
        message: 'All required fields must be provided'
      });
    }
    
    if (!validateEmail(email)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid email format'
      });
    }
    
    if (!validatePassword(password)) {
      return res.status(400).json({
        success: false,
        message: 'Password must be at least 8 characters with uppercase, lowercase, number, and special character'
      });
    }
    
    // Check if user already exists
    const existingUser = await prisma.user.findUnique({
      where: { email: email.toLowerCase() }
    });
    
    if (existingUser) {
      return res.status(409).json({
        success: false,
        message: 'User with this email already exists'
      });
    }
    
    // Hash password
    const passwordHash = await hashPassword(password);
    
    // Create user
    const user = await prisma.user.create({
      data: {
        email: email.toLowerCase(),
        passwordHash,
        firstName,
        lastName,
        phoneNumber: phoneNumber || null
      },
      select: {
        id: true,
        email: true,
        firstName: true,
        lastName: true,
        emailVerified: true,
        createdAt: true
      }
    });
    
    // Generate tokens
    const { accessToken, refreshToken } = generateTokens(user.id);
    
    // Store refresh token
    await prisma.userSession.create({
      data: {
        userId: user.id,
        refreshTokenHash: await hashPassword(refreshToken),
        deviceInfo: req.headers['user-agent'],
        ipAddress: req.ip,
        expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7 days
      }
    });
    
    // Log successful signup
    await prisma.loginAttempt.create({
      data: {
        email: email.toLowerCase(),
        ipAddress: req.ip,
        userAgent: req.headers['user-agent'],
        success: true
      }
    });
    
    res.status(201).json({
      success: true,
      message: 'User created successfully',
      data: {
        user,
        tokens: {
          accessToken,
          refreshToken
        }
      }
    });
    
  } catch (error) {
    console.error('Signup error:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// Login API
app.post('/api/login', authLimiter, async (req, res) => {
  try {
    const { email, password } = req.body;
    
    // Input validation
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Email and password are required'
      });
    }
    
    if (!validateEmail(email)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid email format'
      });
    }
    
    // Find user
    const user = await prisma.user.findUnique({
      where: { email: email.toLowerCase() }
    });
    
    if (!user) {
      // Log failed attempt
      await prisma.loginAttempt.create({
        data: {
          email: email.toLowerCase(),
          ipAddress: req.ip,
          userAgent: req.headers['user-agent'],
          success: false,
          failureReason: 'USER_NOT_FOUND'
        }
      });
      
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password'
      });
    }
    
    // Check if account is locked
    if (user.lockedUntil && user.lockedUntil > new Date()) {
      return res.status(423).json({
        success: false,
        message: 'Account is temporarily locked due to too many failed attempts'
      });
    }
    
    // Check if account is active
    if (!user.isActive) {
      return res.status(403).json({
        success: false,
        message: 'Account is deactivated'
      });
    }
    
    // Verify password
    const isPasswordValid = await verifyPassword(password, user.passwordHash);
    
    if (!isPasswordValid) {
      // Increment failed attempts
      const failedAttempts = user.failedLoginAttempts + 1;
      const lockUntil = failedAttempts >= 5 ? new Date(Date.now() + 30 * 60 * 1000) : null; // 30 minutes
      
      await prisma.user.update({
        where: { id: user.id },
        data: {
          failedLoginAttempts: failedAttempts,
          lockedUntil: lockUntil
        }
      });
      
      // Log failed attempt
      await prisma.loginAttempt.create({
        data: {
          email: email.toLowerCase(),
          ipAddress: req.ip,
          userAgent: req.headers['user-agent'],
          success: false,
          failureReason: 'INVALID_PASSWORD'
        }
      });
      
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password'
      });
    }
    
    // Reset failed attempts on successful login
    await prisma.user.update({
      where: { id: user.id },
      data: {
        failedLoginAttempts: 0,
        lockedUntil: null,
        lastLoginAt: new Date()
      }
    });
    
    // Generate tokens
    const { accessToken, refreshToken } = generateTokens(user.id);
    
    // Store refresh token
    await prisma.userSession.create({
      data: {
        userId: user.id,
        refreshTokenHash: await hashPassword(refreshToken),
        deviceInfo: req.headers['user-agent'],
        ipAddress: req.ip,
        expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7 days
      }
    });
    
    // Log successful login
    await prisma.loginAttempt.create({
      data: {
        email: email.toLowerCase(),
        ipAddress: req.ip,
        userAgent: req.headers['user-agent'],
        success: true
      }
    });
    
    res.json({
      success: true,
      message: 'Login successful',
      data: {
        user: {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          emailVerified: user.emailVerified
        },
        tokens: {
          accessToken,
          refreshToken
        }
      }
    });
    
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// Refresh token API
app.post('/api/refresh', async (req, res) => {
  try {
    const { refreshToken } = req.body;
    
    if (!refreshToken) {
      return res.status(400).json({
        success: false,
        message: 'Refresh token is required'
      });
    }
    
    // Verify refresh token
    const decoded = jwt.verify(refreshToken, JWT_REFRESH_SECRET);
    
    if (decoded.type !== 'refresh') {
      return res.status(401).json({
        success: false,
        message: 'Invalid token type'
      });
    }
    
    // Find active session
    const session = await prisma.userSession.findFirst({
      where: {
        userId: decoded.userId,
        isActive: true,
        expiresAt: { gt: new Date() }
      },
      include: { user: true }
    });
    
    if (!session) {
      return res.status(401).json({
        success: false,
        message: 'Invalid or expired refresh token'
      });
    }
    
    // Verify refresh token hash
    const isTokenValid = await verifyPassword(refreshToken, session.refreshTokenHash);
    
    if (!isTokenValid) {
      return res.status(401).json({
        success: false,
        message: 'Invalid refresh token'
      });
    }
    
    // Generate new tokens
    const { accessToken, refreshToken: newRefreshToken } = generateTokens(decoded.userId);
    
    // Update session with new refresh token
    await prisma.userSession.update({
      where: { id: session.id },
      data: {
        refreshTokenHash: await hashPassword(newRefreshToken),
        expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7 days
      }
    });
    
    res.json({
      success: true,
      message: 'Tokens refreshed successfully',
      data: {
        tokens: {
          accessToken,
          refreshToken: newRefreshToken
        }
      }
    });
    
  } catch (error) {
    console.error('Refresh token error:', error);
    res.status(401).json({
      success: false,
      message: 'Invalid refresh token'
    });
  }
});

// Logout API
app.post('/api/logout', async (req, res) => {
  try {
    const { refreshToken } = req.body;
    
    if (refreshToken) {
      // Deactivate session
      const session = await prisma.userSession.findFirst({
        where: {
          refreshTokenHash: await hashPassword(refreshToken),
          isActive: true
        }
      });
      
      if (session) {
        await prisma.userSession.update({
          where: { id: session.id },
          data: { isActive: false }
        });
      }
    }
    
    res.json({
      success: true,
      message: 'Logged out successfully'
    });
    
  } catch (error) {
    console.error('Logout error:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
```

#### **3.2 Spring Boot Implementation (Alternative)**

```java
// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and setters
}

// AuthController.java
@RestController
@RequestMapping("/api")
@Validated
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        try {
            User user = userService.createUser(request);
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(true, "User created successfully", 
                    new UserDto(user), new TokenPair(accessToken, refreshToken)));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthResponse(false, "User with this email already exists", null, null));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.authenticateUser(request.getEmail(), request.getPassword());
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
            
            return ResponseEntity.ok(new AuthResponse(true, "Login successful",
                new UserDto(user), new TokenPair(accessToken, refreshToken)));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(false, "Invalid email or password", null, null));
        }
    }
}
```

---

## **Phase 4: Security Implementation 🔒**

### **Step 4: Security Best Practices**

#### **4.1 Environment Configuration**
```bash
# .env
DATABASE_URL="postgresql://username:password@localhost:5432/movein_db"
JWT_SECRET="your-super-secret-jwt-key-here"
JWT_REFRESH_SECRET="your-super-secret-refresh-key-here"
NODE_ENV="production"
PORT=3000
```

#### **4.2 Security Middleware**
```javascript
// security.js
const rateLimit = require('express-rate-limit');
const helmet = require('helmet');
const cors = require('cors');

// Rate limiting for different endpoints
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // limit each IP to 5 requests per windowMs
  message: 'Too many authentication attempts, please try again later.',
  standardHeaders: true,
  legacyHeaders: false,
});

const generalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  standardHeaders: true,
  legacyHeaders: false,
});

// CORS configuration
const corsOptions = {
  origin: process.env.NODE_ENV === 'production' 
    ? ['https://yourdomain.com'] 
    : ['http://localhost:3000', 'http://localhost:8080'],
  credentials: true,
  optionsSuccessStatus: 200
};

module.exports = {
  authLimiter,
  generalLimiter,
  corsOptions
};
```

---

## **Phase 5: Testing and Deployment 🧪**

### **Step 5: Testing Implementation**

#### **5.1 Unit Tests**
```javascript
// auth.test.js
const request = require('supertest');
const app = require('../server');

describe('Authentication API', () => {
  describe('POST /api/signup', () => {
    it('should create a new user with valid data', async () => {
      const userData = {
        email: 'test@example.com',
        password: 'TestPassword123!',
        firstName: 'John',
        lastName: 'Doe'
      };
      
      const response = await request(app)
        .post('/api/signup')
        .send(userData)
        .expect(201);
      
      expect(response.body.success).toBe(true);
      expect(response.body.data.user.email).toBe(userData.email);
      expect(response.body.data.tokens.accessToken).toBeDefined();
    });
    
    it('should reject invalid email format', async () => {
      const userData = {
        email: 'invalid-email',
        password: 'TestPassword123!',
        firstName: 'John',
        lastName: 'Doe'
      };
      
      const response = await request(app)
        .post('/api/signup')
        .send(userData)
        .expect(400);
      
      expect(response.body.success).toBe(false);
      expect(response.body.message).toContain('Invalid email format');
    });
  });
});
```

#### **5.2 Integration Tests**
```javascript
// integration.test.js
describe('Authentication Flow', () => {
  it('should complete full authentication flow', async () => {
    // 1. Sign up
    const signupResponse = await request(app)
      .post('/api/signup')
      .send({
        email: 'integration@example.com',
        password: 'TestPassword123!',
        firstName: 'Integration',
        lastName: 'Test'
      });
    
    expect(signupResponse.status).toBe(201);
    
    // 2. Login
    const loginResponse = await request(app)
      .post('/api/login')
      .send({
        email: 'integration@example.com',
        password: 'TestPassword123!'
      });
    
    expect(loginResponse.status).toBe(200);
    
    // 3. Refresh token
    const refreshResponse = await request(app)
      .post('/api/refresh')
      .send({
        refreshToken: loginResponse.body.data.tokens.refreshToken
      });
    
    expect(refreshResponse.status).toBe(200);
    
    // 4. Logout
    const logoutResponse = await request(app)
      .post('/api/logout')
      .send({
        refreshToken: loginResponse.body.data.tokens.refreshToken
      });
    
    expect(logoutResponse.status).toBe(200);
  });
});
```

---

## **Phase 6: Deployment and Monitoring 📊**

### **Step 6: Production Deployment**

#### **6.1 Docker Configuration**
```dockerfile
# Dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

EXPOSE 3000

CMD ["npm", "start"]
```

#### **6.2 Docker Compose**
```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "3000:3000"
    environment:
      - DATABASE_URL=postgresql://postgres:password@db:5432/movein_db
      - JWT_SECRET=your-jwt-secret
      - JWT_REFRESH_SECRET=your-refresh-secret
    depends_on:
      - db
    restart: unless-stopped

  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=movein_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

---

## **🎯 Implementation Checklist**

### **✅ Backend Setup**
- [ ] Choose technology stack (Node.js/Express or Spring Boot)
- [ ] Set up database (PostgreSQL)
- [ ] Configure environment variables
- [ ] Set up security middleware

### **✅ Database Implementation**
- [ ] Create user database schema
- [ ] Implement password hashing (bcrypt/Argon2)
- [ ] Set up session management
- [ ] Create indexes for performance

### **✅ API Development**
- [ ] Implement sign-up API (`/api/signup`)
- [ ] Implement login API (`/api/login`)
- [ ] Implement refresh token API (`/api/refresh`)
- [ ] Implement logout API (`/api/logout`)

### **✅ Security Features**
- [ ] Input validation and sanitization
- [ ] Rate limiting
- [ ] JWT token management
- [ ] Password strength requirements
- [ ] Account lockout policies

### **✅ Testing and Deployment**
- [ ] Unit tests
- [ ] Integration tests
- [ ] Docker configuration
- [ ] Production deployment

---

## **🚀 Next Steps**

1. **Choose your backend technology** (Node.js/Express or Spring Boot)
2. **Set up the database** with the provided schema
3. **Implement the authentication APIs** step by step
4. **Add security features** and validation
5. **Test thoroughly** before deployment
6. **Deploy to production** with proper monitoring

**This backend implementation provides a solid foundation for your MoveIn app's authentication system with enterprise-grade security and scalability!** 🎯

Would you like me to help you implement any specific part of this backend system?

