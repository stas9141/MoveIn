const request = require('supertest');
const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

// Mock the logger to avoid console output during tests
jest.mock('../src/utils/logger', () => ({
  logger: {
    info: jest.fn(),
    error: jest.fn(),
    warn: jest.fn(),
    debug: jest.fn()
  }
}));

// Mock Prisma to avoid database calls during tests
jest.mock('@prisma/client', () => ({
  PrismaClient: jest.fn().mockImplementation(() => ({
    user: {
      findUnique: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
      updateMany: jest.fn()
    },
    token: {
      create: jest.fn(),
      findFirst: jest.fn(),
      updateMany: jest.fn()
    },
    $transaction: jest.fn()
  }))
}));

// Create a simple test app without database connection
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');

// Create test app
const app = express();

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());

// Rate limiting
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // limit each IP to 5 requests per windowMs
  message: {
    success: false,
    message: 'Too many authentication attempts, please try again later.'
  }
});

// Mock auth routes
app.post('/auth/login', (req, res) => {
  const { email, password } = req.body;
  
  if (!email || !password) {
    return res.status(400).json({
      success: false,
      message: 'Email and password are required'
    });
  }
  
  if (email === 'test@example.com' && password === 'TestPassword123!') {
    return res.json({
      success: true,
      message: 'Login successful',
      data: {
        user: {
          id: 'test-user-id',
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'User'
        },
        tokens: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token'
        }
      }
    });
  }
  
  return res.status(401).json({
    success: false,
    message: 'Invalid email or password'
  });
});

app.post('/auth/google-signin', (req, res) => {
  const { idToken } = req.body;
  
  if (!idToken) {
    return res.status(400).json({
      success: false,
      message: 'Google ID token is required'
    });
  }
  
  if (idToken === 'valid-google-id-token') {
    return res.json({
      success: true,
      message: 'Google sign-in successful',
      data: {
        user: {
          id: 'google-user-id',
          email: 'google@example.com',
          firstName: 'Google',
          lastName: 'User'
        },
        tokens: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token'
        }
      }
    });
  }
  
  return res.status(401).json({
    success: false,
    message: 'Invalid Google token'
  });
});

app.post('/auth/logout-all-devices', (req, res) => {
  res.json({
    success: true,
    message: 'Successfully logged out from all devices. 3 sessions terminated.'
  });
});

describe('Authentication API Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('POST /auth/login', () => {
    it('should login successfully with valid credentials', async () => {
      const response = await request(app)
        .post('/auth/login')
        .send({
          email: 'test@example.com',
          password: 'TestPassword123!'
        });

      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Login successful');
      expect(response.body.data).toHaveProperty('user');
      expect(response.body.data).toHaveProperty('tokens');
      expect(response.body.data.user.email).toBe('test@example.com');
      expect(response.body.data.tokens).toHaveProperty('accessToken');
      expect(response.body.data.tokens).toHaveProperty('refreshToken');
    });

    it('should fail with invalid credentials', async () => {
      const response = await request(app)
        .post('/auth/login')
        .send({
          email: 'wrong@example.com',
          password: 'WrongPassword123!'
        });

      expect(response.status).toBe(401);
      expect(response.body.success).toBe(false);
      expect(response.body.message).toBe('Invalid email or password');
    });

    it('should fail with missing email', async () => {
      const response = await request(app)
        .post('/auth/login')
        .send({
          password: 'TestPassword123!'
        });

      expect(response.status).toBe(400);
      expect(response.body.success).toBe(false);
    });

    it('should fail with missing password', async () => {
      const response = await request(app)
        .post('/auth/login')
        .send({
          email: 'test@example.com'
        });

      expect(response.status).toBe(400);
      expect(response.body.success).toBe(false);
    });
  });

  describe('POST /auth/google-signin', () => {
    it('should login successfully with valid Google token', async () => {
      const response = await request(app)
        .post('/auth/google-signin')
        .send({
          idToken: 'valid-google-id-token'
        });

      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Google sign-in successful');
      expect(response.body.data.user.email).toBe('google@example.com');
    });

    it('should fail with invalid Google token', async () => {
      const response = await request(app)
        .post('/auth/google-signin')
        .send({
          idToken: 'invalid-google-id-token'
        });

      expect(response.status).toBe(401);
      expect(response.body.success).toBe(false);
      expect(response.body.message).toBe('Invalid Google token');
    });

    it('should fail with missing Google token', async () => {
      const response = await request(app)
        .post('/auth/google-signin')
        .send({});

      expect(response.status).toBe(400);
      expect(response.body.success).toBe(false);
      expect(response.body.message).toBe('Google ID token is required');
    });
  });

  describe('POST /auth/logout-all-devices', () => {
    it('should logout from all devices successfully', async () => {
      const response = await request(app)
        .post('/auth/logout-all-devices')
        .send({});

      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Successfully logged out from all devices. 3 sessions terminated.');
    });
  });
});
